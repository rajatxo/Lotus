package com.rajatxo.lutea.ui.foundation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.rememberGraphicsLayer
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop

/**
 * Lutea backdrop — Kyant liquid glass blur layer (Coral's clever 3-layer approach).
 *
 * Same approach Coral's SettingsScreen uses:
 *   Layer 1: Visible background (flower image, solid color, gradient, etc.)
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
 * Critical ordering: Layer 2 (the capture layer with layerBackdrop) must come
 * BEFORE Layer 3 (content with drawBackdrop) in the z-order. Kyant content lives
 * on TOP of the visible background — that's why drawBackdrop can blur it.
 *
 * Usage:
 *   LuteaBackdrop(background = { FlowerImage() }) { backdrop ->
 *     // Your content here — use Modifier.drawBackdrop(backdrop, ...) on individual
 *     // elements to give them the frosted-glass blur effect.
 *     PillTextField(..., backdrop = backdrop)
 *     ContinueButton(..., backdrop = backdrop)
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
        // This is what the user actually sees.
        background()

        // ── Layer 2: Transparent capture layer (NOT clickable, NOT scrollable) ──
        // Renders the SAME background inside it so kyant can capture it for blurring.
        // Zero interactivity — just exists to be captured by the backdrop.
        // CRITICAL: layerBackdrop() must be OUTSIDE any scrollable container to
        // avoid the kyant crash. This Box is not scrollable → safe.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .layerBackdrop(backdrop),
        ) {
            background()
        }

        // ── Layer 3: Foreground content (ON TOP of everything) ──────────
        // Elements here can call Modifier.drawBackdrop(backdrop, ...) to blur
        // whatever is behind them (Layer 1's background, captured by Layer 2).
        // This is the TOPMOST layer — kyant content sits above the background.
        content(backdrop)
    }
}
