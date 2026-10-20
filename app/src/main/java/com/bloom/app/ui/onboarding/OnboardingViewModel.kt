package com.bloom.app.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.bloom.app.data.Goal
import com.bloom.app.data.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

const val ONBOARDING_STEPS = 4

data class OnboardingUiState(
    /** 0-based: 0 welcome, 1 name, 2 goals, 3 reminder. */
    val step: Int = 0,
    val name: String = "",
    val goals: Set<Goal> = Goal.entries.toSet(),
    val reminderMinutes: Int? = null,
    val saving: Boolean = false,
) {
    val canContinue: Boolean get() = step != 1 || name.isNotBlank()
}

class OnboardingViewModel(
    private val preferences: UserPreferencesRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(OnboardingUiState())
    val state: StateFlow<OnboardingUiState> = _state.asStateFlow()

    fun next() = _state.update { s ->
        if (s.canContinue && s.step < ONBOARDING_STEPS - 1) s.copy(step = s.step + 1) else s
    }

    fun back() = _state.update { s -> if (s.step > 0) s.copy(step = s.step - 1) else s }

    fun setName(value: String) = _state.update { it.copy(name = value.take(MAX_NAME_LENGTH)) }

    fun toggleGoal(goal: Goal, enabled: Boolean) = _state.update { s ->
        s.copy(goals = if (enabled) s.goals + goal else s.goals - goal)
    }

    fun setReminder(minutes: Int) = _state.update { it.copy(reminderMinutes = minutes) }

    /**
     * Saves everything and marks onboarding complete, then calls [onDone].
     * [skipReminder] is used by "I'll set this up later".
     */
    fun finish(skipReminder: Boolean, onDone: () -> Unit) {
        val current = _state.value
        if (current.saving) return // ignore double taps
        _state.update { it.copy(saving = true) }
        viewModelScope.launch {
            preferences.completeOnboarding(
                name = current.name,
                trackedGoals = current.goals,
                reminderMinutes = if (skipReminder) null else current.reminderMinutes,
            )
            onDone()
        }
    }

    companion object {
        private const val MAX_NAME_LENGTH = 30

        fun factory(repository: UserPreferencesRepository) = viewModelFactory {
            initializer { OnboardingViewModel(repository) }
        }
    }
}
