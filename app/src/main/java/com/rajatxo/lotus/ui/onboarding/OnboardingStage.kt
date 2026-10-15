package com.rajatxo.lotus.ui.onboarding

/**
 * The five narrative stages of the cinematic onboarding, in order.
 *
 * Inspiration reference (the "Opal" app onboarding video):
 *   Splash     → pure black, single brand mark breathing
 *   Brand      → brand mark + "Lotus" wordmark reveal on dark textured bg
 *   Bud        → closed lotus bud drawn at full hero size, "Look closer."
 *   Bloom      → bud opens into full petals with glow, "Let it bloom."
 *   Welcome    → smaller bloomed lotus shifts to header, conversational form
 *                "I'll help you find your sound." / "First, what's your name?"
 *
 * No progress dots, no step counter — pacing is meditative.
 */
enum class OnboardingStage {
    Splash,
    Brand,
    Bud,
    Bloom,
    Welcome;

    val next: OnboardingStage?
        get() = entries.getOrNull(ordinal + 1)

    val previous: OnboardingStage?
        get() = entries.getOrNull(ordinal - 1)
}
