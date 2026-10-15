package com.rajatxo.lutea.ui

import androidx.compose.runtime.Composable
import com.rajatxo.lutea.ui.screens.NameCardScreen

/**
 * Lutea — root composable.
 *
 * Currently routes straight to the NameCardScreen — the first content screen of the
 * app, featuring the cinematic blur-in text reveal + name input form (from the
 * Lutea-Inspiration video).
 *
 * The screen uses LuteaBackground (teal-tinted gradient + radial glow) for
 * universe-fitting consistency — every screen in Lutea will use this same background.
 */
@Composable
fun LuteaRoot() {
    NameCardScreen(
        onComplete = { displayName ->
            // TODO: persist the name + navigate to the next screen.
            // For now this is a no-op so we can verify the animation + form behavior.
            @Suppress("UNUSED_VARIABLE")
            val _name = displayName
        },
    )
}
