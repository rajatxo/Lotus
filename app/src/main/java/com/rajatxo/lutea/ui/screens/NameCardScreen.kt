package com.rajatxo.lutea.ui.screens

import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
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
    // sentence1Dismissing = true triggers S1's combined exit animation:
    // S1 moves UP + blurs out + fades out — ALL CONCURRENTLY (buttery smooth).
    var sentence1Dismissing by remember { mutableStateOf(false) }

    // Continue button appears mid-way through sentence 2's reveal.
    var showContinueButton by remember { mutableStateOf(false) }

    // Name capsule appears AFTER sentence 2 fully reveals.
    var showCapsule by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        // Sentence 1 reveals: 200ms delay + ~1300ms reveal = ~1500ms.
        delay(1500)

        // S1 starts dismissing CONCURRENTLY with S2 starting:
        // S1 moves up + blurs + fades, all at once. Smooth combined exit.
        sentence1Dismissing = true

        // S2 starts revealing at the same moment.
        // (S2's BlurRevealText has delayMillis = 1500 below)
        showContinueButton = true

        // S2 reveal: 24 chars * 30ms stagger + 300ms = ~1020ms, finishes ~2520ms.
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

    // ── S1's smoke-dissolve exit — blend into air and vanish ──
    // Per user: 'when it move it just dissolve..mean its blur spread away and expand..
    // just like a smoke blend into air..like it dissolve..blend and vanish and so smoothly'
    //
    // Mechanics (all CONCURRENT, perfectly synchronized):
    //   1. Y offset: 17dp (overlap S2) → -40dp (moves UP gently, not too far — smoke drifts)
    //   2. Scale: 1.0 → 1.3 (text EXPANDS outward in all directions like smoke spreading)
    //   3. Letter-spacing: 0 → 12sp (characters DRIFT APART as they dissolve)
    //   4. Blur: 0 → 32dp (heavy blur so the text loses all shape — becomes smoke)
    //   5. Alpha: 1 → 0 (fades to invisible as it spreads)
    //
    // All animations use the SAME 1500ms duration + LinearOutSlowInEasing for perfect sync.
    // LinearOutSlowInEasing: starts fast (smoke initial puff), slows to settle (gentle fade).
    // 1500ms is slow enough to feel like smoke, not a snap.
    val S1_EXIT_DURATION = 1500
    val S1_EXIT_EASING = androidx.compose.animation.core.LinearOutSlowInEasing

    // S1 vanish offset: starts at 0 (natural position, where the reveal left it),
    // moves UP to -40dp during the vanish. Gently drifts up like smoke.
    val sentence1YOffset: Dp by animateDpAsState(
        targetValue = if (sentence1Dismissing) (-40).dp else 0.dp,
        animationSpec = tween(durationMillis = S1_EXIT_DURATION, easing = S1_EXIT_EASING),
        label = "sentence1_y",
    )
    val sentence1Alpha by animateFloatAsState(
        targetValue = if (sentence1Dismissing) 0f else 1f,
        animationSpec = tween(durationMillis = S1_EXIT_DURATION, easing = S1_EXIT_EASING),
        label = "sentence1_alpha",
    )
    val sentence1BlurProgress by animateFloatAsState(
        targetValue = if (sentence1Dismissing) 1f else 0f,
        animationSpec = tween(durationMillis = S1_EXIT_DURATION, easing = S1_EXIT_EASING),
        label = "sentence1_blur_progress",
    )
    // Heavy blur (32dp max) so text loses all shape and becomes pure smoke.
    val sentence1BlurPx = with(LocalDensity.current) {
        (32.dp * sentence1BlurProgress).toPx()
    }
    // Scale up so the text expands outward (smoke spreading).
    val sentence1Scale by animateFloatAsState(
        targetValue = if (sentence1Dismissing) 1.3f else 1f,
        animationSpec = tween(durationMillis = S1_EXIT_DURATION, easing = S1_EXIT_EASING),
        label = "sentence1_scale",
    )
    // Letter-spacing grows so characters drift apart as they dissolve.
    val sentence1LetterSpacing by animateFloatAsState(
        targetValue = if (sentence1Dismissing) 12f else 0f,
        animationSpec = tween(durationMillis = S1_EXIT_DURATION, easing = S1_EXIT_EASING),
        label = "sentence1_letter_spacing",
    )

    // ── OUTER Box: NOT imePadded. Background image lives here, completely fixed. ──
    // The flower Image fills the ENTIRE screen and NEVER moves when keyboard opens.
    // The content Box (with imePadding) is a SIBLING — when it shrinks due to keyboard,
    // the background Image is unaffected because it's not a child of the imePadded Box.
    Box(modifier = Modifier.fillMaxSize()) {
        // ── Layer A: Flower image — TRULY FIXED BACKGROUND ──────────────────
        // In its own fillMaxSize Box. Keyboard opens/closes has ZERO effect on this.
        Image(
            painter = painterResource(R.drawable.flower_top),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )

        // ── Layer B: Content Box — FIXED position (NO imePadding) ─────────────
        // Content cluster stays EXACTLY where it is. Keyboard does NOT move it.
        // The centered cluster is high enough that the keyboard (which takes the bottom
        // ~40% of screen) doesn't cover the capsule — user can still see what they type.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .padding(
                    top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding(),
                    bottom = WindowInsets.navigationBars.asPaddingValues()
                        .calculateBottomPadding() + 24.dp,
                ),
            contentAlignment = Alignment.Center,  // ← center the cluster, FIXED
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,  // ← cluster centered
            ) {
                // ── Sentence 1 — "I'll help you find your sound." ───────────
                // Two stacked instances crossfaded:
                //   A) Reveal instance (reverse=false): chars appear blur→sharp at t=200ms
                //   B) Vanish instance (reverse=true): chars disappear sharp→blur at t=1500ms
                //      AND moves UP via offset animation while vanishing.
                // Same blur mechanic in both directions — the vanish is literally the
                // reveal played backward, per user request.
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    // A) Reveal instance — visible until S1 starts dismissing.
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
                        reverse = false,
                        modifier = Modifier
                            .fillMaxWidth()
                            .alpha(if (sentence1Dismissing) 0f else 1f),
                    )

                    // B) Vanish instance — same blur effect played backward + moves up.
                    // delayMillis=1500 so it starts vanishing exactly when S2 starts revealing.
                    BlurRevealText(
                        text = "I'll help you find your sound.",
                        color = Color.White,
                        fontSize = 22,
                        fontWeight = FontWeight.Normal,
                        letterSpacing = 0f,
                        delayMillis = 1500,
                        perCharDurationMillis = 400,
                        staggerMillis = 30,
                        maxBlurDp = 10.dp,
                        reverse = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .alpha(if (sentence1Dismissing) 1f else 0f)
                            .offset(y = sentence1YOffset),
                    )
                }

                // ── Sentence 2 — "First, what's your name?" ─────────────────
                // fontSize reduced from 27 → 24 (per user: 'make the second sentence
                // text size little bit smaller'). delayMillis = 1500 to match the new
                // S1-dismiss timing (S1 starts dismissing at 1500ms, S2 reveals at same time).
                Spacer(Modifier.height(17.dp))
                BlurRevealText(
                    text = "First, what's your name?",
                    color = Color.White,
                    fontSize = 24,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0f,
                    delayMillis = 1500,
                    perCharDurationMillis = 300,
                    staggerMillis = 30,
                    maxBlurDp = 10.dp,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(12.dp))  // gap between sentence 2 and capsule

                // ── Name capsule ──────────────────────────────────────────────
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

                Spacer(Modifier.height(16.dp))  // tiny gap between capsule and button

                // ── Continue button ──────────────────────────────────────────
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
            .height(48.dp)
            .clip(RoundedCornerShape(24.dp))
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
            .height(48.dp)
            .scale(scale)
            .clip(RoundedCornerShape(24.dp))
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
