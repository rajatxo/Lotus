package com.rajatxo.lotus.ui.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/**
 * The cinematic onboarding experience — a 5-stage narrative inspired by the Opal app's
 * onboarding video.
 *
 * Stages:
 *   1. Splash  → 3s, black screen with breathing lotus bud, then auto-advances
 *   2. Brand   → 2.4s, "Lotus" wordmark reveal, then auto-advances
 *   3. Bud     → tap to continue, "Look closer.", bud at full hero size
 *   4. Bloom   → tap to continue, bud blooms into full petals with glow, "Let it bloom."
 *   5. Welcome → input form, "I'll help you find your sound." / "First, what's your name?"
 *                Continue button is disabled until name is non-empty.
 *
 * On finish calls [onComplete] with the trimmed display name (or "Friend" if blank).
 *
 * No progress dots. No step counter. The pacing is intentionally meditative.
 */
@Composable
fun OnboardingScreen(
    onComplete: (displayName: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var stage by remember { mutableStateOf(OnboardingStage.Splash) }
    var name by remember { mutableStateOf("") }

    // ★ Auto-advance Splash → Brand → Bud
    LaunchedEffect(Unit) {
        delay(3000)        // splash holds for 3 seconds
        stage = OnboardingStage.Brand
        delay(2400)        // brand holds for 2.4 seconds
        stage = OnboardingStage.Bud
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF000000)),
        contentAlignment = Alignment.Center,
    ) {
        AnimatedContent(
            targetState = stage,
            transitionSpec = {
                // Soft cross-fade (300ms in, 300ms out) — same cadence as the Opal app
                (fadeIn(tween(600)) + scaleIn(tween(600), initialScale = 0.96f)) togetherWith
                    (fadeOut(tween(400)) + scaleOut(tween(400), targetScale = 1.04f))
            },
            label = "stage_transition",
            contentKey = { it },
        ) { current ->
            when (current) {
                OnboardingStage.Splash -> SplashStage()
                OnboardingStage.Brand -> BrandStage()
                OnboardingStage.Bud -> BudStage(onTap = { stage = OnboardingStage.Bloom })
                OnboardingStage.Bloom -> BloomStage(onTap = { stage = OnboardingStage.Welcome })
                OnboardingStage.Welcome -> WelcomeStage(
                    name = name,
                    onNameChange = { name = it },
                    onBack = { stage = OnboardingStage.Bloom },
                    onContinue = { onComplete(name.ifBlank { "Friend" }) },
                )
            }
        }
    }
}

// ────────────────────────────────────────────────────────────────────────────────────
// Stage 1: Splash — single breathing lotus bud, pure black background
// ────────────────────────────────────────────────────────────────────────────────────

@Composable
private fun SplashStage() {
    // Breathing scale 0.97 → 1.0 → 0.97 on a 3.6s cycle.
    val infiniteTransition = rememberInfiniteTransition(label = "splash_breath")
    val breath by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "breath",
    )
    val breathScale = 0.97f + 0.03f * breath

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        LotusBloom(
            bloom = 0f,            // closed bud
            showGlow = false,
            sizeDp = 96.dp,
            modifier = Modifier.scale(breathScale),
        )
    }
}

// ────────────────────────────────────────────────────────────────────────────────────
// Stage 2: Brand — Lotus bud mark + "Lotus" wordmark on a dark textured-feeling background
// ────────────────────────────────────────────────────────────────────────────────────

