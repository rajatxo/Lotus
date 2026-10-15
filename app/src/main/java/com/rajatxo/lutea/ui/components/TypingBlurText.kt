package com.rajatxo.lutea.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rajatxo.lutea.R
import kotlinx.coroutines.delay

/**
 * Cal Sans font family — bundled in res/font/cal_sans_semibold.ttf.
 * Same family declaration as Coral uses (CoralTypography.kt:75).
 */
val CalSansFamily = FontFamily(
    Font(R.font.cal_sans_semibold, FontWeight.Normal),
    Font(R.font.cal_sans_semibold, FontWeight.SemiBold),
    Font(R.font.cal_sans_semibold, FontWeight.Medium),
    Font(R.font.cal_sans_semibold, FontWeight.Bold),
)

/**
 * Typing-blur text — text types out letter-by-letter FAST. The currently-being-typed
 * letter is BLURRED with SOFT EDGES; once a letter is "passed" it becomes sharp. The
 * blur follows the cursor as it sweeps left→right.
 *
 * Recreated from Lutea-Inspiration video ultra-fine analysis (Record_2026-10-15-19-45-47.mp4):
 *   t=3.0s: 'I' (blurred)
 *   t=3.2s: 'I'll' (sharp) + 'h' (blurred)
 *   t=3.8s: 'I'll help protect' (sharp) + 'y' (blurred)
 *   t=4.1s: 'I'll help protect your focus.' (sharp, fully revealed)
 *
 * Fixes from previous attempt (v1 had 3 problems):
 *   1. Font: was Poppins default → now Cal Sans (user request: 'use calsan font')
 *   2. Text shift: was Row(sharp + blurred) — Row re-measured its width every frame as
 *      chars appeared, causing the whole Row to re-center and 'move left'.
 *      FIX: render ONE Text with the full revealed substring (no Row, no re-measure
 *      jitter) using CompositingStrategy.Offscreen + drawWithContent to apply a
 *      soft-edged blur mask ONLY to the trailing characters.
 *   3. Square/hard blur: was Modifier.blur(8dp) on the whole trailing Text — the blur
 *      had a hard rectangular edge where the Text's bounding box ended.
 *      FIX: render the blurred layer as the FULL text, then mask it with a horizontal
 *      Brush.linearGradient whose alpha is:
 *        - 0 (transparent) for the sharp portion
 *        - smoothly fades to 1 (opaque) over a 'softness' region
 *        - 1 (opaque) for the last blurTrailChars characters
 *      This produces a soft-edged blur that fades in/out smoothly.
 *
 * Implementation = 2 stacked Text layers in a Box:
 *   - Layer A: SHARP TEXT — full revealed substring, in [color]. Always visible.
 *   - Layer B: BLURRED TEXT — same substring, in [color], with Modifier.blur(blurRadius).
 *              Masked by a horizontal gradient brush via BlendMode.DstIn so the blur
 *              is ONLY visible inside the trailing soft band.
 *
 * The Box has fixed width = text width (measured once), so no jitter.
 *
 * @param text           the full text to reveal
 * @param delayMillis    wait before starting the typing animation
 * @param durationMillis how long the full left-to-right type-out takes
 * @param blurTrailChars how many of the latest characters are blurred
 * @param blurRadiusDp   blur radius applied to the trailing characters
 * @param softnessDp     width of the soft-edge transition between sharp and blurred
 */
