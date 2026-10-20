package com.bloom.app.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDate

enum class MealType(val label: String) { Breakfast("Breakfast"), Lunch("Lunch"), Dinner("Dinner") }

enum class MealQuality(val label: String) {
    Light("Light"),
    Balanced("Balanced"),
    Indulgent("Indulgent"),
    Skipped("Skipped"),
}

enum class SymptomCategory(val label: String, val options: List<String>) {
    Bloating("Bloating", listOf("None", "Mild", "Moderate", "Severe")),
    Skin("Skin & acne", listOf("Clear", "Mild", "Moderate", "Flare-up")),
    Mood("Mood", listOf("Great", "Okay", "Low", "Very low")),
    SleepQuality("Sleep quality", listOf("Great", "Okay", "Poor", "Very poor")),
}

/** Cycle length used to estimate the next period until real history exists. */
const val DEFAULT_CYCLE_DAYS = 28L

data class DailyLog(
    val meals: Map<MealType, MealQuality> = emptyMap(),
    val movementMin: Int = 0,
    val waterGlasses: Int = 0,
    val relaxationMin: Int = 0,
    val sleepHrs: Int = 0,
    val periodStart: LocalDate? = null,
    /** Selected option index per symptom category. */
    val symptoms: Map<SymptomCategory, Int> = emptyMap(),
) {
    fun progressOf(goal: Goal): Int = when (goal) {
        Goal.Meals -> meals.size
        Goal.Movement -> movementMin
        Goal.Water -> waterGlasses
        Goal.Relaxation -> relaxationMin
        Goal.Sleep -> sleepHrs
        Goal.Cycle, Goal.Symptoms -> 0
    }
}

data class GoalProgress(val goal: Goal, val current: Int, val target: Int) {
    val fraction: Float get() = if (target <= 0) 0f else (current.toFloat() / target).coerceIn(0f, 1f)
    val isComplete: Boolean get() = target > 0 && current >= target
    val label: String get() = "$current/$target ${goal.unit}"
}

/** Progress for the measurable goals the user chose to track. */
fun goalProgress(prefs: UserPreferences, log: DailyLog): List<GoalProgress> =
    Goal.entries
        .filter { it.measurable && it in prefs.trackedGoals }
        .map { GoalProgress(it, log.progressOf(it), prefs.targetOf(it)) }

fun List<GoalProgress>.overallPercent(): Int =
    if (isEmpty()) 0 else (map { it.fraction }.average() * 100).toInt()

/**
 * In-memory log of today's entries. (Prototype: it is not persisted, so it resets when the
 * app process restarts. Swap this class for a Room/DataStore-backed one to persist.)
 */
class WellnessRepository {

    private val _log = MutableStateFlow(DailyLog())
    val log: StateFlow<DailyLog> = _log.asStateFlow()

    fun addProgress(goal: Goal, delta: Int) = _log.update { current ->
        val updated = (current.progressOf(goal) + delta).coerceIn(0, MAX_VALUE)
        when (goal) {
            Goal.Movement -> current.copy(movementMin = updated)
            Goal.Water -> current.copy(waterGlasses = updated)
            Goal.Relaxation -> current.copy(relaxationMin = updated)
            Goal.Sleep -> current.copy(sleepHrs = updated)
            else -> current
        }
    }

    /** Selecting the already-selected option clears it. */
    fun setMeal(type: MealType, quality: MealQuality) = _log.update { current ->
        val meals = if (current.meals[type] == quality) current.meals - type else current.meals + (type to quality)
        current.copy(meals = meals)
    }

    fun setPeriodStart(date: LocalDate?) = _log.update { it.copy(periodStart = date) }

    fun setSymptom(category: SymptomCategory, optionIndex: Int) = _log.update { current ->
        val symptoms = if (current.symptoms[category] == optionIndex) {
            current.symptoms - category
        } else {
            current.symptoms + (category to optionIndex)
        }
        current.copy(symptoms = symptoms)
    }

    fun reset() {
        _log.value = DailyLog()
    }

    private companion object {
        const val MAX_VALUE = 999
    }
}
