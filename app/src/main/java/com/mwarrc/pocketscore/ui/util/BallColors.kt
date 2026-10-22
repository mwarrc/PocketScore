package com.mwarrc.pocketscore.ui.util

import androidx.compose.ui.graphics.Color

/**
 * Standard colors for pool balls (1-15) as used in official game sets.
 */
object BallColors {
    // Solid colors
    val Yellow = Color(0xFFFFD600)  // Bright Yellow
    val Blue = Color(0xFF0D47A1)    // Primary Blue
    val Red = Color(0xFFB71C1C)     // Deep Red
    val Purple = Color(0xFF6A1B9A)  // Rich Purple
    val Orange = Color(0xFFE65100)  // Vivid Orange
    val Green = Color(0xFF2E7D32)   // Grass Green
    val Maroon = Color(0xFF800000)  // Deep Maroon
    val Black = Color(0xFF111111)   // Slate Black
    val White = Color(0xFFFFFFFF)   // Pure White (Cue)

    /**
     * Map a ball number to its standard color.
     * 
     * Handles both solids (1-7) and stripes (9-15) where colors repeat.
     */
    fun getBallColor(number: Int): Color = when (number) {
        1, 9 -> Yellow
        2, 10 -> Blue
        3, 11 -> Red
        4, 12 -> Purple
        5, 13 -> Orange
        6, 14 -> Green
        7, 15 -> Maroon
        8 -> Black
        0 -> White // 0 represents the Cue ball in some contexts
        else -> Color.Gray
    }
}
