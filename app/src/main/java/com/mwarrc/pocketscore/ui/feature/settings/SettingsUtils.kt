package com.mwarrc.pocketscore.ui.feature.settings

import androidx.compose.ui.graphics.Color

/**
 * Returns the standard color for a pool ball based on its number.
 * 
 * @param number The ball number (1-15).
 * @return The [Color] associated with the ball.
 */
fun getBallColor(number: Int): Color = when (number) {
    1 -> Color(0xFFFFD700) 
    2 -> Color(0xFFC0C0C0) 
    3 -> Color(0xFFE91E63) 
    4 -> Color(0xFF9C27B0) 
    5 -> Color(0xFF673AB7) 
    6 -> Color(0xFF3F51B5) 
    7 -> Color(0xFF2196F3) 
    8 -> Color(0xFF1A1A1A) 
    9 -> Color(0xFF009688) 
    10 -> Color(0xFF4CAF50) 
    11 -> Color(0xFF8BC34A) 
    12 -> Color(0xFF8D9542)
    13 -> Color(0xFF6F6B47)
    14 -> Color(0xFFFF9800) 
    15 -> Color(0xFFFF5722) 
    else -> Color.Gray
}

/**
 * Data class representing the content of an information dialog in settings.
 */
data class InfoContent(
    val title: String,
    val description: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)
