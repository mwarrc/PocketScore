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
                        if (scoreInput.length < 5) onScoreInputChange(scoreInput + num)
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
