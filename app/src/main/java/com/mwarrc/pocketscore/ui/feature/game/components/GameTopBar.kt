package com.mwarrc.pocketscore.ui.feature.game.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mwarrc.pocketscore.R
import com.mwarrc.pocketscore.domain.model.AppSettings

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.getValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color

/**
 * The primary top navigation bar for the game screen.
 * 
 * Shows the session control (End Game), undo action, and 
 * current table state (points remaining) if pool management is enabled.
 * 
 * @param settings Current application settings
 * @param tableSum Sum of points remaining on the table
 * @param canUndo Whether an action is available to undo
 * @param lastAutoRemovedBall The number of the last automatically removed ball, if any. -1 indicates 'No fit'
 * @param onEndSession Callback to trigger match ending flow
 * @param onUndo Callback to revert last action
 * @param onQuickBalls Callback to open ball selection overlay
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameTopBar(
    settings: AppSettings,
    tableSum: Int,
    canUndo: Boolean,
    lastAutoRemovedBall: Int?,
    onEndSession: () -> Unit,
    onUndo: () -> Unit,
    onQuickBalls: () -> Unit
) {
    CenterAlignedTopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (!settings.poolBallManagementEnabled) {
                    Text(
                        "Scoreboard",
                        style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp),
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        softWrap = false
                    )
                    Spacer(Modifier.width(8.dp))
                }
                
                AnimatedVisibility(
                    visible = settings.poolBallManagementEnabled,
                    enter = fadeIn() + scaleIn(),
                    exit = fadeOut() + scaleOut()
                ) {
                    val targetBgColor = if (lastAutoRemovedBall != null) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.primaryContainer
                    val targetContentColor = if (lastAutoRemovedBall != null) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onPrimaryContainer
                    
                    val bgColor by animateColorAsState(targetBgColor, label = "bgColor")
                    val contentColor by animateColorAsState(targetContentColor, label = "contentColor")

                    Surface(
                        onClick = onQuickBalls,
                        shape = RoundedCornerShape(50), // Fully rounded pill
                        color = bgColor,
                        contentColor = contentColor,
                        modifier = Modifier
                            .height(50.dp) // Bigger touch target
                            .widthIn(min = 140.dp), // Wider for presence
                        tonalElevation = 6.dp, // Higher elevation for "glossy"/pop effect
                        shadowElevation = 4.dp,
                        border = BorderStroke(1.dp, contentColor.copy(alpha = 0.1f))
                    ) {
                        AnimatedContent(
                            targetState = lastAutoRemovedBall, 
                            label = "BallStateAnimation",
                            transitionSpec = {
                                (fadeIn(animationSpec = tween(600)) + scaleIn(initialScale = 0.8f)) togetherWith
                                    (fadeOut(animationSpec = tween(600)) + scaleOut(targetScale = 0.8f))
                            }
                        ) { removedBall ->
                            if (removedBall != null) {
                                if (removedBall == -1) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "No match",
                                            tint = contentColor,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            "No matching ball",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = contentColor
                                        )
                                    }
                                } else {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(CircleShape)
                                                .background(com.mwarrc.pocketscore.ui.feature.settings.getBallColor(removedBall)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(14.dp)
                                                    .clip(CircleShape)
                                                    .background(Color.White),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = removedBall.toString(),
                                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.Black
                                                )
                                            }
                                        }
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            "Ball $removedBall Removed",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = contentColor
                                        )
                                    }
                                }
                            } else {
                                Row(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_pool_balls),
                                        contentDescription = "Quick Balls",
                                        tint = contentColor,
                                        modifier = Modifier.size(24.dp) // Slightly bigger icon
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        "$tableSum pts",
                                        style = MaterialTheme.typography.titleMedium, // Larger text
                                        fontWeight = FontWeight.Black,
                                        color = contentColor
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        navigationIcon = {
            TextButton(
                onClick = onEndSession,
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Close,
                        "End Session",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("End Game", style = MaterialTheme.typography.labelLarge)
                }
            }
        },
        actions = {
            TextButton(
                onClick = onUndo,
                enabled = canUndo,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary,
                    disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                )
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.AutoMirrored.Filled.Undo,
                        "Undo Last",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("Undo", style = MaterialTheme.typography.labelLarge)
                }
            }
        },
        windowInsets = TopAppBarDefaults.windowInsets,
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
        )
    )
}
