package com.rajatxo.lutea.ui.foundation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Lutea — cinematic dark background.
 *
 * Recreated from the Lutea-Inspiration screenshot (Screenshot_2026-10-15-18-12-32-35_*.jpg).
 * NOT pure black — that looks flat and generic. Instead it's a teal-tinted dark base
 * with a soft radial glow positioned high up. The combination is what makes premium
 * music apps feel "deep" rather than "empty":
 *
 *   ┌──────────────────────────┐
 *   │                          │
 *   │       ░░░░░░░░░░         │  ← Top-center radial glow (desaturated teal)
 *   │     ░░░░░░░░░░░░░       │     Wide ellipse, ~20% down, low alpha
 *   │   ░░░░░░░░░░░░░░░░░     │
 *   │  ░░░░░░░░░░░░░░░░░░░    │
 *   │                          │
 *   │                          │  ← Linear gradient continues fading down
 *   │                          │
 *   │                          │
 *   │                          │  ← Bottom: deepest tinted black #08090A
 *   └──────────────────────────┘
 *
 * Color values sampled from the inspiration screenshot:
 *   Top (slate-teal):  #1A2024 (L: ~12%)
 *   Middle transition:  #0F1315 (L: ~6%)
 *   Bottom (deep tint):  #08090A (L: ~3%)
 *   Glow center color:   #2A383C at ~40% alpha
 *
 * The teal-tinted base (#0A0B0C, NOT neutral gray #111) is the critical ingredient
 * — it gives the background that "oceanic / cinematic" feel rather than "boring dark
 * mode". Pure black would crush shadows; pure gray would look like a Notion doc.
 *
 * Usage: wrap any screen with `LuteaBackground { ... }` instead of using
 * `Modifier.background(Color.Black)` directly.
 */
@Composable
fun LuteaBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    BoxWithConstraints(
        modifier = modifier.fillMaxSize(),
    ) {
        val widthPx = constraints.maxWidth.toFloat()
        val heightPx = constraints.maxHeight.toFloat()

        // Vertical gradient: slate-teal at top → deep tinted black at bottom.
        val verticalGradient = Brush.verticalGradient(
            colors = listOf(
                Color(0xFF1A2024),  // top: dark slate-teal
                Color(0xFF0F1315),  // ~40% down: transition
                Color(0xFF08090A),  // bottom: deep tinted black
            ),
        )

        // Soft radial glow at top-center (~20% down from top).
        // Wide ellipse — radius = 1.2× width so it stretches wider than the screen.
        // Alpha 0.4 at center fades to transparent, creating the "ambient spotlight" feel.
        val glowRadius = maxOf(widthPx * 0.6f, heightPx * 0.18f)
        val radialGlow = Brush.radialGradient(
            colors = listOf(
                Color(0xFF2A383C).copy(alpha = 0.40f),  // glow center
                Color(0xFF2A383C).copy(alpha = 0.15f),  // mid fade
                Color.Transparent,                       // outer fade
            ),
            center = Offset(
                x = widthPx * 0.5f,   // horizontal center
                y = heightPx * 0.2f,  // 20% down from top — upper third
            ),
            radius = glowRadius,
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(verticalGradient)
                .background(radialGlow),
        ) {
            content()
        }
    }
}
