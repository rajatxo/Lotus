package com.rajatxo.lutea.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rajatxo.lutea.R
import com.rajatxo.lutea.ui.components.AnimatedLuteaLogo
import com.rajatxo.lutea.ui.components.BlurRevealText
import com.rajatxo.lutea.ui.foundation.LuteaBackground
import kotlinx.coroutines.delay

/**
 * NameCardScreen — the first content screen of Lutea.
 *
 * Reveal sequence (per Lutea-Inspiration Opal video analysis):
 *
 *   1. Logo (top-center, 120dp) — visible from t=0
 *
 *   2. Sentence 1 reveals at the LOWER position (where Sentence 2 will eventually sit).
 *      → BlurRevealText, delay 200ms, perChar 300ms, stagger 30ms (finishes ~1430ms)
 *
 *   3. Sentence 1 MOVES UP to its final upper position.
 *      → animateDpAsState on the bottom padding, spring animation, ~500ms
 *
 *   4. Sentence 2 reveals in the now-vacated lower position.
 *      → BlurRevealText, starts after Sentence 1 finishes moving up
 *
 *   5. Continue button appears MID-WAY through Sentence 2's reveal (~50% progress).
 *      → fadeIn + slideIn from below
 *
 *   6. Name capsule appears AFTER Sentence 2 fully reveals.
 *      → fadeIn + slideIn from below — positioned in the SAME Column as the Continue
 *        button with a Spacer between them, so they never overlap.
 *
 *   7. User taps capsule → keyboard opens → ALL elements shift UP together as ONE unit
 *      via Modifier.imePadding() on the root Box.
 *
 *   8. User taps back → keyboard closes → ALL elements shift DOWN together.
 *
 * Universe-fitting rule:
 *   Uses LuteaBackground (teal-tinted gradient + radial glow).
 *
 * Layout structure:
 *   Root Box (imePadding — moves everything as one unit)
 *     └─ Column (TopCenter)
 *         ├─ Logo (120dp)
 *         ├─ Spacer
 *         ├─ Sentence 1 (animates from lower position to upper position)
 *         ├─ Sentence 2 (reveals in the lower position)
 *         ├─ Spacer
 *         ├─ Name capsule (appears after sentence 2)
 *         ├─ Spacer (16dp)
 *         └─ Continue button (appears mid-way through sentence 2)
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
    val scrollState = rememberScrollState()  // hoisted so LaunchedEffect can use it

    // ── Animation state flags ──────────────────────────────────────────────
    // Sentence 1 starts at LOWER position. Once it finishes revealing, set
    // sentence1MovedUp = true → triggers animateDpAsState to move it UP.
    var sentence1MovedUp by remember { mutableStateOf(false) }

    // Continue button appears mid-way through sentence 2's reveal.
    var showContinueButton by remember { mutableStateOf(false) }

    // Name capsule appears AFTER sentence 2 fully reveals.
    var showCapsule by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        // Sentence 1 starts revealing at 200ms, finishes at ~1430ms.
        // Wait for it to finish, then trigger the move-up animation.
        delay(1500)  // 200ms delay + ~1300ms reveal
        sentence1MovedUp = true

        // Sentence 2 starts revealing simultaneously with sentence 1 finishing move-up.
        // Sentence 1 move-up takes ~600ms, so sentence 2 starts at ~2100ms.
        delay(600)

        // Continue button appears SIMULTANEOUSLY when sentence 2 starts revealing
        // (user feedback: "i want simentunesly when the second sentence show")
        showContinueButton = true

        // Sentence 2 reveal: 24 chars * 30ms stagger + 300ms = ~1020ms, finishes ~3120ms
        delay(1100)

        // Capsule appears after sentence 2 fully reveals.
        showCapsule = true
    }

    // After the capsule appears, auto-focus it so the keyboard slides up.
    LaunchedEffect(showCapsule) {
        if (showCapsule) {
            delay(300)
            focusRequester.requestFocus()
        }
    }

    // Sentence 1's Y offset animates from 17dp DOWN (sentence 2's position) → 0dp (natural position).
    // 17dp = exact gap between sentence 1 and sentence 2 (measured from inspiration video:
    // 50px at 3x density = 17dp). This is what sentence 1 travels through during its move-up.
    val sentence1YOffset: Dp by animateDpAsState(
        targetValue = if (sentence1MovedUp) 0.dp else 17.dp,
        animationSpec = tween(durationMillis = 600, easing = androidx.compose.animation.core.FastOutSlowInEasing),
        label = "sentence1_y",
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .imePadding(),  // ← shrinks the Box height when keyboard opens.
        contentAlignment = Alignment.TopCenter,
    ) {
        // ── Single scrollable Column containing ALL elements as ONE rigid block ──
        // Per inspiration video: logo, sentences, capsule, and button all move together
        // as one unit when keyboard opens/closes. They are NOT independent anchored boxes.
        //
        // Using verticalScroll + focusRequester auto-triggers Android's bringIntoView
        // mechanism: when the capsule gets focused (via focusRequester.requestFocus()),
        // the scroll automatically brings the capsule into view above the keyboard.
        // The whole Column shifts up — logo, sentences, capsule, button all move together.
        //
        // The Continue button is INSIDE this Column (not anchored separately to BottomCenter)
        // so it moves with everything else. The large gap between capsule and button is
        // achieved with a Spacer that fills remaining space via weight(1f).
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp)
                .padding(
                    top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 56.dp,
                    bottom = WindowInsets.navigationBars.asPaddingValues()
                        .calculateBottomPadding() + 24.dp,
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            // Top→bottom arrangement so content sits naturally at the top, with the
            // Continue button pushed down toward the bottom via a flexible-weight Spacer.
            verticalArrangement = Arrangement.Top,
        ) {
            // ── 1. Lotus logo — Lottie animation plays ONE TIME, then holds final frame ──
            // Fixed size (160dp) — no shrink animation when keyboard opens (per user:
            // 'why are you making the logo small? don't mess with logo..just increase the size').
            AnimatedLuteaLogo(
                sizeDp = 160.dp,
            )
            Spacer(Modifier.height(24.dp))  // 24dp gap between logo and sentence 1 (was 53dp — too big)

            // ── 2. Sentence 1 — "I'll help you find your sound." ───────────
            BlurRevealText(
                text = "I'll help you find your sound.",
                color = Color.White,
                fontSize = 22,
                fontWeight = FontWeight.Normal,
                letterSpacing = 0f,
                delayMillis = 200,
                perCharDurationMillis = 300,
                staggerMillis = 30,
                maxBlurDp = 10.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = sentence1YOffset),
            )

            // ── 3. Sentence 2 — "First, what's your name?" ─────────────────
            Spacer(Modifier.height(17.dp))
            BlurRevealText(
                text = "First, what's your name?",
                color = Color.White,
                fontSize = 27,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0f,
                delayMillis = 2100,
                perCharDurationMillis = 300,
                staggerMillis = 30,
                maxBlurDp = 10.dp,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(20.dp))  // 20dp gap between sentence 2 and capsule

            // ── 4. Name capsule ──────────────────────────────────────────────
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

            // ── Flexible gap between capsule and Continue button ─────────────
            // Per inspiration: 293dp gap (button sits at screen bottom, not under capsule).
            // Using Spacer with weight=1f fills ALL remaining vertical space, pushing the
            // Continue button to the bottom of the Column. When keyboard opens, imePadding
            // shrinks the Box height → Column's available height shrinks → weight spacer
            // shrinks → button moves UP closer to capsule (matches the video where gap
            // shrinks from 293dp to ~60dp when keyboard opens).
            Spacer(modifier = Modifier.weight(1f))

            // ── 5. Continue button — at the bottom of the same Column ─────────
            // Now part of the same scrollable block, moves together with everything else.
            AnimatedVisibility(
                visible = showContinueButton,
                enter = fadeIn(tween(400)) +
                    slideInVertically(
                        initialOffsetY = { it / 4 },
                        animationSpec = tween(400),
                    ),
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
            cursorBrush = SolidColor(Color(0xFFD4A03A)),
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
 *  Disabled = #2C2C2E bg + #8E8E93 text.
 *  Enabled  = #D4A03A ginkgo-gold bg + black text. */
@Composable
private fun ContinueButton(
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale = if (pressed && enabled) 0.98f else 1f
    val bg = if (enabled) Color(0xFFFFFFFF) else Color(0xFF3A3A3A)
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
