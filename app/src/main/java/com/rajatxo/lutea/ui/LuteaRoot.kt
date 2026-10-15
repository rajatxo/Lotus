package com.rajatxo.lutea.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

/**
 * Lutea — root composable.
 *
 * CLEAN SLATE: This is a placeholder. As the user specifies each page/feature, this
 * composable will route to the actual screens.
 *
 * For now it shows a single centered wordmark on pure black so we can verify the
 * theme + typography + edge-to-edge setup before building anything else.
 */
@Composable
fun LuteaRoot() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "Lutea",
            color = Color(0xFFD4A03A),
        )
    }
}
