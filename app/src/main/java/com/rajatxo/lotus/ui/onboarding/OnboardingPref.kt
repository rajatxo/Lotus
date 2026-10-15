package com.rajatxo.lotus.ui.onboarding

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * Single source of truth for "has the user finished onboarding" + their chosen display name.
 *
 * Uses androidx.datastore.preferences. Read once at app entry to decide between
 * OnboardingScreen vs LotusApp; written once when the user taps Continue.
 */

private val Context.onboardingDataStore by preferencesDataStore(
    name = "lotus_onboarding"
)

private val KEY_COMPLETED = booleanPreferencesKey("completed")
private val KEY_DISPLAY_NAME = stringPreferencesKey("display_name")

/** Snapshot — use from a coroutine (not a composable). */
suspend fun isOnboardingComplete(context: Context): Boolean =
    context.onboardingDataStore.data
        .map { it[KEY_COMPLETED] ?: false }
        .first()

/** Snapshot — use from a coroutine (not a composable). */
suspend fun readDisplayName(context: Context): String? =
    context.onboardingDataStore.data
        .map { it[KEY_DISPLAY_NAME] }
        .first()

/** Mark onboarding complete and persist the user's chosen name. */
suspend fun completeOnboarding(context: Context, displayName: String) {
    context.onboardingDataStore.edit { prefs ->
        prefs[KEY_COMPLETED] = true
        prefs[KEY_DISPLAY_NAME] = displayName.trim().ifBlank { "Friend" }
    }
}
