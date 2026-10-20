package com.bloom.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

interface UserPreferencesRepository {
    val preferences: Flow<UserPreferences>
    suspend fun completeOnboarding(name: String, trackedGoals: Set<Goal>, reminderMinutes: Int?)

    suspend fun setName(name: String)
    suspend fun setJourneyStage(stage: JourneyStage?)
    suspend fun setGoalTracked(goal: Goal, tracked: Boolean)
    suspend fun setTarget(goal: Goal, value: Int)
    suspend fun setNotificationsEnabled(enabled: Boolean)
    suspend fun setReminderMinutes(minutes: Int?)
    suspend fun clear()
}

private val Context.bloomDataStore: DataStore<Preferences> by preferencesDataStore(name = "bloom_prefs")

class DataStoreUserPreferencesRepository(context: Context) : UserPreferencesRepository {

    private val dataStore = context.applicationContext.bloomDataStore

    override val preferences: Flow<UserPreferences> = dataStore.data
        .catch { error ->
            if (error is IOException) emit(emptyPreferences()) else throw error
        }
        .map { prefs -> prefs.toUserPreferences() }

    override suspend fun completeOnboarding(name: String, trackedGoals: Set<Goal>, reminderMinutes: Int?) {
        dataStore.edit { prefs ->
            prefs[KEY_COMPLETED] = true
            prefs[KEY_NAME] = name.trim()
            prefs[KEY_TRACKED] = trackedGoals.map { it.name }.toSet()
            prefs[KEY_REMINDER] = reminderMinutes ?: NO_REMINDER
        }
    }

    override suspend fun setName(name: String) {
        dataStore.edit { it[KEY_NAME] = name.trim() }
    }

    override suspend fun setJourneyStage(stage: JourneyStage?) {
        dataStore.edit { prefs ->
            if (stage == null) prefs.remove(KEY_STAGE) else prefs[KEY_STAGE] = stage.name
        }
    }

    override suspend fun setGoalTracked(goal: Goal, tracked: Boolean) {
        dataStore.edit { prefs ->
            val current = prefs[KEY_TRACKED] ?: Goal.entries.map { it.name }.toSet()
            prefs[KEY_TRACKED] = if (tracked) current + goal.name else current - goal.name
        }
    }

    override suspend fun setTarget(goal: Goal, value: Int) {
        if (!goal.measurable) return
        dataStore.edit { it[targetKey(goal)] = value.coerceIn(goal.minTarget, goal.maxTarget) }
    }

    override suspend fun setNotificationsEnabled(enabled: Boolean) {
        dataStore.edit { it[KEY_NOTIFICATIONS] = enabled }
    }

    override suspend fun setReminderMinutes(minutes: Int?) {
        dataStore.edit { it[KEY_REMINDER] = minutes ?: NO_REMINDER }
    }

    override suspend fun clear() {
        dataStore.edit { it.clear() }
    }

    private fun Preferences.toUserPreferences(): UserPreferences {
        val tracked = this[KEY_TRACKED]
            ?.mapNotNull { saved -> Goal.entries.firstOrNull { it.name == saved } }
            ?.toSet()
            ?: Goal.entries.toSet()
        val targets = Goal.entries
            .filter { it.measurable }
            .associateWith { goal -> this[targetKey(goal)] ?: goal.defaultTarget }
        return UserPreferences(
            onboardingCompleted = this[KEY_COMPLETED] ?: false,
            userName = this[KEY_NAME].orEmpty(),
            trackedGoals = tracked,
            targets = targets,
            reminderMinutes = this[KEY_REMINDER]?.takeIf { it >= 0 },
            notificationsEnabled = this[KEY_NOTIFICATIONS] ?: true,
            journeyStage = this[KEY_STAGE]?.let { saved -> JourneyStage.entries.firstOrNull { it.name == saved } },
        )
    }

    private companion object {
        const val NO_REMINDER = -1
        val KEY_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val KEY_NAME = stringPreferencesKey("user_name")
        val KEY_TRACKED = stringSetPreferencesKey("tracked_goals")
        val KEY_REMINDER = intPreferencesKey("reminder_minutes")
        val KEY_NOTIFICATIONS = booleanPreferencesKey("notifications_enabled")
        val KEY_STAGE = stringPreferencesKey("journey_stage")
        fun targetKey(goal: Goal) = intPreferencesKey("target_${goal.name}")
    }
}
