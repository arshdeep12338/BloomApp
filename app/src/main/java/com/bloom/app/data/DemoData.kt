package com.bloom.app.data

object DemoData {
    val weekScores: List<Int> = listOf(62, 78, 70, 66, 74, 58, 55)
    val previousWeekAverages: List<Int> = listOf(64, 71, 58)
    val symptomHistory: Map<SymptomCategory, List<Int>> = mapOf(
        SymptomCategory.Bloating to listOf(2, 3, 2, 1, 2, 2),
        SymptomCategory.Skin to listOf(1, 1, 2, 2, 1, 0),
        SymptomCategory.Mood to listOf(0, 2, 1, 3, 1, 1),
        SymptomCategory.SleepQuality to listOf(1, 0, 2, 1, 2, 0),
    )
}