@Composable
private fun BrandStage() {
    // Subtle radial vignette gives the "dark rock texture" feel without an asset.
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = androidx.compose.ui.graphics.Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF101010),
                        Color(0xFF000000),
                    ),
                    radius = 800f,
                )
            ),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            // Small bud mark, breathing
            val infiniteTransition = rememberInfiniteTransition(label = "brand_breath")
            val breath by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(3600, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse,
                ),
                label = "breath",
            )
            val breathScale = 0.98f + 0.02f * breath

            LotusBloom(
                bloom = 0f,
                showGlow = false,
                sizeDp = 64.dp,
                modifier = Modifier.scale(breathScale),
            )
            Spacer(Modifier.height(20.dp))
            // Wordmark reveals with a blur-focus after the bud mark appears
            BlurRevealText(
                text = "Lotus",
                color = Color.White,
                fontSize = 56,
                fontWeight = FontWeight.Medium,
                letterSpacing = -0.5f,
                delayMillis = 350,
                durationMillis = 1200,
                maxBlurDp = 24.dp,
                textAlign = TextAlign.Center,
            )
        }
    }
}

// ────────────────────────────────────────────────────────────────────────────────────
// Stage 3: Bud — "Look closer." + tap to continue
// ────────────────────────────────────────────────────────────────────────────────────

@Composable
private fun BudStage(onTap: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onTap,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 56.dp,
                    bottom = WindowInsets.navigationBars.asPaddingValues()
                        .calculateBottomPadding() + 56.dp,
                ),
        ) {
            // Header text — "Look closer." — top third, blurs into focus
            BlurRevealText(
                text = "Look closer.",
                color = Color.White,
                fontSize = 32,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                delayMillis = 150,
                durationMillis = 1100,
                maxBlurDp = 18.dp,
            )

            // Hero lotus bud — center, full size, no glow yet
            LotusBloom(
                bloom = 0f,
                showGlow = false,
                sizeDp = 280.dp,
            )

            // CTA — "TAP TO CONTINUE" — bottom, gray, uppercase, wide tracking
            PulsingTapToContinue()
        }
    }
}

// ────────────────────────────────────────────────────────────────────────────────────
// Stage 4: Bloom — "Let it bloom." + tap to continue + glow + bloom animation
// ────────────────────────────────────────────────────────────────────────────────────

@Composable
private fun BloomStage(onTap: () -> Unit) {
    // Drive the bloom from 0 → 1 over 1.6s on first composition of this stage.
    var targetBloom by remember { mutableStateOf(0f) }
    LaunchedEffect(Unit) { targetBloom = 1f }
    val bloom by animateFloatAsState(
        targetValue = targetBloom,
        animationSpec = tween(1600, easing = LinearEasing),
        label = "bloom",
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onTap,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 56.dp,
                    bottom = WindowInsets.navigationBars.asPaddingValues()
                        .calculateBottomPadding() + 56.dp,
                ),
        ) {
            BlurRevealText(
                text = "Let it bloom.",
                color = Color.White,
                fontSize = 32,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                delayMillis = 150,
                durationMillis = 1100,
                maxBlurDp = 18.dp,
            )

            LotusBloom(
                bloom = bloom,
                showGlow = true,
                sizeDp = 280.dp,
                glowColor = Color(0xFF80E5FF),
                petalColor = Color(0xFFF5F5F5),
            )

            PulsingTapToContinue()
        }
    }
}

// ────────────────────────────────────────────────────────────────────────────────────
// Stage 5: Welcome — conversational form, "I'll help you find your sound." / name input
// ────────────────────────────────────────────────────────────────────────────────────

