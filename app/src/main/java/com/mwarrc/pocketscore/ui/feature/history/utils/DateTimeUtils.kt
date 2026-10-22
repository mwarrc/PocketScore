package com.mwarrc.pocketscore.ui.feature.history.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * Utility functions for date and time formatting in the history feature.
 */
object DateTimeUtils {
    
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    private val dateTimeFormat = SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.getDefault())
    
    /**
     * Formats a timestamp as time only (HH:mm).
     */
    fun formatTime(timestamp: Long): String {
        return timeFormat.format(Date(timestamp))
    }
    
    /**
     * Formats a timestamp as date only (MMM dd, yyyy).
     */
    fun formatDate(timestamp: Long): String {
        return dateFormat.format(Date(timestamp))
    }
    
    /**
     * Formats a timestamp as date and time (MMM dd, yyyy • HH:mm).
     */
    fun formatDateTime(timestamp: Long): String {
        return dateTimeFormat.format(Date(timestamp))
    }
    
    /**
     * Formats a duration in milliseconds as a human-readable string.
     * 
     * Examples:
     * - 45000ms -> "45s"
     * - 125000ms -> "2m 5s"
     * - 3725000ms -> "1h 2m"
     */
    fun formatDuration(durationMillis: Long): String {
        val hours = TimeUnit.MILLISECONDS.toHours(durationMillis)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(durationMillis) % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(durationMillis) % 60
        
        return buildString {
            if (hours > 0) {
                append("${hours}h")
                if (minutes > 0) append(" ${minutes}m")
            } else if (minutes > 0) {
                append("${minutes}m")
                if (seconds > 0) append(" ${seconds}s")
            } else {
                append("${seconds}s")
            }
        }
    }
    
    /**
     * Gets a relative time string (e.g., "Today", "Yesterday", "2 days ago").
     */
    fun getRelativeTimeString(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        val days = TimeUnit.MILLISECONDS.toDays(diff)
        
        return when {
            days == 0L -> "Today"
            days == 1L -> "Yesterday"
            days < 7 -> "$days days ago"
            days < 30 -> "${days / 7} weeks ago"
            days < 365 -> "${days / 30} months ago"
            else -> "${days / 365} years ago"
        }
    }
}