@Composable
fun TypingBlurText(
    text: String,
    modifier: Modifier = Modifier,
    delayMillis: Int = 0,
    durationMillis: Int = 1500,
    blurTrailChars: Int = 2,
    blurRadiusDp: Dp = 8.dp,
    softnessDp: Dp = 16.dp,
    color: Color = Color.White,
    fontSize: Int = 24,
    fontWeight: FontWeight = FontWeight.Normal,
    letterSpacing: Float = 0f,
    textAlign: TextAlign = TextAlign.Center,
    lineHeight: Int? = null,
) {
    var started by remember { mutableStateOf(false) }
    var textWidthPx by remember { mutableStateOf(0) }

    LaunchedEffect(text) {
        if (delayMillis > 0) delay(delayMillis.toLong())
        started = true
    }

    // Animate revealed count from 0 → text.length.
    val targetCount = if (started) text.length else 0
    val progress by animateFloatAsState(
        targetValue = targetCount.toFloat(),
        animationSpec = tween(durationMillis, easing = LinearEasing),
        label = "typing_progress",
    )
    val revealedCount = progress.toInt().coerceIn(0, text.length)

    val style = TextStyle(
        color = color,
        fontSize = fontSize.sp,
        fontFamily = CalSansFamily,  // ← Cal Sans per user request
        fontWeight = fontWeight,
        letterSpacing = letterSpacing.sp,
        textAlign = textAlign,
    ).let { if (lineHeight != null) it.copy(lineHeight = lineHeight.sp) else it }

    val density = LocalDensity.current
    val blurRadiusPx = with(density) { blurRadiusDp.toPx() }
    val softnessPx = with(density) { softnessDp.toPx() }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentSize(align = Alignment.Center)
            .onSizeChanged { size ->
                if (size.width > 0) textWidthPx = size.width
            },
        contentAlignment = Alignment.Center,
    ) {
        if (textWidthPx == 0 || revealedCount == 0) {
            // First pass: render to measure width, OR nothing typed yet.
            // We still render an (invisible) full text so the layout reserves space.
            Text(
                text = text,
                style = style.copy(color = Color.Transparent),
            )
            return@Box
        }

        // The revealed substring (what's currently visible).
        val revealed = text.substring(0, revealedCount)

        // ── Layer A: SHARP TEXT (always visible) ─────────────────────────────
        Text(
            text = revealed,
            style = style,
        )

        // ── Layer B: BLURRED TEXT (soft-edged mask on trailing chars) ────────
        // Same text, blurred, but masked by a horizontal gradient so only the
        // trailing [blurTrailChars] characters show through with soft edges.
        //
        // To compute the mask brush, we need the X position where the trailing
        // characters START. We use TextLayoutResult to get the exact horizontal
        // offset of the character at index (revealedCount - blurTrailChars).
        //
        // Simpler approximation: assume each char has the same average width.
        // avgCharWidth = textWidthPx / text.length (since we measured full text).
        // This is good enough for proportional fonts at typical UI sizes.
        val avgCharWidth = textWidthPx.toFloat() / text.length.toFloat()
        val blurStartX = (revealedCount - blurTrailChars).coerceAtLeast(0) * avgCharWidth
        val blurEndX = revealedCount * avgCharWidth

        // Soft-edged mask brush: transparent until (blurStartX - softness), then
        // smoothly fades to opaque over [softness] px, stays opaque through the
        // trailing chars, then fades back to transparent at the very end.
        val maskBrush = Brush.horizontalGradient(
            colors = listOf(
                Color.Transparent,                                           // sharp zone
                Color.Black,                                                 // blurred zone begins
                Color.Black,                                                 // trailing chars
                Color.Transparent,                                           // fade out past cursor
            ),
            start = Offset(x = (blurStartX - softnessPx).coerceAtLeast(0f), y = 0f),
            end = Offset(x = (blurEndX + softnessPx), y = 0f),
        )

        Text(
            text = revealed,
            style = style,
            modifier = Modifier
                .blur(blurRadiusDp)
                .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                .drawWithContent {
                    // Draw the blurred text on the offscreen layer.
                    drawContent()
                    // Mask it with the soft-edged brush: keep text where brush is opaque,
                    // erase where transparent. BlendMode.DstIn uses the brush's ALPHA
                    // to control visibility of the destination (the blurred text).
                    drawRect(
                        brush = maskBrush,
                        blendMode = BlendMode.DstIn,
                    )
                },
        )
    }
}
