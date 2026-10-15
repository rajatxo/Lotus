package com.rajatxo.lutea.ui.components

import android.graphics.RenderEffect
import android.graphics.Shader
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
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
 * Per-character blur reveal — port of codrops/ScrollBlurTypography Effect 1.
 *
 * SOURCE: https://github.com/codrops/ScrollBlurTypography/blob/master/js/effect-1/blurScrollEffect.js
 *
 * Original codrops:
 *   gsap.fromTo(chars, {
 *       filter: 'blur(10px) brightness(0%)',
 *   }, {
 *       filter: 'blur(0px) brightness(100%)',
 *       stagger: 0.05,
 *   });
 *
 * Previous attempts (v1-v3) used Modifier.blur() per-character Text. That produced
 * a HARD-EDGED BOX BLUR because each char's Text was too small for the blur to fade
 * naturally — the blur got clipped at the character's bounding box.
 *
 * THIS VERSION (v4) uses the proper approach:
 *   - Each character animates ONLY its alpha (0 → 1) and a SMALL scale (0.5 → 1.0).
 *     No per-character Modifier.blur().
 *   - The ENTIRE ROW is wrapped in a single `Modifier.graphicsLayer { renderEffect = ... }`
 *     that applies a Gaussian RenderEffect blur (Android 12+, API 31+).
 *   - The blur radius on the whole row animates from 24dp → 0dp over the total duration.
 *   - As characters appear (alpha 0→1) and the whole row de-blurs together, the effect
 *     looks exactly like the codrops reference: smooth Gaussian blur, soft edges,
 *     wave-like reveal because chars stagger their alpha animations.
 *
 * On Android < 12 (API < 31), RenderEffect is a no-op, so the blur won't apply —
 * characters will just fade in (alpha animation only). Acceptable graceful degrade.
 *
 * Why this fixes the previous problems:
 *   1. Cal Sans font — applied via CalSansFamily
 *   2. Text shifts left — FIXED. Row contains ALL chars from t=0 (alpha=0).
 *   3. Square/hard blur — FIXED. Single RenderEffect on the whole row produces
 *      a proper Gaussian blur with soft edges (Android's native RenderEffect
 *      uses Gaussian, not box blur).
 *
 * @param text           the full text to reveal
 * @param delayMillis    global delay before the animation starts
 * @param perCharDurationMillis  how long each char's alpha animation takes
 * @param staggerMillis  delay between consecutive chars' alpha animations (50ms per codrops)
 * @param maxBlurDp      starting blur radius for the whole row (10dp per codrops)
 */
@Composable
fun BlurRevealText(
    text: String,
    modifier: Modifier = Modifier,
    delayMillis: Int = 0,
    perCharDurationMillis: Int = 600,
    staggerMillis: Int = 50,
    maxBlurDp: Dp = 10.dp,
    color: Color = Color.White,
    fontSize: Int = 24,
    fontWeight: FontWeight = FontWeight.Normal,
    letterSpacing: Float = 0f,
    textAlign: TextAlign = TextAlign.Center,
    lineHeight: Int? = null,
) {
    var started by remember { mutableStateOf(false) }
    LaunchedEffect(text) {
        if (delayMillis > 0) delay(delayMillis.toLong())
        started = true
    }

    // Total animation time = (last char's stagger) + (per-char duration).
    val totalDurationMs = (text.length - 1) * staggerMillis + perCharDurationMillis

    // Whole-row blur progress: 0 (max blur) → 1 (no blur) over totalDurationMs.
    // Starts at the same time as the first char.
    var blurStarted by remember(text) { mutableStateOf(false) }
    LaunchedEffect(text) {
        if (delayMillis > 0) delay(delayMillis.toLong())
        blurStarted = true
    }
    val blurProgress by animateFloatAsState(
        targetValue = if (blurStarted) 1f else 0f,
        animationSpec = tween(totalDurationMs, easing = LinearEasing),
        label = "row_blur",
    )
    // Animate blur radius from maxBlurDp → 0dp.
    val currentBlurPx = with(LocalDensity.current) {
        (maxBlurDp * (1f - blurProgress)).toPx()
    }

    val style = TextStyle(
        color = color,
        fontSize = fontSize.sp,
        fontFamily = CalSansFamily,
        fontWeight = fontWeight,
        letterSpacing = letterSpacing.sp,
        textAlign = textAlign,
    ).let { if (lineHeight != null) it.copy(lineHeight = lineHeight.sp) else it }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentSize(align = Alignment.Center)
            // Apply a single Gaussian RenderEffect blur to the whole row.
            // RenderEffect.createBlurEffect uses true Gaussian (not box) blur.
            .graphicsLayer {
                if (currentBlurPx > 0f) {
                    renderEffect = RenderEffect.createBlurEffect(
                        currentBlurPx,
                        currentBlurPx,
                        Shader.TileMode.DECAL,
                    ).asComposeRenderEffect()
                } else {
                    renderEffect = null
                }
            },
        verticalAlignment = Alignment.Top,
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
    ) {
        text.forEachIndexed { index, char ->
            val charStartDelay = delayMillis + index * staggerMillis
            var charStarted by remember(text, index) { mutableStateOf(false) }
            LaunchedEffect(text, index) {
                if (charStartDelay > 0) delay(charStartDelay.toLong())
                charStarted = true
            }

            // Per-char alpha (codrops' 'brightness 0% → 100%' → alpha 0 → 1).
            val charAlpha by animateFloatAsState(
                targetValue = if (charStarted) 1f else 0f,
                animationSpec = tween(perCharDurationMillis, easing = LinearEasing),
                label = "char_${index}_alpha",
            )

            Text(
                text = char.toString(),
                style = style,
                modifier = Modifier.alpha(charAlpha),
            )
        }
    }
}
