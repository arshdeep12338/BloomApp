package com.bloom.app.data

/** Everything a user can track. The first five have numeric daily targets. */
enum class Goal(
    val title: String,
    val unit: String,
    val defaultTarget: Int,
    val logStep: Int,
    val targetStep: Int,
    val minTarget: Int,
    val maxTarget: Int,
    val onboardingTitle: String,
    val onboardingDescription: String,
) {
    Meals(
        "Meals", "meals", 3, 1, 1, 1, 6,
        "Meals – 3 balanced meals",
        "Steadier blood sugar helps keep PCOS symptoms calmer.",
    ),
    Movement(
        "Movement", "min", 30, 10, 5, 10, 120,
        "Movement – 30 minutes",
        "Supports insulin sensitivity, and easy, low-impact movement counts.",
    ),
    Water(
        "Water", "glasses", 8, 1, 1, 1, 16,
        "Water – 8 glasses",
        "Helps with energy and bloating.",
    ),
    Relaxation(
        "Relaxation", "min", 15, 5, 5, 5, 60,
        "Relaxation – 15 minutes",
        "Lower stress means lower cortisol, which can worsen symptoms.",
    ),
    Sleep(
        "Sleep", "hrs", 8, 1, 1, 4, 12,
        "Sleep – 8 hours",
        "Consistent sleep helps balance hormones over time.",
    ),
    Cycle(
        "Cycle", "", 0, 0, 0, 0, 0,
        "Cycle – period dates",
        "Spot your personal patterns and predict your next period.",
    ),
    Symptoms(
        "Symptom check-in", "", 0, 0, 0, 0, 0,
        "Symptom check-in – bloating, skin, mood, sleep quality",
        "Track how you feel day to day to notice patterns over time.",
    );

    /** True for goals with a numeric daily target that feeds "Today's Goals". */
    val measurable: Boolean get() = defaultTarget > 0
}

enum class JourneyStage(val label: String, val hint: String) {
    Diagnosed("Diagnosed", "I have a confirmed PCOS diagnosis"),
    Suspected("Suspected", "A doctor mentioned it, but it's not confirmed"),
    Exploring("Exploring", "I think I might have symptoms"),
    NotSure("Not sure yet", "I just want to learn more"),
}

enum class ReminderSlot(val label: String, val minutes: Int, val hint: String) {
    Morning("Morning", 7 * 60 + 30, "start the day on track"),
    Afternoon("Afternoon", 13 * 60, "a midday check-in"),
    Evening("Evening", 20 * 60, "wind down and reflect"),
}

data class UserPreferences(
    val onboardingCompleted: Boolean = false,
    val userName: String = "",
    val trackedGoals: Set<Goal> = Goal.entries.toSet(),
    val targets: Map<Goal, Int> = Goal.entries.filter { it.measurable }.associateWith { it.defaultTarget },
    /** Minutes after midnight, or null when no reminder is set. */
    val reminderMinutes: Int? = null,
    val notificationsEnabled: Boolean = true,
    val journeyStage: JourneyStage? = null,
) {
    fun targetOf(goal: Goal): Int = targets[goal] ?: goal.defaultTarget
}
