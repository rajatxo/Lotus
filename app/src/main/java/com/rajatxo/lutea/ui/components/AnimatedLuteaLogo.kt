package com.rajatxo.lutea.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.rajatxo.lutea.R

/**
 * Animated Lutea logo — plays the Lottie animation ONCE on first composition.
 *
 * Source: Lutea-Inspiration repo, Relaxing.json (Lottie v5.8.1, 60fps, 360 frames,
 * 250x250 viewport, 6-second animation, 2 layers).
 *
 * Per user request:
 *   'instead of showing static logo on the page..I want it animate one time'
 *
 * Behavior:
 *   - Loads the Lottie JSON from res/raw/lutea_logo_animation.json
 *   - Plays from frame 0 → 360 exactly ONCE (iterations = 1, NOT LottieConstants.IterateForever)
 *   - Stays on the last frame after playback finishes (logo holds its final pose)
 *
 * @param sizeDp display size of the logo (matches the previous static Image's size)
 * @param modifier standard Compose modifier
 */
@Composable
fun AnimatedLuteaLogo(
    sizeDp: Dp,
    modifier: Modifier = Modifier,
) {
    val composition by rememberLottieComposition(
        spec = LottieCompositionSpec.RawRes(R.raw.lutea_logo_animation),
    )

    // playOnce = true → animation plays one time, then stops at the last frame.
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = 1,  // ← ONE TIME only (NOT LottieConstants.IterateForever)
        isPlaying = true,
    )

    LottieAnimation(
        composition = composition,
        progress = { progress },
        modifier = modifier.size(sizeDp),
    )
}
