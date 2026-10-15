package com.rajatxo.lotus.ui

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import com.rajatxo.lotus.ui.onboarding.OnboardingScreen
import com.rajatxo.lotus.ui.onboarding.completeOnboarding
import com.rajatxo.lotus.ui.onboarding.isOnboardingComplete
import kotlinx.coroutines.launch

/**
 * Root routing composable — decides between the cinematic OnboardingScreen (first launch)
 * and the main LotusApp (every subsequent launch).
 *
 * Onboarding completion is persisted via DataStore. The display name collected during
 * onboarding is saved alongside the completion flag.
 *
 * Permission flow is preserved: if the user hasn't granted READ_MEDIA_AUDIO, we still
 * show a black screen rather than the song list (matches the onboarding's pure-black
 * aesthetic so the transition feels seamless).
 */
@Composable
fun LotusRoot() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var hasPermission by remember { mutableStateOf(checkMusicPermission(context)) }
    var onboardingState by remember { mutableStateOf<OnboardingState>(OnboardingState.Loading) }

    // Read the onboarding completion flag once on first composition.
    LaunchedEffect(Unit) {
        onboardingState = if (isOnboardingComplete(context)) {
            OnboardingState.Done
        } else {
            OnboardingState.Pending
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        hasPermission = result.values.all { it }
    }

    LaunchedEffect(Unit) {
        if (!hasPermission) {
            permissionLauncher.launch(getRequiredPermissions())
        }
    }

    when (onboardingState) {
        OnboardingState.Loading -> {
            // Hold pure black while the prefs read resolves — invisible to the user,
            // the window background is already black via the theme.
            Box(modifier = Modifier.fillMaxSize())
        }

        OnboardingState.Pending -> {
            OnboardingScreen(
                onComplete = { displayName ->
                    scope.launch {
                        completeOnboarding(context, displayName)
                        onboardingState = OnboardingState.Done
                    }
                },
            )
        }

        OnboardingState.Done -> {
            if (!hasPermission) {
                Box(modifier = Modifier.fillMaxSize())
            } else {
                LotusApp()
            }
        }
    }
}

private sealed class OnboardingState {
    data object Loading : OnboardingState()
    data object Pending : OnboardingState()
    data object Done : OnboardingState()
}

private fun checkMusicPermission(context: android.content.Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_AUDIO) ==
            PermissionChecker.PERMISSION_GRANTED
    } else {
        ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) ==
            PermissionChecker.PERMISSION_GRANTED
    }
}

private fun getRequiredPermissions(): Array<String> {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(Manifest.permission.READ_MEDIA_AUDIO)
    } else {
        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
    }
}
