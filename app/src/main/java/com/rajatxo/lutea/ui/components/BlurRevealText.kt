package com.rajatxo.lutea.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/**
 * Blur-reveal text — the whole text element animates from blurred (16dp) to sharp (0dp)
 * over [durationMillis]. Auto-triggers after [delayMillis] so multiple lines can stagger.
 *
 * Pattern follows Gemini's reference implementation:
 *   val blurRadius by animateDpAsState(
 *       targetValue = if (isRevealed) 0.dp else 16.dp,
 *       animationSpec = tween(500),
 *   )
 *   Box(modifier = Modifier.blur(blurRadius)) { Text(...) }
 *
 * Differences from Gemini's tap-to-flip demo:
 *   - Auto-triggers on first composition (no tap required)
 *   - Accepts delayMillis so multiple lines can stagger their reveals
 *   - Takes text-styling parameters so it can be reused for headlines, subheads, etc.
 *
 * @param text           the text to display
 * @param delayMillis    how long to wait before starting the blur reveal
 * @param durationMillis how long the blur-to-sharp transition takes (Gemini used 500ms)
 * @param maxBlurDp      starting blur radius (Gemini used 16dp)
 */
@Composable
fun BlurRevealText(
    text: String,
    modifier: Modifier = Modifier,
    delayMillis: Int = 0,
    durationMillis: Int = 500,
    maxBlurDp: Dp = 16.dp,
    color: Color = Color.White,
    fontSize: Int = 24,
    fontWeight: FontWeight = FontWeight.Normal,
    letterSpacing: Float = 0f,
    textAlign: TextAlign = TextAlign.Center,
    lineHeight: Int? = null,
) {
    // Tracks whether the content is revealed (true) or hidden/blurred (false)
    var isRevealed by remember { mutableStateOf(false) }

    // Auto-trigger after delayMillis
    LaunchedEffect(text) {
        if (delayMillis > 0) delay(delayMillis.toLong())
        isRevealed = true
    }

    // Smoothly animates the blur from maxBlurDp (blurry) down to 0.dp (clear)
    val blurRadius by animateDpAsState(
        targetValue = if (isRevealed) 0.dp else maxBlurDp,
        animationSpec = tween(durationMillis = durationMillis),
        label = "blurRevealAnimation",
    )

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
        modifier = modifier.blur(blurRadius),
    )
}
