package com.bloom.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

interface OnboardingRepository {
    /** Emits true once the user has finished (or skipped) onboarding. */
    val onboardingCompleted: Flow<Boolean>

    suspend fun setOnboardingCompleted()
}

private val Context.bloomDataStore: DataStore<Preferences> by preferencesDataStore(name = "bloom_prefs")

class DataStoreOnboardingRepository(context: Context) : OnboardingRepository {

    private val dataStore = context.applicationContext.bloomDataStore

    override val onboardingCompleted: Flow<Boolean> = dataStore.data
        .catch { error ->
            // A corrupted/unreadable file should not crash launch: treat as first launch.
            if (error is IOException) emit(emptyPreferences()) else throw error
        }
        .map { prefs -> prefs[KEY_ONBOARDING_COMPLETED] ?: false }

    override suspend fun setOnboardingCompleted() {
        dataStore.edit { prefs -> prefs[KEY_ONBOARDING_COMPLETED] = true }
    }

    private companion object {
        val KEY_ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
    }
}
