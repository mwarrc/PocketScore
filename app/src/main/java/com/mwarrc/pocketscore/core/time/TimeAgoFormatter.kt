package com.mwarrc.pocketscore.core.time

import kotlin.math.abs

/**
 * Formats a timestamp as a human-readable "time ago" string.
 * 
 * Examples: "Just now", "5 min ago", "3 hr ago", "2 days ago"
 * 
 * @param timestamp The timestamp to format
 * @param now Current time (defaults to System.currentTimeMillis())
 * @return Human-readable time difference string
 */
fun formatTimeAgo(timestamp: Long, now: Long = System.currentTimeMillis()): String {
    val diff = now - timestamp
    
    // Handle future timestamps
    if (diff < 0) {
        return formatFutureTime(abs(diff))
    }
    
    val seconds = diff / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24
    
    return when {
        seconds < 10 -> "Just now"
        seconds < 60 -> "${seconds}s ago"
        minutes < 60 -> formatMinutes(minutes)
        hours < 24 -> formatHours(hours)
        days < 7 -> formatDays(days)
        days < 30 -> formatWeeks(days / 7)
        days < 365 -> formatMonths(days / 30)
        else -> formatYears(days / 365)
    }
}

/**
 * Formats minutes with proper singular/plural.
 */
private fun formatMinutes(minutes: Long): String {
    return if (minutes == 1L) "1 min ago" else "$minutes min ago"
}

/**
 * Formats hours with proper singular/plural.
 */
private fun formatHours(hours: Long): String {
    return if (hours == 1L) "1 hr ago" else "$hours hr ago"
}

/**
 * Formats days with proper singular/plural.
 */
private fun formatDays(days: Long): String {
    return if (days == 1L) "1 day ago" else "$days days ago"
}

/**
 * Formats weeks with proper singular/plural.
 */
private fun formatWeeks(weeks: Long): String {
    return if (weeks == 1L) "1 week ago" else "$weeks weeks ago"
}

/**
 * Formats months with proper singular/plural.
 */
private fun formatMonths(months: Long): String {
    return if (months == 1L) "1 month ago" else "$months months ago"
}

/**
 * Formats years with proper singular/plural.
 */
private fun formatYears(years: Long): String {
    return if (years == 1L) "1 year ago" else "$years years ago"
}

/**
 * Formats future timestamps.
 */
private fun formatFutureTime(diff: Long): String {
    val seconds = diff / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    
    return when {
        seconds < 60 -> "in ${seconds}s"
        minutes < 60 -> "in ${minutes} min"
        hours < 24 -> "in ${hours} hr"
        else -> "in ${hours / 24} days"
    }
}

/**
 * Formats a timestamp as a relative date string.
 * 
 * More detailed than formatTimeAgo, includes actual dates for older items.
 * Examples: "Today", "Yesterday", "Jan 15", "Dec 2023"
 * 
 * @param timestamp The timestamp to format
 * @param now Current time (defaults to System.currentTimeMillis())
 * @return Human-readable date string
 */
fun formatRelativeDate(timestamp: Long, now: Long = System.currentTimeMillis()): String {
    val diff = now - timestamp
    val hours = diff / (1000 * 60 * 60)
    val days = hours / 24
    
    return when {
        hours < 24 -> "Today"
        days < 2 -> "Yesterday"
        days < 7 -> "$days days ago"
        else -> {
            val calendar = java.util.Calendar.getInstance()
            calendar.timeInMillis = timestamp
            val month = calendar.getDisplayName(
                java.util.Calendar.MONTH,
                java.util.Calendar.SHORT,
                java.util.Locale.getDefault()
            )
            val day = calendar.get(java.util.Calendar.DAY_OF_MONTH)
            val year = calendar.get(java.util.Calendar.YEAR)
            val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
            
            if (year == currentYear) {
                "$month $day"
            } else {
                "$month $year"
            }
        }
    }
}