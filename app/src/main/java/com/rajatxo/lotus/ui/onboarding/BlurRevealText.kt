package com.rajatxo.lotus.ui.onboarding

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/**
 * Cinematic blur-in text reveal — the same effect used in the Opal app's onboarding:
 * the text starts heavily blurred + invisible, then animates to sharp + opaque over
 * [durationMillis] with a slight scale settle (1.04 → 1.0) so it feels like the text
 * is focusing into view through a camera lens.
 *
 * Three properties animate together, driven by a single progress Float 0 → 1:
 *   alpha : 0   → 1       (text materialises)
 *   blur  : 20dp → 0dp     (focus locks in)
 *   scale : 1.04 → 1.0     (settle from "too close")
 *
 * Note on `Modifier.blur`: hardware-accelerated on Android 12+ (API 31+). On older
 * devices, blur is silently a no-op so the text just fades in without the focus
 * effect — graceful degradation.
 *
 * @param text the text to display
 * @param delayMillis how long to wait before starting the reveal (for staggered sequencing)
 * @param durationMillis how long the blur-to-sharp transition takes
 * @param maxBlurDp starting blur radius — 20dp feels cinematic; lower for subtle effect
 */
@Composable
fun BlurRevealText(
    text: String,
    modifier: Modifier = Modifier,
    delayMillis: Int = 0,
    durationMillis: Int = 1200,
    maxBlurDp: Dp = 20.dp,
    color: Color = Color.White,
    fontSize: Int = 28,
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

    val progress by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = tween(durationMillis, easing = LinearEasing),
        label = "blur_reveal",
    )

    val currentBlur = maxBlurDp * (1f - progress)
    val currentAlpha = progress
    val currentScale = 1.04f - 0.04f * progress

    val style = TextStyle(
        color = color,
        fontSize = fontSize.sp,
        fontWeight = fontWeight,
        letterSpacing = letterSpacing.sp,
        textAlign = textAlign,
    ).let { if (lineHeight != null) it.copy(lineHeight = lineHeight.sp) else it }

    Text(
        text = text,
        style = style,
        modifier = modifier
            .blur(currentBlur)
            .alpha(currentAlpha)
            .scale(currentScale),
    )
}
