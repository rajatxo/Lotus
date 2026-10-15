package com.rajatxo.lutea.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/**
 * Glow-flow text — the text is fully sharp and visible from the start. A smooth glow
 * (soft white light) flows through the sentence from left to right, illuminating each
 * letter as the glow passes over it. After the glow exits the right edge, the text
 * returns to its base color and stays put.
 *
 * This is the "smooth glowing effect flow through the sentence" per user spec:
 *   'forget about blur, I want a smooth glowing effect flow through the sentence'
 *
 * Implementation = two stacked Text layers:
 *
 *   Layer A: BASE TEXT — renders in [color] (default white). Always visible underneath.
 *
 *   Layer B: GLOW TEXT — renders the SAME text in pure white, but masked by a
 *            horizontal gradient brush that's transparent everywhere EXCEPT inside
 *            a moving "glow band" (default ~30% of text width). The brush's
 *            transparent center fades smoothly to opaque at the band center, then
 *            back to transparent — so the glow has soft edges, not a hard line.
 *
 *            Layer B uses CompositingStrategy.Offscreen + BlendMode.DstIn so the
 *            brush alpha controls where the white text shows. Where the brush is
 *            transparent, Layer B is invisible and Layer A (the base text) shows
 *            through. Where the brush is opaque, Layer B covers Layer A with
 *            bright white — that's the glow.
 *
 *   The brush's center x animates from -glowWidth/2 (off-screen left) to
 *   textWidth + glowWidth/2 (off-screen right) over [durationMillis], so the
 *   glow band sweeps continuously across the text.
 *
 * @param text           the text to display
 * @param delayMillis    how long to wait before the glow sweep starts
 * @param durationMillis how long the left-to-right sweep takes
 * @param color         base text color (default white)
 * @param glowColor     color of the glow (default warm white)
 * @param glowWidthFraction  glow band width as a fraction of text width (0.3 = ~30%)
 */
@Composable
fun GlowFlowText(
    text: String,
    modifier: Modifier = Modifier,
    delayMillis: Int = 0,
    durationMillis: Int = 1500,
    color: Color = Color.White,
    glowColor: Color = Color(0xFFFFFFFF),
    glowWidthFraction: Float = 0.3f,
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

    // Animate glow sweep 0 → 1 over the duration (one-shot, no repeat).
    val glowProgress by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = tween(durationMillis, easing = LinearEasing),
        label = "glow_sweep",
    )

    val style = TextStyle(
        color = color,
        fontSize = fontSize.sp,
        fontWeight = fontWeight,
        letterSpacing = letterSpacing.sp,
        textAlign = textAlign,
    ).let { if (lineHeight != null) it.copy(lineHeight = lineHeight.sp) else it }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(align = Alignment.Top)
            .onSizeChanged { size ->
                if (size.width > 0) textWidthPx = size.width
            },
        contentAlignment = Alignment.Center,
    ) {
        if (textWidthPx == 0) {
            // First pass — render to measure width via onSizeChanged.
            Text(text = text, style = style)
            return@Box
        }

        // ── Layer A: BASE TEXT (always visible, full color) ──────────────────
        Text(
            text = text,
            style = style,
        )

        // ── Layer B: GLOW TEXT (sweeps left→right) ────────────────────────────
        // Glow band center goes from -glowWidth/2 (off-screen left) to
        // textWidth + glowWidth/2 (off-screen right).
        val glowWidthPx = textWidthPx * glowWidthFraction
        val glowCenterFraction = (-glowWidthPx / 2f / textWidthPx) +
            (1f + glowWidthPx / textWidthPx) * glowProgress

        // Brush color stops — soft-edged glow band: transparent → glowColor → transparent.
        // We pass explicit start/end pixel positions so the gradient covers ONLY the
        // glow band. Default Clamp tile mode stretches the edge colors (Transparent)
        // to the rest of the text, keeping the glow localized.
        //
        // Using linearGradient (not horizontalGradient) because horizontalGradient
        // takes startX: Float / endX: Float, but we want full control over start/end
        // Offset (matches both x and y axes).
        val halfGlow = glowWidthFraction / 2f
        val glowBrush = Brush.linearGradient(
            colors = listOf(
                Color.Transparent,        // left edge of glow band
                glowColor,                 // center of glow (brightest)
                Color.Transparent,         // right edge of glow band
            ),
            start = androidx.compose.ui.geometry.Offset(
                x = (glowCenterFraction - halfGlow) * textWidthPx,
                y = 0f,
            ),
            end = androidx.compose.ui.geometry.Offset(
                x = (glowCenterFraction + halfGlow) * textWidthPx,
                y = 0f,
            ),
        )

        Text(
            text = text,
            style = style.copy(color = Color.White),  // any color works; brush overrides
            modifier = Modifier
                .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                .drawWithContent {
                    // Draw the text first (in white, on this offscreen layer)
                    drawContent()
                    // Then mask the layer's alpha with the glow brush:
                    //   BlendMode.DstIn keeps destination (text) where source (brush)
                    //   is opaque, erases destination where source is transparent.
                    drawRect(
                        brush = glowBrush,
                        blendMode = BlendMode.DstIn,
                    )
                },
        )
    }
}
