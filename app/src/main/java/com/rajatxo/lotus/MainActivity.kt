package com.rajatxo.lotus

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import com.rajatxo.lotus.ui.LotusRoot
import com.rajatxo.lotus.ui.theme.LotusTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // ★ Edge-to-edge — universal layout law #2
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent { LotusTheme { LotusRoot() } }
    }
}
