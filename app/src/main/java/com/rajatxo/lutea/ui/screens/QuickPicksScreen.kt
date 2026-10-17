package com.rajatxo.lutea.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.rajatxo.lutea.ui.components.CalSansFamily

/**
 * QuickPicksScreen — the first page of Lutea after onboarding.
 *
 * Placeholder for now — shows "Quick Picks" centered on pure black.
 * Will be built out with actual music content later.
 */
@Composable
fun QuickPicksScreen(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "Quick Picks",
            color = Color.White,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = CalSansFamily,
        )
    }
}
