package com.mwarrc.pocketscore.ui.feature.game.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Dp
import com.mwarrc.pocketscore.ui.util.BallColors

/**
 * The primary top navigation bar for the game screen.
 * 
 * Shows the session control (End Game), undo action, and 
 * current table state (points remaining) if pool management is enabled.
 * 
 * @param settings Current application settings
 * @param tableSum Sum of points remaining on the table
 * @param ballsRemaining Count of balls still on the table, used for the endgame counter
 * @param canUndo Whether an action is available to undo
 * @param lastAutoRemovedBall The number of the last automatically removed ball, if any.
 * @param eliminationPulse Whether to run a "check the table" attention pulse on the middle pill
 * @param onEliminationPulseConsumed Callback invoked once the pulse has been acknowledged by a tap
 * @param onEndSession Callback to trigger match ending flow
 * @param onUndo Callback to revert last action
 * @param onQuickBalls Callback to open ball selection overlay
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameTopBar(
    settings: AppSettings,
    tableSum: Int,
    ballsRemaining: Int,
    canUndo: Boolean,
    lastAutoRemovedBall: Int?,
    eliminationPulse: Boolean = false,
    onEliminationPulseConsumed: () -> Unit = {},
    onEndSession: () -> Unit,
    onUndo: () -> Unit,
    onQuickBalls: () -> Unit
) {
    val haptic = LocalHapticFeedback.current

    // 0f = idle, 1f = peak of the pulse. Drives scale + glow.
    val pulse = remember { Animatable(0f) }

    LaunchedEffect(eliminationPulse) {
        if (eliminationPulse) {
            // Medium haptic so the reminder registers even if the screen isn't watched.
            if (settings.hapticFeedbackEnabled) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            }
            // Two restrained breaths, then rest.
            repeat(2) {
                pulse.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 380, easing = LinearOutSlowInEasing)
                )
                pulse.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(durationMillis = 480, easing = FastOutSlowInEasing)
                )
            }
        } else {
            pulse.snapTo(0f)
        }
    }

    // Gentle scale - kept subtle so the bar doesn't feel jumpy.
    val pulseScale = 1f + (pulse.value * 0.06f)
    val glowAlpha = pulse.value * 0.55f
    val glowColor = MaterialTheme.colorScheme.error

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
                        onClick = {
                            if (eliminationPulse) onEliminationPulseConsumed()
                            onQuickBalls()
                        },
                        shape = RoundedCornerShape(28.dp),
                        color = bgColor,
                        contentColor = contentColor,
                        modifier = Modifier
                            .graphicsLayer {
                                scaleX = pulseScale
                                scaleY = pulseScale
                            }
                            .drawBehind {
                                if (glowAlpha > 0.01f) {
                                    drawRoundRect(
                                        color = glowColor.copy(alpha = glowAlpha),
                                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(
                                            size.height / 2f
                                        ),
                                        style = androidx.compose.ui.graphics.drawscope.Stroke(
                                            width = 3.dp.toPx()
                                        )
                                    )
                                }
                            }
                            .height(50.dp)
                            .widthIn(min = 140.dp),
                        tonalElevation = 6.dp,
                        shadowElevation = 4.dp
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
                                Row(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .padding(horizontal = 18.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    TopBarBall(number = removedBall, size = 30.dp)
                                    Spacer(Modifier.width(10.dp))
                                    Text(
                                        "Ball $removedBall",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Black,
                                        color = contentColor
                                    )
                                }
                            } else {
                                Row(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .padding(horizontal = 18.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_pool_balls),
                                        contentDescription = "Quick Balls",
                                        tint = contentColor,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(Modifier.width(10.dp))
                                    Text(
                                        "$tableSum pts",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Black,
                                        color = contentColor
                                    )

                                    // Low-ball counter: only bothers the player in the
                                    // endgame, when the remaining count actually matters.
                                    if (ballsRemaining in 1..5) {
                                        Spacer(Modifier.width(8.dp))
                                        LowBallCountBadge(
                                            count = ballsRemaining,
                                            contentColor = contentColor
                                        )
                                    }
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

/**
 * Minimal pool ball used inside the top bar pill.
 *
 * Solids are a flat colour with the number. Stripes (9–15) stay fully coloured and
 * only get two very thin white slivers - one near the top, one near the bottom -
 * so the stripe reads at a glance without becoming a heavy band.
 */
@Composable
private fun TopBarBall(
    number: Int,
    size: Dp,
    modifier: Modifier = Modifier
) {
    val ballColor = BallColors.getBallColor(number)
    val isEight = number == 8
    val needsDarkText = number == 1

    Surface(
        modifier = modifier.size(size),
        shape = CircleShape,
        color = if (isEight) Color(0xFF151515) else ballColor,
        border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.12f)),
        shadowElevation = 2.dp
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            // Soft specular so it reads as a ball, not a flat disc.
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(Color.White.copy(alpha = 0.32f), Color.Transparent),
                            center = androidx.compose.ui.geometry.Offset(
                                size.value * 0.3f,
                                size.value * 0.28f
                            )
                        )
                    )
            )

            if (isEight) {
                Box(
                    modifier = Modifier
                        .size(size * 0.6f)
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = number.toString(),
                        fontSize = (size.value * 0.3f).sp,
                        fontWeight = FontWeight.Black,
                        color = if (isEight) Color(0xFF151515) else ballColor
                    )
                }
            } else {
                Text(
                    text = number.toString(),
                    fontSize = (size.value * 0.44f).sp,
                    fontWeight = FontWeight.Black,
                    color = if (needsDarkText) Color.Black.copy(alpha = 0.85f) else Color.White,
                    style = MaterialTheme.typography.titleMedium.copy(
                        shadow = if (!needsDarkText) Shadow(
                            color = Color.Black.copy(alpha = 0.45f),
                            offset = androidx.compose.ui.geometry.Offset(1.5f, 1.5f),
                            blurRadius = 3f
                        ) else null
                    )
                )
            }
        }
    }
}

/**
 * Small endgame counter shown only while the table is nearly cleared.
 *
 * Pops in with a gentle scale so it draws the eye exactly when the remaining
 * count starts to matter, without ever nagging during normal play.
 */
@Composable
private fun LowBallCountBadge(
    count: Int,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    AnimatedContent(
        targetState = count,
        transitionSpec = {
            (fadeIn(animationSpec = tween(220)) + scaleIn(initialScale = 0.7f)) togetherWith
                (fadeOut(animationSpec = tween(160)) + scaleOut(targetScale = 0.7f))
        },
        label = "LowBallCount",
        modifier = modifier
    ) { value ->
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(contentColor.copy(alpha = 0.18f))
                .border(1.dp, contentColor.copy(alpha = 0.4f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Black,
                color = contentColor
            )
        }
    }
}
