package com.rajatxo.lotus.ui.onboarding

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Cinematic lotus flower drawn on a Compose Canvas.
 *
 * The lotus has 5 outer petals arranged radially. The [bloom] value (0f → 1f) drives
 * the open/close: at 0 the petals hug the center forming a closed bud; at 1 they fan
 * outward into a full bloom with a luminous radial glow.
 *
 * A subtle breathing pulse (scales the entire flower 1.0 → 1.025 → 1.0) runs on an
 * infinite transition so the lotus never feels static — same meditative cadence as the
 * Opal app's 3D gemstone in the inspiration video.
 *
 * @param bloom       0f = closed bud, 1f = fully bloomed.
 * @param showGlow    when true, paints a soft radial halo behind the petals. Used during
 *                    the Bloom and Welcome stages for the "glow from within" look.
 * @param sizeDp      overall canvas size.
 * @param petalColor  petal fill color.
 * @param glowColor   halo color (usually a tinted variant of petalColor).
 */
@Composable
fun LotusBloom(
    bloom: Float,
    showGlow: Boolean,
    modifier: Modifier = Modifier,
    sizeDp: Dp = 240.dp,
    petalColor: Color = Color(0xFFF5F5F5),
    glowColor: Color = Color(0xFF80E5FF),
) {
    // ★ Breathing pulse — 4.2s cycle, very subtle
    val infiniteTransition = rememberInfiniteTransition(label = "lotus_breath")
    val breath by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "breath",
    )

    val easedBloom by animateFloatAsState(
        targetValue = bloom,
        animationSpec = tween(durationMillis = 1200, easing = LinearEasing),
        label = "bloom_eased",
    )

    // Breathing adds up to ±0.025 scale around the eased bloom.
    val breathScale = 1f + 0.025f * sin(breath * PI).toFloat()

    Canvas(
        modifier = modifier
            .size(sizeDp)
            .alpha(1f),
    ) {
        val w = this.size.width
        val h = this.size.height
        val cx = w / 2f
        val cy = h / 2f
        val maxRadius = minOf(w, h) / 2f * 0.92f

        // Outer halo
        if (showGlow) {
            drawRadialGlow(
                center = Offset(cx, cy),
                radius = maxRadius * 1.6f,
                color = glowColor.copy(alpha = 0.55f * (0.6f + 0.4f * easedBloom)),
            )
        }

        // Apply breathing scale by scaling all the coordinates below.
        val scale = breathScale
        val center = Offset(cx, cy)

        // ── Draw 5 outer petals ──────────────────────────────────────────────
        // Petal angle: at bloom=0 → petals hug vertical (closed bud, angle = ~12°).
        //               at bloom=1 → petals fan to ~75° (full bloom).
        val petalCount = 5
        val baseAngle = (12f + (75f - 12f) * easedBloom) // degrees from vertical
        val petalLength = maxRadius * (0.85f + 0.05f * easedBloom)
        val petalWidth = maxRadius * (0.32f + 0.06f * easedBloom)

        // Start from top (−90°) and go around clockwise.
        for (i in 0 until petalCount) {
            val theta = (-90f + (360f / petalCount) * i)
            drawPetal(
                center = center,
                thetaDeg = theta,
                openDeg = baseAngle,
                length = petalLength * scale,
                width = petalWidth * scale,
                color = petalColor,
                bloomProgress = easedBloom,
            )
        }

        // ── Inner 5 small petals (only visible past bloom=0.3) ───────────────
        val innerAlpha = ((easedBloom - 0.25f) / 0.75f).coerceIn(0f, 1f)
        if (innerAlpha > 0.01f) {
            val innerBaseAngle = 8f + (55f - 8f) * easedBloom
            val innerLength = maxRadius * 0.55f * scale
            val innerWidth = maxRadius * 0.22f * scale
            for (i in 0 until petalCount) {
                val theta = (-90f + (360f / petalCount) * i + (360f / petalCount) / 2f)
                drawPetal(
                    center = center,
                    thetaDeg = theta,
                    openDeg = innerBaseAngle,
                    length = innerLength,
                    width = innerWidth,
                    color = petalColor.copy(alpha = 0.92f * innerAlpha),
                    bloomProgress = easedBloom,
                )
            }
        }

        // ── Center pistil dot — appears as bloom opens ────────────────────────
        if (easedBloom > 0.5f) {
            val pistilAlpha = ((easedBloom - 0.5f) / 0.5f).coerceIn(0f, 1f)
            val pistilRadius = maxRadius * 0.06f * pistilAlpha
            drawCircle(
                color = glowColor.copy(alpha = 0.9f * pistilAlpha),
                radius = pistilRadius,
                center = center,
            )
        }
    }
}