@Composable
private fun WelcomeStage(
    name: String,
    onNameChange: (String) -> Unit,
    onBack: () -> Unit,
    onContinue: () -> Unit,
) {
    val keyboard = LocalSoftwareKeyboardController.current

    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        // Top-left circular back button
        BackButton(
            onClick = onBack,
            modifier = Modifier
                .padding(
                    start = 16.dp,
                    top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 8.dp,
                )
                .align(Alignment.TopStart),
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .padding(
                    top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 80.dp,
                    bottom = WindowInsets.navigationBars.asPaddingValues()
                        .calculateBottomPadding() + 32.dp,
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
        ) {
            // Small bloomed lotus, centered at top
            LotusBloom(
                bloom = 1f,
                showGlow = true,
                sizeDp = 96.dp,
                glowColor = Color(0xFF80E5FF),
                petalColor = Color(0xFFF5F5F5),
            )
            Spacer(Modifier.height(28.dp))

            BlurRevealText(
                text = "I'll help you find your sound.",
                color = Color.White,
                fontSize = 28,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Center,
                lineHeight = 36,
                delayMillis = 300,
                durationMillis = 1000,
                maxBlurDp = 16.dp,
            )
            Spacer(Modifier.height(40.dp))

            BlurRevealText(
                text = "First, what's your name?",
                color = Color.White,
                fontSize = 22,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                delayMillis = 1000,
                durationMillis = 900,
                maxBlurDp = 14.dp,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(20.dp))

            // Pill-shaped input — dark fill, no border, large touch target
            PillTextField(
                value = name,
                onValueChange = { onNameChange(it.take(40)) },
                placeholder = "Your name",
                imeAction = ImeAction.Done,
                onImeAction = { keyboard?.hide() },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(28.dp))

            // Continue button — disabled (gray) when name is blank, enabled (white-on-cyan) when filled
            val canContinue = name.isNotBlank()
            ContinueButton(
                enabled = canContinue,
                onClick = { onContinue() },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

// ────────────────────────────────────────────────────────────────────────────────────
// Reusable bits
// ────────────────────────────────────────────────────────────────────────────────────

/**
 * "TAP TO CONTINUE" — first reveals with a cinematic blur-in entrance (delayed so the
 * header reads first), then settles into a continuous pulsing alpha (0.55 ↔ 1.0, 1.8s cycle).
 */
@Composable
private fun PulsingTapToContinue() {
    // Entrance — blur-in, delayed so the header reveals first
    var started by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(550)
        started = true
    }
    val entrance by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = tween(900, easing = LinearEasing),
        label = "tap_entrance",
    )

    // Continuous pulse — alpha oscillates 0.55 ↔ 1.0
    val infiniteTransition = rememberInfiniteTransition(label = "tap_pulse")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.55f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "tap_alpha",
    )

    // Combine: blur fades out during entrance, alpha = (entrance progress) × (current pulse)
    val currentBlur = 14.dp * (1f - entrance)
    val currentAlpha = entrance * pulse
    val currentScale = 1.04f - 0.04f * entrance

    Text(
        text = "TAP TO CONTINUE",
        color = Color(0xFF888888),
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 1.4.sp,
        modifier = Modifier
            .blur(currentBlur)
            .alpha(currentAlpha)
            .scale(currentScale),
    )
}

/** Circular back button with arrow icon, top-left placement. */
@Composable
private fun BackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.92f else 1f,
        animationSpec = tween(120),
        label = "back_scale",
    )
    Box(
        modifier = modifier
            .size(44.dp)
            .scale(scale)
            .clip(CircleShape)
            .background(Color(0xFF1C1C1E))
            .clickable(
                interactionSource = interaction,
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        // Inline left arrow (no extra drawable dependency)
        Text(
            text = "←",
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Medium,
        )
    }
}

/** Pill-shaped text input — dark fill #1C1C1E, no border, 28dp corner radius. */
@Composable
private fun PillTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
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
            cursorBrush = SolidColor(Color(0xFF80E5FF)),
            textStyle = TextStyle(
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Normal,
            ),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = imeAction,
            ),
            keyboardActions = KeyboardActions(onDone = { onImeAction() }),
            modifier = Modifier
                .fillMaxWidth()
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

/** Full-width pill Continue button. Disabled = #3A3A3A bg + gray text; enabled = cyan bg + black text. */
@Composable
private fun ContinueButton(
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed && enabled) 0.98f else 1f,
        animationSpec = tween(120),
        label = "continue_scale",
    )
    val bg = if (enabled) Color(0xFF80E5FF) else Color(0xFF3A3A3A)
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
