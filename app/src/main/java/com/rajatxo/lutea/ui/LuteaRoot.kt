package com.rajatxo.lutea.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.rajatxo.lutea.ui.screens.NameCardScreen
import com.rajatxo.lutea.ui.screens.QuickPicksScreen

/**
 * Lutea — root composable.
 *
 * Routes between:
 *   - NameCardScreen (onboarding: name entry + permission flow)
 *   - QuickPicksScreen (first page of the app after onboarding)
 */
@Composable
fun LuteaRoot() {
    var onboardingComplete by remember { mutableStateOf(false) }

    if (!onboardingComplete) {
        NameCardScreen(
            onComplete = { displayName ->
                // Onboarding finished — navigate to Quick Picks
                onboardingComplete = true
            },
        )
    } else {
        QuickPicksScreen()
    }
}
