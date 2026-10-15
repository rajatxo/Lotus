package com.rajatxo.lutea.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Lutea — color scheme.
 *
 * Premium dark-first palette inspired by the autumn ginkgo leaf:
 *   - Pure black background (#000000) for OLED-friendly cinematic dark
 *   - Elevated dark surfaces (#1A1A1A, #2A2A2A) for cards/inputs (no borders)
 *   - Ginkgo-gold accent (#D4A03A) — the iconic Lutea yellow
 *   - Soft cream text (#F5F0E6) — warmer than pure white, more premium feel
 *
 * Always dark — Lutea is a music player, not a docs app.
 */
private val LuteaDark = darkColorScheme(
    primary = Color(0xFFD4A03A),
    onPrimary = Color(0xFF000000),
    primaryContainer = Color(0xFF2A2A2A),
    onPrimaryContainer = Color(0xFFF5F0E6),
    secondary = Color(0xFFB0B0B0),
    onSecondary = Color(0xFF000000),
    secondaryContainer = Color(0xFF1E1E1E),
    onSecondaryContainer = Color(0xFFF5F0E6),
    tertiary = Color(0xFF80E5FF),
    onTertiary = Color(0xFF000000),
    background = Color(0xFF000000),
    onBackground = Color(0xFFF5F0E6),
    surface = Color(0xFF0D0D0D),
    onSurface = Color(0xFFF5F0E6),
    surfaceVariant = Color(0xFF1A1A1A),
    onSurfaceVariant = Color(0xFFB0B0B0),
    outline = Color(0xFF404040),
    outlineVariant = Color(0xFF282828),
    inverseSurface = Color(0xFFF5F0E6),
    inverseOnSurface = Color(0xFF000000),
)

@Composable
fun LuteaTheme(
    @Suppress("UNUSED_PARAMETER") darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    // Always dark — Lutea is a music player, not a docs app.
    MaterialTheme(
        colorScheme = LuteaDark,
        typography = LuteaTypography,
        content = content,
    )
}
