package com.numan.wcmp.core.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Utility functions for timestamps and telemetry formatting.
 */
object TimeFormatter {
    private val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    private val fullDateTimeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    fun formatTime(timestamp: Long): String {
        if (timestamp <= 0) return "--:--:--"
        return timeFormat.format(Date(timestamp))
    }

    fun formatFullDateTime(timestamp: Long): String {
        if (timestamp <= 0) return "N/A"
        return fullDateTimeFormat.format(Date(timestamp))
    }
}
