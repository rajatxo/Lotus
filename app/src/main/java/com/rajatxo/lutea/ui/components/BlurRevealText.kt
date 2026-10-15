package com.rajatxo.lutea.ui.components

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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
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
 * Original codrops code (adapted from scroll-driven to time-driven):
 *   gsap.fromTo(chars, {
 *       filter: 'blur(10px) brightness(0%)',
 *       willChange: 'filter'
 *   }, {
 *       ease: 'none',
 *       filter: 'blur(0px) brightness(100%)',
 *       stagger: 0.05,  // 50ms per-character delay
 *       scrollTrigger: { scrub: true, ... }  // we replace this with animateFloatAsState
 *   });
 *
 * Behavior:
 *   - ALL characters are in the layout from the start (just alpha=0 + blur=10dp)
 *   - Each character animates independently:
 *       char[0]: delay 0ms      + 600ms anim
 *       char[1]: delay 50ms     + 600ms anim
 *       char[2]: delay 100ms    + 600ms anim
 *       ...
 *   - Each char's animation: blur 10dp → 0dp AND alpha 0 → 1
 *   - The 'blur cloud' emerges naturally as the wave of mid-transition chars
 *     sweeps left→right (typically 2-3 chars mid-blur at any moment)
 *
 * Why this fixes the previous problems:
 *   1. Cal Sans font — applied via CalSansFamily
 *   2. Text shifts left — FIXED. Row layout contains ALL chars from t=0, so its
 *      width never changes as the animation progresses. No re-centering jitter.
 *   3. Square/hard blur — FIXED. Each char is its own Text with Modifier.blur,
 *      so the blur has soft edges naturally (Modifier.blur uses a Gaussian blur
 *      in hardware, not a hard square mask).
 *
 * @param text           the full text to reveal
 * @param delayMillis    global delay before the whole animation starts
 * @param perCharDurationMillis  how long each character's blur→sharp takes
 * @param staggerMillis  delay between consecutive characters' starts (50ms per codrops)
 * @param maxBlurDp      starting blur radius (10dp per codrops)
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

    val style = TextStyle(
        color = color,
        fontSize = fontSize.sp,
        fontFamily = CalSansFamily,  // ← Cal Sans per user request
        fontWeight = fontWeight,
        letterSpacing = letterSpacing.sp,
        textAlign = textAlign,
    ).let { if (lineHeight != null) it.copy(lineHeight = lineHeight.sp) else it }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentSize(align = Alignment.Center),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
    ) {
        text.forEachIndexed { index, char ->
            // Each character has its own staggered start delay.
            // char[0] starts at delayMillis + 0 * staggerMillis
            // char[1] starts at delayMillis + 1 * staggerMillis
            // etc.
            val charStartDelay = delayMillis + index * staggerMillis
            var charStarted by remember(text, index) { mutableStateOf(false) }
            LaunchedEffect(text, index) {
                if (charStartDelay > 0) delay(charStartDelay.toLong())
                charStarted = true
            }

            val progress by animateFloatAsState(
                targetValue = if (charStarted) 1f else 0f,
                animationSpec = tween(perCharDurationMillis, easing = LinearEasing),
                label = "char_${index}_blur",
            )

            // Per-character blur radius: maxBlurDp → 0dp
            val blurRadius = maxBlurDp * (1f - progress)
            // Per-character alpha (codrops' 'brightness 0% → 100%'): 0 → 1
            val charAlpha = progress

            Text(
                text = char.toString(),
                style = style,
                modifier = Modifier
                    .blur(blurRadius)
                    .alpha(charAlpha),
            )
        }
    }
}
