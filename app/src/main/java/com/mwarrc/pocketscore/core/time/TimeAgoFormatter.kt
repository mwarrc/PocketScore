package com.mwarrc.pocketscore.core.time

fun formatTimeAgo(timestamp: Long, now: Long = System.currentTimeMillis()): String {
    val diff = now - timestamp
    val seconds = diff / 1000
    val minutes = seconds / 60
    return when {
        seconds < 60 -> "Just now"
        minutes < 60 -> "$minutes min ago"
        minutes < 1440 -> "${minutes / 60} hr ago"
        else -> ">1 day ago"
    }
}

