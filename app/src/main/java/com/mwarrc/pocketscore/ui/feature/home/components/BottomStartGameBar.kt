package com.mwarrc.pocketscore.ui.feature.home.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * A ultra-premium bottom bar for starting a new match.
 * Smoothly transforms between 'Waiting' and 'Ready' states with fluid animations.
 */
@Composable
fun BottomStartGameBar(
    selectedCount: Int,
    onStartGame: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    val isEnabled = selectedCount >= 2
    
    // Animate color, width, and height with cohesive easing
    val containerColor by animateColorAsState(
        targetValue = if (isEnabled) colorScheme.primary else colorScheme.surfaceContainerHigh.copy(alpha = 0.85f),
        animationSpec = tween(450, easing = FastOutSlowInEasing),
        label = "color"
    )
    
    val contentColor by animateColorAsState(
        targetValue = if (isEnabled) colorScheme.onPrimary else colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
        animationSpec = tween(400),
        label = "content_color"
    )

    val shadowElevation by animateFloatAsState(
        targetValue = if (isEnabled) 12f else 2f,
        animationSpec = tween(400),
        label = "shadow"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        // High-quality shadow layer decoupled from content to prevent "edgy" artifacts
        Surface(
            onClick = { if (isEnabled) onStartGame() },
            enabled = isEnabled,
            shape = CircleShape,
            color = containerColor,
            modifier = Modifier
                // Fixed width and height: the bar used to grow and shrink with the
                // selection count, which made the whole bottom of the screen jitter
                // on every tap. A stable shape reads as one consistent control.
                .fillMaxWidth()
                .height(56.dp)
                .graphicsLayer {
                    this.shadowElevation = shadowElevation
                    shape = CircleShape
                    clip = false
                }
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.animation.AnimatedContent(
                    targetState = isEnabled,
                    transitionSpec = {
                        (fadeIn(tween(350, 100)) + scaleIn(initialScale = 0.94f))
                            .togetherWith(fadeOut(tween(250)))
                    },
                    label = "content_swipe"
                ) { ready ->
                    if (ready) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        ) {
                            Icon(
                                Icons.Default.PlayArrow,
                                null,
                                tint = contentColor,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(
                                "Start Match",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 0.4.sp
                                ),
                                color = contentColor
                            )
                            Spacer(Modifier.width(12.dp))

                            Surface(
                                shape = CircleShape,
                                color = colorScheme.onPrimary.copy(alpha = 0.18f),
                                modifier = Modifier.size(28.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    androidx.compose.animation.AnimatedContent(
                                        targetState = selectedCount,
                                        transitionSpec = {
                                            (slideInVertically { it } + fadeIn()).togetherWith(slideOutVertically { -it } + fadeOut())
                                        },
                                        label = "count_val"
                                    ) { count ->
                                        Text(
                                            "$count",
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = contentColor
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        // "Waiting" state content
                        val remaining = 2 - selectedCount
                        Text(
                            text = if (selectedCount == 0) "Select players to begin"
                                   else "Add $remaining more player${if (remaining > 1) "s" else ""}",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = contentColor,
                            modifier = Modifier.padding(horizontal = 20.dp)
                        )
                    }
                }
            }
        }
    }
}
