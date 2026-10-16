package com.rajatxo.lutea.ui.foundation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.rememberGraphicsLayer
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop

/**
 * Lutea backdrop — Kyant liquid glass blur layer.
 *
 * Same approach Coral's SettingsScreen uses (3-layer structure):
 *   Layer 1: Background (visible — flower image, solid color, gradient, etc.)
 *   Layer 2: Transparent capture layer — NOT clickable, NOT scrollable.
 *            layerBackdrop() captures the background behind it.
 *   Layer 3: Foreground content — uses drawBackdrop() to blur the captured
 *            background, producing frosted-glass effect.
 *
 * Why this works (per Coral's comment):
 *   The crash happens when BOTH layerBackdrop AND drawBackdrop are inside a
 *   scrollable container. By keeping layerBackdrop OUTSIDE the scroll (in Layer 2),
 *   the kyant criteria is met — no crash.
 *
 * Usage:
 *   LuteaBackdrop(background = { FlowerImage() }) {
 *     // Your content here — use drawBackdrop(...) on individual elements
 *     // to give them the frosted-glass blur effect.
 *     PillTextField(...)
 *     ContinueButton(...)
 *   }
 *
 * @param background  composable that renders the visible background (image, gradient, etc.)
 *                   This is rendered TWICE — once as the visible Layer 1, once inside
 *                   the layerBackdrop capture Box (Layer 2) so kyant can capture it.
 * @param content     foreground content — elements here can use Modifier.drawBackdrop(...)
 *                   to blur whatever is behind them (Layer 1's background).
 */
@Composable
fun LuteaBackdrop(
    modifier: Modifier = Modifier,
    background: @Composable () -> Unit,
    content: @Composable (backdrop: com.kyant.backdrop.backdrops.LayerBackdrop) -> Unit,
) {
    val graphicsLayer = rememberGraphicsLayer()
    val backdrop = rememberLayerBackdrop(graphicsLayer = graphicsLayer) {
        drawContent()
    }

    Box(modifier = modifier.fillMaxSize()) {
        // ── Layer 1: Visible background ───────────────────────────────────
        // Whatever the caller passes (flower image, gradient, etc.).
        background()

        // ── Layer 2: Transparent capture layer (NOT clickable, NOT scrollable) ──
        // Renders the SAME background inside it so kyant can capture it.
        // Zero interactivity — just exists to be captured by the backdrop.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .layerBackdrop(backdrop),
        ) {
            background()
        }

        // ── Layer 3: Foreground content ──────────────────────────────────
        // Elements here can call Modifier.drawBackdrop(backdrop, ...) to blur
        // whatever is behind them.
        content(backdrop)
    }
}
