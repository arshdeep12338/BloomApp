package com.bloom.app.ui.home

import androidx.compose.ui.graphics.Color
import com.bloom.app.ui.theme.LavenderLight
import com.bloom.app.ui.theme.MintLight
import com.bloom.app.ui.theme.PeachLight
import com.bloom.app.ui.theme.RoseLight

enum class Mood(val emoji: String, val label: String) {
    Happy("😊", "Happy"),
    Calm("😌", "Calm"),
    Tired("😴", "Tired"),
    Anxious("😟", "Anxious"),
    Low("😢", "Low"),
}

enum class Symptom(val label: String) {
    Cramps("Cramps"),
    Bloating("Bloating"),
    Acne("Acne"),
    Fatigue("Fatigue"),
    Headache("Headache"),
    HairFall("Hair fall"),
    Cravings("Cravings"),
    MoodSwings("Mood swings"),
}

enum class QuickAction(val emoji: String, val label: String, val confirmation: String) {
    LogPeriod("🩸", "Log period", "Period logged for today"),
    Water("💧", "Water", "Added a glass of water"),
    Sleep("😴", "Sleep", "Sleep log opened"),
    Meds("💊", "Meds", "Medication reminder noted"),
}

enum class HomeTab(val label: String) {
    Home("Home"),
    Track("Track"),
    Insights("Insights"),
    Profile("Profile"),
}

data class WeekDay(val label: String, val dayOfMonth: Int, val isToday: Boolean)

data class Insight(val emoji: String, val title: String, val body: String, val background: Color)

data class Article(val emoji: String, val title: String, val meta: String, val background: Color)

data class HomeUiState(
    val userName: String = "Priya",
    val cycleDay: Int = 14,
    val cycleLength: Int = 32,
    val phase: String = "Ovulation window",
    val nextPeriodInDays: Int = 18,
    val week: List<WeekDay> = emptyList(),
    val selectedDayIndex: Int = 0,
    val selectedMood: Mood? = null,
    val energy: Float = 0.5f,
    val selectedSymptoms: Set<Symptom> = emptySet(),
    val checkInSaved: Boolean = false,
    val selectedTab: HomeTab = HomeTab.Home,
    val message: String? = null,
    val insights: List<Insight> = SampleContent.insights,
    val articles: List<Article> = SampleContent.articles,
) {
    val cycleProgress: Float get() = (cycleDay.toFloat() / cycleLength).coerceIn(0f, 1f)
}

/** Sample content standing in for real data / the Figma copy. */
object SampleContent {
    val insights = listOf(
        Insight("📈", "Cycle regularity", "Your last 3 cycles varied by only 2 days.", RoseLight),
        Insight("🌙", "Sleep & energy", "You feel best after 7+ hours of sleep.", LavenderLight),
        Insight("💧", "Hydration", "You average 6 of 8 glasses a day this week.", MintLight),
    )

    val articles = listOf(
        Article("🥗", "Eating for insulin balance with PCOS", "5 min read · Nutrition", MintLight),
        Article("🧘", "Gentle workouts for high-cramp days", "4 min read · Movement", LavenderLight),
        Article("🩺", "Questions to ask at your next check-up", "6 min read · Care", PeachLight),
    )
}
