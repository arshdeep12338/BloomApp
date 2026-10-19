package com.bloom.app.ui.home

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

class HomeViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState().withCurrentWeek())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun onDaySelected(index: Int) = _uiState.update { it.copy(selectedDayIndex = index) }

    fun onMoodSelected(mood: Mood) = _uiState.update {
        it.copy(selectedMood = mood, checkInSaved = false)
    }

    fun onEnergyChanged(value: Float) = _uiState.update {
        it.copy(energy = value, checkInSaved = false)
    }

    fun onSymptomToggled(symptom: Symptom) = _uiState.update { state ->
        val updated = if (symptom in state.selectedSymptoms) {
            state.selectedSymptoms - symptom
        } else {
            state.selectedSymptoms + symptom
        }
        state.copy(selectedSymptoms = updated, checkInSaved = false)
    }

    fun onSaveCheckIn() = _uiState.update { state ->
        if (state.selectedMood == null) {
            state.copy(message = "Pick how you're feeling first")
        } else {
            state.copy(checkInSaved = true, message = "Check-in saved. Nice work today!")
        }
    }

    fun onQuickAction(action: QuickAction) = _uiState.update { it.copy(message = action.confirmation) }

    fun onArticleClicked(article: Article) = _uiState.update { it.copy(message = "Opening: ${article.title}") }

    fun onTabSelected(tab: HomeTab) = _uiState.update { it.copy(selectedTab = tab) }

    fun onMessageShown() = _uiState.update { it.copy(message = null) }

    private fun HomeUiState.withCurrentWeek(): HomeUiState {
        val today = LocalDate.now()
        val monday = today.with(DayOfWeek.MONDAY)
        val days = (0..6).map { offset ->
            val date = monday.plusDays(offset.toLong())
            WeekDay(
                label = date.dayOfWeek.getDisplayName(TextStyle.NARROW, Locale.getDefault()),
                dayOfMonth = date.dayOfMonth,
                isToday = date == today,
            )
        }
        return copy(week = days, selectedDayIndex = days.indexOfFirst { it.isToday }.coerceAtLeast(0))
    }
}
