package com.bloom.app.data

/**
 * Sample history so the Insights charts have something to show. The app does not store
 * past days yet; replace these with real data once a history store exists.
 */
object DemoData {
    /** Daily goal completion (0-100) for Mon..Sun. Only days before today are used. */
    val weekScores: List<Int> = listOf(62, 78, 70, 66, 74, 58, 55)

    /** Average completion for the 3 weeks before the current one. */
    val previousWeekAverages: List<Int> = listOf(64, 71, 58)

    /** Severity index (0..3) for the 6 days before today, per symptom category. */
    val symptomHistory: Map<SymptomCategory, List<Int>> = mapOf(
        SymptomCategory.Bloating to listOf(2, 3, 2, 1, 2, 2),
        SymptomCategory.Skin to listOf(1, 1, 2, 2, 1, 0),
        SymptomCategory.Mood to listOf(0, 2, 1, 3, 1, 1),
        SymptomCategory.SleepQuality to listOf(1, 0, 2, 1, 2, 0),
    )
}
