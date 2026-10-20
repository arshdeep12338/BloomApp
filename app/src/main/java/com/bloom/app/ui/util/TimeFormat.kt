package com.bloom.app.ui.util

import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

fun formatMinutes(minutes: Int, pattern: String = "h:mm a"): String =
    LocalTime.of((minutes / 60).coerceIn(0, 23), (minutes % 60).coerceIn(0, 59))
        .format(DateTimeFormatter.ofPattern(pattern, Locale.getDefault()))