/**
 * Draws a single lotus petal — a rounded teardrop/leaf shape rotated to [thetaDeg] from
 * the +x axis (so −90 = straight up). [openDeg] controls how far the petal tips away
 * from vertical — 0° = petal points straight up (closed bud), 90° = petal lies flat
 * (full bloom).
 */
private fun DrawScope.drawPetal(
    center: Offset,
    thetaDeg: Float,
    openDeg: Float,
    length: Float,
    width: Float,
    color: Color,
    bloomProgress: Float,
) {
    val thetaRad = Math.toRadians(thetaDeg.toDouble())
    val openRad = Math.toRadians(openDeg.toDouble())

    // Direction the petal tip points in (rotated by openDeg away from vertical).
    val tipDx = sin(thetaRad + openRad).toFloat()
    val tipDy = -cos(thetaRad + openRad).toFloat()

    val tipX = center.x + tipDx * length
    val tipY = center.y + tipDy * length

    // Two shoulder control points perpendicular to the petal axis.
    val perpDx = -tipDy
    val perpDy = tipDx
    val shoulder = width * 0.5f
    val shoulderAnchorT = 0.45f // shoulder sits at 45% along the petal length

    val s1x = center.x + tipDx * length * shoulderAnchorT + perpDx * shoulder
    val s1y = center.y + tipDy * length * shoulderAnchorT + perpDy * shoulder
    val s2x = center.x + tipDx * length * shoulderAnchorT - perpDx * shoulder
    val s2y = center.y + tipDy * length * shoulderAnchorT - perpDy * shoulder

    // Base anchor — slightly inside the petal cluster center so petals join cleanly.
    val baseInset = length * 0.08f
    val bx = center.x - tipDx * baseInset
    val by = center.y - tipDy * baseInset

    val path = Path().apply {
        moveTo(bx, by)
        cubicTo(s1x, s1y, tipX + perpDx * width * 0.18f, tipY + perpDy * width * 0.18f, tipX, tipY)
        cubicTo(tipX - perpDx * width * 0.18f, tipY - perpDy * width * 0.18f, s2x, s2y, bx, by)
        close()
    }

    drawPath(
        path = path,
        color = color,
    )

    // Subtle seam highlight down the middle of each petal — gives the "petal vein" feel.
    if (bloomProgress > 0.4f) {
        val seamAlpha = ((bloomProgress - 0.4f) / 0.6f).coerceIn(0f, 1f) * 0.3f
        drawPath(
            path = Path().apply {
                moveTo(bx, by)
                lineTo(tipX, tipY)
            },
            color = Color.Black.copy(alpha = seamAlpha),
            style = Stroke(width = 1.2f),
        )
    }
}

/** Soft radial halo behind the lotus — implemented as a multi-stop radial gradient. */
private fun DrawScope.drawRadialGlow(
    center: Offset,
    radius: Float,
    color: Color,
) {
    val brush = Brush.radialGradient(
        colors = listOf(
            color,
            color.copy(alpha = color.alpha * 0.4f),
            color.copy(alpha = 0f),
        ),
        center = center,
        radius = radius,
        tileMode = TileMode.Clamp,
    )
    drawRect(brush = brush, size = this.size)
}
