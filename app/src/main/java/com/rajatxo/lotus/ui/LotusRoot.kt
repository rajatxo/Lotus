package com.rajatxo.lotus.ui

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/**
 * ★ LotusRoot — the root composable.
 *
 * Shows an animated splash screen with the Lotus name, then will transition
 * to the main app content (player, library, etc.) in future steps.
 *
 * The splash has a smooth fade-in + scale-up animation with spring physics —
 * this sets the tone for the entire app: everything moves with springs.
 */
@Composable
fun LotusRoot() {
    var showSplash by remember { mutableStateOf(true) }

    // Splash animation state
    var animationStarted by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(100)
        animationStarted = true
        delay(1500)
        showSplash = false
    }

    if (showSplash) {
        LotusSplash(animationStarted)
    } else {
        // Placeholder — will be replaced with the actual app
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Lotus",
                color = Color.White,
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.headlineLarge
            )
        }
    }
}

/**
 * Splash screen with smooth spring animation.
 * The "Lotus" text fades in + scales up with spring physics.
 */
@Composable
private fun LotusSplash(animate: Boolean) {
    // ★ Alpha animation — spring, not tween
    val alpha by animateFloatAsState(
        targetValue = if (animate) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "splashAlpha"
    )

    // ★ Scale animation — spring with a little bounce
    val scale by animateFloatAsState(
        targetValue = if (animate) 1f else 0.6f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "splashScale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0A0A0A),
                        Color(0xFF000000)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .alpha(alpha)
                .padding(32.dp)
        ) {
            Text(
                text = "Lotus",
                color = Color.White,
                fontSize = 56.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.alpha(alpha)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Music Player",
                color = Color.White.copy(alpha = 0.4f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 2.sp,
                modifier = Modifier.alpha(alpha * 0.7f)
            )
        }
    }
}
