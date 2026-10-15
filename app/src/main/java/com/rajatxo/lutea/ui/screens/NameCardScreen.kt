package com.rajatxo.lutea.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rajatxo.lutea.R
import com.rajatxo.lutea.ui.components.TypingBlurText
import com.rajatxo.lutea.ui.foundation.LuteaBackground
import kotlinx.coroutines.delay

/**
 * NameCardScreen — the first content screen of Lutea.
 *
 * Recreated from the Lutea-Inspiration video (Record_2026-10-15-19-45-47.mp4) which
 * shows the Opal app's name-input screen with its signature blur-in text reveal.
 *
 * Layout (top-to-bottom, vertically centered in the safe area above the keyboard):
 *   1. Lotus logo (top-center, ~15% from top, 120dp)
 *   2. Primary text: "I'll help you find your sound."
 *      → BlurRevealText with delay 200ms, duration 900ms, blur 20dp
 *   3. Secondary text: "First, what's your name?"
 *      → BlurRevealText with delay 1100ms (waits for line 1 to settle), 18dp blur
 *   4. Pill input field — dark fill #1C1C1E, 56dp tall, 28dp radius
 *      → Slides in + fades in AFTER the secondary text reveals (delay ~2000ms)
 *   5. Continue button — full-width pill
 *      → Disabled (#2C2C2E bg + #8E8E93 text) when name is blank
 *      → Enabled (#D4A03A ginkgo-gold bg + black text) when name is filled
 *
 * Universe-fitting rule:
 *   Uses LuteaBackground (teal-tinted gradient + radial glow) instead of pure black,
 *   so this screen feels continuous with the rest of the Lutea app.
 *
 * Keyboard behavior:
 *   `Modifier.imePadding()` shifts the entire Column up when the keyboard appears.
 *   This recreates the inspiration video's "everything slides up" behavior — the
 *   logo moves toward the top edge, the button sits just above the keyboard.
 */
@Composable
fun NameCardScreen(
    onComplete: (displayName: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LuteaBackground(modifier = modifier) {
        NameCardContent(onComplete = onComplete)
    }
}

@Composable
private fun NameCardContent(
    onComplete: (displayName: String) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    val keyboard = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }

    // Show input + button after the typing animations settle:
    // line 1: delay 200ms + 1500ms type = ends ~1700ms
    // line 2: delay 1900ms + 1100ms type = ends ~3000ms
    // give ~500ms buffer for the eye to settle → ~3500ms
    var showForm by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(3500)
        showForm = true
    }

    // After the form appears, auto-focus the input so the keyboard slides up
    // (matches the inspiration video where keyboard appears right after input shows).
    LaunchedEffect(showForm) {
        if (showForm) {
            delay(300)
            focusRequester.requestFocus()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .imePadding(),  // ← shifts everything up when keyboard appears
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(
                    top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 56.dp,
                    bottom = WindowInsets.navigationBars.asPaddingValues()
                        .calculateBottomPadding() + 24.dp,
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // ── 1. Lotus logo — top-center, 120dp ──────────────────────────
            Image(
                painter = painterResource(R.drawable.ic_launcher_foreground),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(120.dp),
            )
            Spacer(Modifier.height(48.dp))

            // ── 2. Primary text — "I'll help you find your sound." ────────
            // Typing-blur reveal: text types fast letter-by-letter, last 2 chars
            // are blurred (blur follows the cursor as it moves right).
            TypingBlurText(
                text = "I'll help you find your sound.",
                color = Color.White,
                fontSize = 24,
                fontWeight = FontWeight.Normal,
                letterSpacing = 0f,
                delayMillis = 200,
                durationMillis = 1500,
                blurTrailChars = 2,
                blurRadiusDp = 8.dp,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(28.dp))

            // ── 3. Secondary text — "First, what's your name?" ────────────
            TypingBlurText(
                text = "First, what's your name?",
                color = Color.White,
                fontSize = 20,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0f,
                delayMillis = 1900,  // wait for line 1's typing to finish (200 + 1500 = 1700ms)
                durationMillis = 1100,
                blurTrailChars = 2,
                blurRadiusDp = 8.dp,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(40.dp))

            // ── 4. Input field + 5. Continue button — slide in after 2s ───
            AnimatedVisibility(
                visible = showForm,
                enter = fadeIn(tween(400)) +
                    slideInVertically(
                        initialOffsetY = { it / 4 },
                        animationSpec = tween(400),
                    ),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    PillTextField(
                        value = name,
                        onValueChange = { name = it.take(40) },
                        placeholder = "Your name",
                        focusRequester = focusRequester,
                        imeAction = ImeAction.Done,
                        onImeAction = { keyboard?.hide() },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(20.dp))

                    val canContinue = name.isNotBlank()
                    ContinueButton(
                        enabled = canContinue,
                        onClick = { onComplete(name.ifBlank { "Friend" }) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

// ────────────────────────────────────────────────────────────────────────────────────
// Reusable bits
// ────────────────────────────────────────────────────────────────────────────────────

/** Pill-shaped text input — dark fill #1C1C1E, no border, 28dp radius. */
@Composable
private fun PillTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    focusRequester: FocusRequester,
    imeAction: ImeAction,
    onImeAction: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .height(56.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(Color(0xFF1C1C1E)),
        contentAlignment = Alignment.CenterStart,
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            cursorBrush = SolidColor(Color(0xFFD4A03A)),  // ginkgo-gold cursor
            textStyle = TextStyle(
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Normal,
            ),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = imeAction,
                keyboardType = KeyboardType.Text,
            ),
            keyboardActions = KeyboardActions(onDone = { onImeAction() }),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester)
                .padding(horizontal = 22.dp),
            decorationBox = { inner ->
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        color = Color(0xFF6E6E73),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Normal,
                    )
                }
                inner()
            },
        )
    }
}

/** Full-width pill Continue button.
 *  Disabled = #2C2C2E bg + #8E8E93 text (matches inspiration video).
 *  Enabled  = #D4A03A ginkgo-gold bg + black text (Lutea brand accent). */
@Composable
private fun ContinueButton(
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale = if (pressed && enabled) 0.98f else 1f
    val bg = if (enabled) Color(0xFFD4A03A) else Color(0xFF2C2C2E)
    val fg = if (enabled) Color.Black else Color(0xFF8E8E93)

    Box(
        modifier = modifier
            .height(56.dp)
            .scale(scale)
            .clip(RoundedCornerShape(28.dp))
            .background(bg)
            .clickable(
                enabled = enabled,
                interactionSource = interaction,
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "Continue",
            color = fg,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}
