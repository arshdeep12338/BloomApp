package com.bloom.app.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.bloom.app.data.DailyLog
import com.bloom.app.data.Goal
import com.bloom.app.data.GoalProgress
import com.bloom.app.data.JourneyStage
import com.bloom.app.data.MealQuality
import com.bloom.app.data.MealType
import com.bloom.app.data.SymptomCategory
import com.bloom.app.data.UserPreferences
import com.bloom.app.data.UserPreferencesRepository
import com.bloom.app.data.WellnessRepository
import com.bloom.app.data.goalProgress
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

enum class MainTab(val label: String) {
    Dashboard("Dashboard"),
    Insights("Insights"),
    Learn("Learn"),
    Settings("Settings"),
}

enum class SheetType {
    Meals, Movement, Water, Relaxation, Sleep, Cycle, Symptoms,
    EditProfile, ReminderTime, NotificationPreview,
}

fun Goal.sheet(): SheetType = when (this) {
    Goal.Meals -> SheetType.Meals
    Goal.Movement -> SheetType.Movement
    Goal.Water -> SheetType.Water
    Goal.Relaxation -> SheetType.Relaxation
    Goal.Sleep -> SheetType.Sleep
    Goal.Cycle -> SheetType.Cycle
    Goal.Symptoms -> SheetType.Symptoms
}

data class MainUiState(
    val prefs: UserPreferences = UserPreferences(),
    val log: DailyLog = DailyLog(),
    val selectedTab: MainTab = MainTab.Dashboard,
    val sheet: SheetType? = null,
    val tipDismissed: Boolean = false,
    val message: String? = null,
) {
    val progress: List<GoalProgress> get() = goalProgress(prefs, log)
    val completedCount: Int get() = progress.count { it.isComplete }
}
class MainViewModel(
    private val preferences: UserPreferencesRepository,
    private val wellness: WellnessRepository,
) : ViewModel() {

    private data class LocalState(
        val tab: MainTab = MainTab.Dashboard,
        val sheet: SheetType? = null,
        val tipDismissed: Boolean = false,
        val message: String? = null,
    )

    private val local = MutableStateFlow(LocalState())

    val uiState: StateFlow<MainUiState> = combine(
        preferences.preferences,
        wellness.log,
        local,
    ) { prefs, log, l ->
        MainUiState(
            prefs = prefs,
            log = log,
            selectedTab = l.tab,
            sheet = l.sheet,
            tipDismissed = l.tipDismissed,
            message = l.message,
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, MainUiState())
    fun selectTab(tab: MainTab) = local.update { it.copy(tab = tab, sheet = null) }
    fun openSheet(sheet: SheetType) = local.update { it.copy(sheet = sheet) }
    fun closeSheet() = local.update { it.copy(sheet = null) }
    fun showMessage(text: String) = local.update { it.copy(message = text) }
    fun messageShown() = local.update { it.copy(message = null) }
    fun dismissTip() = local.update { it.copy(tipDismissed = true) }

    fun addProgress(goal: Goal, delta: Int) = wellness.addProgress(goal, delta)
    fun setMeal(type: MealType, quality: MealQuality) = wellness.setMeal(type, quality)
    fun setPeriodStart(date: LocalDate) = wellness.setPeriodStart(date)
    fun setSymptom(category: SymptomCategory, index: Int) = wellness.setSymptom(category, index)

    fun saveProfile(name: String, stage: JourneyStage?) {
        viewModelScope.launch {
            if (name.isNotBlank()) preferences.setName(name)
            preferences.setJourneyStage(stage)
        }
    }

    fun setGoalTracked(goal: Goal, tracked: Boolean) {
        viewModelScope.launch { preferences.setGoalTracked(goal, tracked) }
    }

    fun adjustTarget(goal: Goal, direction: Int) {
        val current = uiState.value.prefs.targetOf(goal)
        viewModelScope.launch { preferences.setTarget(goal, current + direction * goal.targetStep) }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch { preferences.setNotificationsEnabled(enabled) }
    }

    fun setReminder(minutes: Int?) {
        viewModelScope.launch { preferences.setReminderMinutes(minutes) }
    }

    fun signOut(onDone: () -> Unit) {
        viewModelScope.launch {
            preferences.clear()
            wellness.reset()
            onDone()
        }
    }

    companion object {
        fun factory(preferences: UserPreferencesRepository, wellness: WellnessRepository) =
            viewModelFactory { initializer { MainViewModel(preferences, wellness) } }
    }
}
