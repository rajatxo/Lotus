package com.rajatxo.lutea.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.rajatxo.lutea.ui.foundation.LuteaBackground

/**
 * Lutea — root composable.
 *
 * CLEAN SLATE: This is a placeholder. As the user specifies each page/feature, this
 * composable will route to the actual screens.
 *
 * For now it shows just the cinematic background — teal-tinted gradient with a soft
 * radial glow at the top. The lotus logo will sit on top of this once the user tells
 * us where to put it.
 */
@Composable
fun LuteaRoot() {
    LuteaBackground {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            // No content yet — pure background per user instruction:
            // "don't add any text for now just work on background"
        }
    }
}
