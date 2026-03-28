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

    val widthPercent by animateFloatAsState(
        targetValue = if (isEnabled) 0.94f else 0.76f,
        animationSpec = spring(dampingRatio = 0.72f, stiffness = Spring.StiffnessLow),
        label = "width"
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
                .fillMaxWidth(widthPercent)
                .height(if (isEnabled) 58.dp else 48.dp)
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
                                    fontSize = 16.sp,
                                    letterSpacing = 0.4.sp
                                ),
                                color = contentColor
                            )
                            Spacer(Modifier.width(12.dp))
                            
                            // Pulse animation for the count badge
                            val badgeScale by animateFloatAsState(
                                targetValue = 1f,
                                animationSpec = spring(dampingRatio = 0.5f, stiffness = Spring.StiffnessMedium),
                                label = "badge_pop"
                            )
                            
                            Surface(
                                shape = CircleShape,
                                color = colorScheme.onPrimary.copy(alpha = 0.18f),
                                modifier = Modifier
                                    .size(28.dp)
                                    .graphicsLayer { scaleX = badgeScale; scaleY = badgeScale }
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
