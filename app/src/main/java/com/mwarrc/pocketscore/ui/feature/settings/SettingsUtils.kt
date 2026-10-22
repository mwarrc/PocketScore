package com.mwarrc.pocketscore.ui.feature.settings

import androidx.compose.ui.graphics.Color
import com.mwarrc.pocketscore.ui.util.BallColors

/**
 * Returns the standard color for a pool ball based on its number.
 * 
 * @param number The ball number (1-15).
 * @return The [Color] associated with the ball.
 */
fun getBallColor(number: Int): Color = BallColors.getBallColor(number)

/**
 * Data class representing the content of an information dialog in settings.
 */
data class InfoContent(
    val title: String,
    val description: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)
