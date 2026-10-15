package com.rajatxo.lutea.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
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
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.DrawScope.Companion
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import androidx.compose.ui.layout.onSizeChanged
import kotlinx.coroutines.delay

/**
 * Cloud-wipe text reveal — text is static; a "blur cloud" sweeps left-to-right across
 * it, revealing letters as it passes. Mirrors the Lutea-Inspiration video's signature
 * text reveal (Record_2026-10-15-19-45-47.mp4).
 *
 * Behavior:
 *   - Text renders in its FINAL position from the start (no slide-in).
 *   - Letters LEFT of the cloud-center are sharp + visible (already revealed).
 *   - Letters UNDER the cloud-center are blurred (transitioning).
 *   - Letters RIGHT of the cloud-center are invisible (not yet revealed).
 *   - Cloud width ~25% of text width (matches inspiration's "moderate, ~3-4 letters").
 *   - Sweep duration ~1200ms (inspiration was ~1.0-1.5s).
 *   - Cloud is a soft-edged horizontal gradient — not a hard line.
 *
 * Implementation:
 *   We render THREE layers stacked on top of each other, in this Z-order:
 *     1. SHARP TEXT (full opacity, blurred = 0) — this is what gets revealed eventually.
 *     2. BLURRED TEXT (RenderEffect blur, 8dp) — same text, but alpha-masked to ONLY
 *        show inside the cloud region (a soft horizontal gradient brush). This gives
 *        the "blur cloud" appearance.
 *   Layer 1 is rendered first as the "always sharp" base. Layer 2 is drawn ON TOP
 *   but only within the cloud mask — so under the cloud you see blurred text (which
 *   still mostly matches the sharp text underneath, but blurred). To hide the not-
 *   yet-revealed text to the RIGHT of the cloud, we also need to clip Layer 1 to
 *   only show what's LEFT of (or under) the cloud.
 *
 *   Simpler approach (taken here):
 *     - Layer A: sharp text, clipped to the "left of cloud center" region (revealed).
 *     - Layer B: blurred text, masked to the "cloud region" (transitioning).
 *     - Region to the right of the cloud = nothing drawn (still hidden).
 *
 *   This is done with a single drawWithContent pass using BlendMode + a moving
 *   horizontal gradient brush as the alpha source.
 *
 * @param text           the text to reveal
 * @param delayMillis    how long to wait before starting the sweep (for staggering)
 * @param durationMillis how long the full left-to-right sweep takes
 * @param cloudWidthFraction  cloud width as a fraction of text width (0.25 = ~25%)
 */
@Composable
fun CloudWipeText(
    text: String,
    modifier: Modifier = Modifier,
    delayMillis: Int = 0,
    durationMillis: Int = 1200,
    cloudWidthFraction: Float = 0.25f,
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

    // progress 0 → 1: 0 = cloud at far left, 1 = cloud exited past far right
    val progress by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = tween(durationMillis, easing = LinearEasing),
        label = "cloud_sweep",
    )

    val density = LocalDensity.current
    val blurRadius: Dp = 8.dp
    val blurRadiusPx = with(density) { blurRadius.toPx() }

    // Track the actual rendered text width so the cloud width scales with it.
    Box(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(align = Alignment.Top)
            .onSizeChanged { size ->
                if (size.width > 0) textWidthPx = size.width
            },
        contentAlignment = Alignment.Center,
    ) {
        val style = TextStyle(
            color = color,
            fontSize = fontSize.sp,
            fontWeight = fontWeight,
            letterSpacing = letterSpacing.sp,
            textAlign = textAlign,
        ).let { if (lineHeight != null) it.copy(lineHeight = lineHeight.sp) else it }

        if (textWidthPx == 0) {
            // First pass: render text just so we can measure its width via onSizeChanged.
            Text(text = text, style = style)
            return@Box
        }

        val cloudWidthPx = textWidthPx * cloudWidthFraction
        // Cloud center x goes from -cloudWidth/2 (cloud just entering from left) to
        // textWidth + cloudWidth/2 (cloud just exiting past right).
        val cloudCenterPx = -cloudWidthPx / 2f +
            (textWidthPx + cloudWidthPx) * progress

        // ── Layer A: SHARP TEXT — clipped to "left of cloud's right edge" ──────
        // Shows the already-revealed portion. We clip via drawWithContent + a
        // horizontal gradient brush as the alpha mask.
        Text(
            text = text,
            style = style,
            modifier = Modifier
                .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                .drawWithContent {
                    drawContent()
                    // Erase everything to the right of the cloud's right edge by
                    // drawing a transparent rectangle on top of it. The right edge
                    // is at cloudCenter + cloudWidth/2 (where the blur ends).
                    val rightEdge = (cloudCenterPx + cloudWidthPx / 2f)
                        .coerceIn(0f, size.width)
                    if (rightEdge < size.width) {
                        drawRect(
                            color = Color.Transparent,
                            topLeft = Offset(rightEdge, 0f),
                            size = Size(size.width - rightEdge, size.height),
                            blendMode = BlendMode.Clear,
                        )
                    }
                },
        )

        // ── Layer B: BLURRED TEXT — masked to cloud region only ─────────────────
        // Same text, with blur applied, but only visible inside the cloud band.
        Text(
            text = text,
            style = style,
            modifier = Modifier
                .blur(blurRadius)
                .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                .drawWithContent {
                    drawContent()
                    // Erase everything outside the cloud band (left + right of it)
                    // by drawing transparent rects on both sides.
                    val leftEdge = (cloudCenterPx - cloudWidthPx / 2f)
                        .coerceIn(0f, size.width)
                    val rightEdge = (cloudCenterPx + cloudWidthPx / 2f)
                        .coerceIn(0f, size.width)

                    // Erase left of cloud
                    if (leftEdge > 0f) {
                        drawRect(
                            color = Color.Transparent,
                            topLeft = Offset(0f, 0f),
                            size = Size(leftEdge, size.height),
                            blendMode = BlendMode.Clear,
                        )
                    }
                    // Erase right of cloud
                    if (rightEdge < size.width) {
                        drawRect(
                            color = Color.Transparent,
                            topLeft = Offset(rightEdge, 0f),
                            size = Size(size.width - rightEdge, size.height),
                            blendMode = BlendMode.Clear,
                        )
                    }
                },
        )
    }
}
