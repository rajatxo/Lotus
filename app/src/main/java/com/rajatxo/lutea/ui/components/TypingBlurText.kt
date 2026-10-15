package com.rajatxo.lutea.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/**
 * Typing-blur text — the text types out letter-by-letter FAST. The currently-being-typed
 * letter is BLURRED; once a letter is "passed" it becomes sharp. The blur effectively
 * follows the cursor as it sweeps left→right across the text.
 *
 * Recreated from Lutea-Inspiration video ultra-fine analysis (Record_2026-10-15-19-45-47.mp4,
 * frames at 0.1s intervals). Observed:
 *   t=3.0s: 'I' (blurred)
 *   t=3.1s: 'I'' (blurred)
 *   t=3.2s: 'I'll' (sharp) + 'h' (blurred)        ← 'I'll' is done, 'h' is the new letter
 *   t=3.4s: 'I'll help' (sharp) + 'p' (blurred)   ← 'I'll help' is done, 'p' is new
 *   t=3.8s: 'I'll help protect' (sharp) + 'y' (blurred)
 *   t=4.0s: 'I'll help protect your focus' (sharp) + '.' (blurred)
 *   t=4.1s: 'I'll help protect your focus.' (sharp, fully revealed)
 *
 * Behavior:
 *   - The text content rendered = substring(0, revealedCount) — grows over time
 *   - That substring is split into TWO parts:
 *       SHARP PART: substring(0, revealedCount - blurTrailChars)
 *       BLURRED PART: substring(revealedCount - blurTrailChars, revealedCount)
 *   - Both parts are rendered in the same Row, sharp part normal, blurred part with
 *     Modifier.blur(blurRadius).
 *   - revealedCount animates 0 → text.length over [durationMillis] (~1.5s).
 *   - blurTrailChars defaults to 2 — the last 2 chars being typed are blurred.
 *
 * @param text           the full text to reveal
 * @param delayMillis    how long to wait before starting the typing animation
 * @param durationMillis how long the full left-to-right type-out takes
 * @param blurTrailChars how many of the latest characters are blurred
 * @param blurRadiusDp   blur radius applied to the trailing characters (8dp matches inspiration)
 */
@Composable
fun TypingBlurText(
    text: String,
    modifier: Modifier = Modifier,
    delayMillis: Int = 0,
    durationMillis: Int = 1500,
    blurTrailChars: Int = 2,
    blurRadiusDp: androidx.compose.ui.unit.Dp = 8.dp,
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

    // Animate the revealed character count from 0 → text.length
    val targetCount = if (started) text.length else 0
    val revealedCount by animateIntAsState(
        targetValue = targetCount,
        animationSpec = tween(durationMillis, easing = LinearEasing),
        label = "typing_progress",
    )

    val style = TextStyle(
        color = color,
        fontSize = fontSize.sp,
        fontWeight = fontWeight,
        letterSpacing = letterSpacing.sp,
        textAlign = textAlign,
    ).let { if (lineHeight != null) it.copy(lineHeight = lineHeight.sp) else it }

    // Split the revealed substring into sharp + blurred parts.
    val sharpEnd = (revealedCount - blurTrailChars).coerceAtLeast(0)
    val sharpPart = text.substring(0, sharpEnd.coerceAtMost(text.length))
    val blurredPart = if (revealedCount > sharpEnd) {
        text.substring(sharpEnd, revealedCount.coerceAtMost(text.length))
    } else ""

    // Render both parts in a single Text using an AnnotatedString would be ideal,
    // but Modifier.blur can only apply to the whole composable. So we render TWO
    // Text composables side-by-side inside a Row — sharp part on the left (no blur),
    // blurred part on the right (with blur). This naturally keeps the blur at the
    // "cursor" position (the end of the typed text).
    androidx.compose.foundation.layout.Row(
        modifier = modifier.wrapContentSize(),
        verticalAlignment = androidx.compose.ui.Alignment.Top,
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Start,
    ) {
        if (sharpPart.isNotEmpty()) {
            Text(
                text = sharpPart,
                style = style,
            )
        }
        if (blurredPart.isNotEmpty()) {
            Text(
                text = blurredPart,
                style = style,
                modifier = Modifier.blur(blurRadiusDp),
            )
        }
    }
}
