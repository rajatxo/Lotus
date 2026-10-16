package com.rajatxo.lutea.ui.foundation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

/**
 * Lutea — pure black background.
 *
 * Updated per user request: 'make the background pure black'.
 * The flower image (drawable/flower_top.jpg) is added on top of this background
 * at the TOP of the screen. Its bottom naturally fades to black (#020001 at the
 * very bottom pixel), so it blends seamlessly into this pure-black background.
 *
 * The flower + text animations sit on top of this black canvas. No teal gradient,
 * no radial glow — just pure black so the flower's bottom transition is invisible.
 */
@Composable
fun LuteaBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        content()
    }
}
