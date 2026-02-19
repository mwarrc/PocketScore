package com.mwarrc.pocketscore.ui.feature.game.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.mwarrc.pocketscore.domain.model.AppSettings

/**
 * A wrapper for the [ScoreNumpad] that handles its visibility and animations.
 * 
 * Only renders if [AppSettings.useCustomKeyboard] is enabled. Uses slide 
 * animations to enter and exit the bottom of the screen.
 * 
 * @param visible Targeted visibility state
 * @param scoreInput Current text buffer for the focused player's entry
 * @param onScoreInputChange Callback when numeric keys update the buffer
 * @param isPinned Whether the numpad should remain visible after entry
 * @param onTogglePin Callback to change the pinning state
 * @param onDismiss Callback to request closing the overlay
 * @param settings Current application configuration for themes/haptics
 * @param onUpdateSettings Callback to modify settings from the numpad
 */
@Composable
fun NumpadOverlay(
    visible: Boolean,
    scoreInput: String,
    onScoreInputChange: (String) -> Unit,
    isPinned: Boolean,
    onTogglePin: () -> Unit,
    onDismiss: () -> Unit,
    settings: AppSettings,
    onUpdateSettings: ((AppSettings) -> AppSettings) -> Unit
) {
    if (settings.useCustomKeyboard) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            AnimatedVisibility(
                visible = visible,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
                ScoreNumpad(
                    onNumberClick = { num ->
                        onScoreInputChange(scoreInput + num)
                    },
                    onBackspaceClick = {
                        if (scoreInput.isNotEmpty()) onScoreInputChange(scoreInput.dropLast(1))
                    },
                    onDismiss = onDismiss,
                    isPinned = isPinned,
                    onTogglePin = onTogglePin,
                    settings = settings,
                    onUpdateSettings = onUpdateSettings
                )
            }
        }
    }
}
