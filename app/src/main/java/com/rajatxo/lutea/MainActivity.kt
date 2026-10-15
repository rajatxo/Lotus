package com.rajatxo.lutea

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import com.rajatxo.lutea.ui.LuteaRoot
import com.rajatxo.lutea.ui.theme.LuteaTheme

/**
 * Lutea — single Activity host for the Compose UI.
 *
 * Edge-to-edge layout is enforced (universal layout law) — the system bars are transparent
 * and the Compose tree is responsible for handling window insets via `WindowInsets`.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent { LuteaTheme { LuteaRoot() } }
    }
}
