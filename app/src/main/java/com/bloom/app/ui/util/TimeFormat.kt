package com.bloom.app.ui.util

import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Formats minutes-after-midnight, e.g. 450 -> "7:30 AM" (pattern "h:mm a"). */
fun formatMinutes(minutes: Int, pattern: String = "h:mm a"): String =
    LocalTime.of((minutes / 60).coerceIn(0, 23), (minutes % 60).coerceIn(0, 59))
        .format(DateTimeFormatter.ofPattern(pattern, Locale.getDefault()))
