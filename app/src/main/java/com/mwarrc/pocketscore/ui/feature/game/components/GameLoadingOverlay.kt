package com.mwarrc.pocketscore.ui.feature.game.components

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Minimal full-screen Material 3 loading overlay.
 *
 * One indeterminate spinner, one primary label, one muted sub-label.
 * Nothing else.
 *
 * @param visible     Whether the overlay is shown
 * @param message     Primary label (e.g. "Processing")
 * @param subMessage  Secondary muted label (e.g. "Applying changes")
 */
@Composable
fun GameLoadingOverlay(
    visible: Boolean,
    message: String? = null,
    subMessage: String? = null,
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(300)),
        exit  = fadeOut(tween(200)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.88f)),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(28.dp),
            ) {
                CircularProgressIndicator(
                    modifier   = Modifier.size(44.dp),
                    color      = MaterialTheme.colorScheme.onBackground,
                    trackColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.12f),
                    strokeWidth = 3.dp,
                    strokeCap   = StrokeCap.Round,
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text       = message ?: "Processing",
                        style      = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color      = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.87f),
                    )
                    Text(
                        text  = subMessage ?: "Applying changes",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.45f),
                    )
                }
            }
        }
    }
}