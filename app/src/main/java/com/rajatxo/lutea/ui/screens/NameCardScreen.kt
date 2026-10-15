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
import com.rajatxo.lutea.ui.components.BlurRevealText
import com.rajatxo.lutea.ui.foundation.LuteaBackground
import kotlinx.coroutines.delay

/**
 * NameCardScreen — the first content screen of Lutea.
 *
 * Reveal sequence (per Lutea-Inspiration video analysis, frames_keyboard/ dir):
 *   1. Logo (top-center, 120dp) — visible from t=0
 *   2. Sentence 1: "I'll help you find your sound."
 *      → BlurRevealText with delay 200ms, perChar 300ms, stagger 30ms
 *        (finishes at ~1430ms)
 *   3. Continue button — slides in at the BOTTOM of the screen (~1500ms, right after
 *      sentence 1 finishes). Anchored to screen bottom via Box(BottomCenter).
 *   4. Sentence 2: "First, what's your name?"
 *      → BlurRevealText with delay 1700ms, finishes at ~2720ms
 *   5. Name capsule (pill input) — slides in below sentence 2 (~2900ms)
 *   6. User taps capsule → keyboard opens → ALL elements shift UP together as one unit
 *      (via Modifier.imePadding() on the root Box).
 *   7. User taps back to dismiss keyboard → ALL elements shift DOWN together.
 *
 * Universe-fitting rule:
 *   Uses LuteaBackground (teal-tinted gradient + radial glow) instead of pure black,
 *   so this screen feels continuous with the rest of the Lutea app.
 *
 * Keyboard behavior:
 *   Modifier.imePadding() on the root Box shifts the ENTIRE content (logo + sentences +
 *   capsule + button) up when keyboard appears, and back down when it disappears.
 *   Everything moves together as ONE unit — relative spacing preserved.
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

    // Timing (matches Lutea-Inspiration video analysis):
    //   sentence 1: delay 200ms + (31 chars * 30ms) + 300ms = ~1430ms
    //   continue button appears at ~1500ms (right after sentence 1)
    //   sentence 2: delay 1700ms + (24 chars * 30ms) + 300ms = ~2720ms
    //   capsule appears at ~2900ms (right after sentence 2)
    var showContinueButton by remember { mutableStateOf(false) }
    var showCapsule by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(1500)
        showContinueButton = true
        delay(1400)  // wait for sentence 2 to finish (~2900ms total)
        showCapsule = true
    }

    // After the capsule appears, auto-focus it so the keyboard slides up
    // (matches the inspiration video where keyboard appears right after capsule shows).
    LaunchedEffect(showCapsule) {
        if (showCapsule) {
            delay(300)
            focusRequester.requestFocus()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .imePadding(),  // ← shifts ENTIRE content (logo + sentences + capsule + button) up as one unit
    ) {
        // ── Top content: Logo + Sentence 1 + Sentence 2 + Capsule ────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(
                    top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 56.dp,
                )
                .align(Alignment.TopCenter),
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

            // ── 2. Sentence 1 — "I'll help you find your sound." ───────────
            BlurRevealText(
                text = "I'll help you find your sound.",
                color = Color.White,
                fontSize = 24,
                fontWeight = FontWeight.Normal,
                letterSpacing = 0f,
                delayMillis = 200,
                perCharDurationMillis = 300,
                staggerMillis = 30,
                maxBlurDp = 10.dp,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(28.dp))

            // ── 3. Sentence 2 — "First, what's your name?" ─────────────────
            BlurRevealText(
                text = "First, what's your name?",
                color = Color.White,
                fontSize = 20,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0f,
                delayMillis = 1700,
                perCharDurationMillis = 300,
                staggerMillis = 30,
                maxBlurDp = 10.dp,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(40.dp))

            // ── 4. Name capsule — slides in AFTER sentence 2 finishes ──────
            AnimatedVisibility(
                visible = showCapsule,
                enter = fadeIn(tween(400)) +
                    slideInVertically(
                        initialOffsetY = { it / 4 },
                        animationSpec = tween(400),
                    ),
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
            }
        }

        // ── Bottom: Continue button — appears BEFORE sentence 2 ──────────────
        // Anchored to the bottom of the screen, so it stays there until the
        // keyboard pushes everything up.
        AnimatedVisibility(
            visible = showContinueButton,
            enter = fadeIn(tween(400)) +
                slideInVertically(
                    initialOffsetY = { it / 4 },
                    animationSpec = tween(400),
                ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(
                    bottom = WindowInsets.navigationBars.asPaddingValues()
                        .calculateBottomPadding() + 24.dp,
                )
                .align(Alignment.BottomCenter),
        ) {
            val canContinue = name.isNotBlank()
            ContinueButton(
                enabled = canContinue,
                onClick = { onComplete(name.ifBlank { "Friend" }) },
                modifier = Modifier.fillMaxWidth(),
            )
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
