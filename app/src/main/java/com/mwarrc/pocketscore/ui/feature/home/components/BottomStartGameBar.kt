package com.mwarrc.pocketscore.ui.feature.home.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Fully floating bottom capsule. Stays fixed — does NOT move with keyboard.
 * User must dismiss keyboard before starting the game.
 */
@Composable
fun BottomStartGameBar(
    selectedCount: Int,
    onStartGame: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    val isEnabled = selectedCount >= 2

    // Fully transparent wrapper — no background bar whatsoever
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 28.dp, vertical = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        AnimatedContent(
            targetState = isEnabled,
            transitionSpec = {
                fadeIn(tween(220)) + scaleIn(tween(220), initialScale = 0.95f) togetherWith
                        fadeOut(tween(150)) + scaleOut(tween(150), targetScale = 0.95f)
            },
            label = "bar_state"
        ) { enabled ->
            if (enabled) {
                // ── READY ──
                Surface(
                    onClick = onStartGame,
                    shape = CircleShape,
                    color = colorScheme.primary,
                    shadowElevation = 18.dp,
                    modifier = Modifier
                        .fillMaxWidth(0.88f)
                        .height(56.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = colorScheme.onPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Start Match",
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp,
                            letterSpacing = 0.5.sp,
                            color = colorScheme.onPrimary
                        )
                        Spacer(Modifier.width(10.dp))
                        Surface(
                            shape = CircleShape,
                            color = colorScheme.onPrimary.copy(alpha = 0.18f)
                        ) {
                            AnimatedContent(
                                targetState = selectedCount,
                                transitionSpec = {
                                    if (targetState > initialState)
                                        slideInVertically { it } + fadeIn() togetherWith
                                                slideOutVertically { -it } + fadeOut()
                                    else
                                        slideInVertically { -it } + fadeIn() togetherWith
                                                slideOutVertically { it } + fadeOut()
                                },
                                label = "count"
                            ) { count ->
                                Text(
                                    text = "$count",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 13.sp,
                                    color = colorScheme.onPrimary,
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            } else {
                // ── WAITING --─────
                Surface(
                    shape = CircleShape,
                    color = colorScheme.surfaceContainerHighest.copy(alpha = 0.85f),
                    shadowElevation = 3.dp,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        colorScheme.outlineVariant.copy(alpha = 0.2f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth(0.65f)
                        .height(46.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = if (selectedCount == 0) "Select players to begin"
                            else "Select ${2 - selectedCount} more player",
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp,
                            color = colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    }
}
