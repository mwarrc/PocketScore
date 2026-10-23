package com.mwarrc.pocketscore.ui.feature.history.components

import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/**
 * Data representation for a navigatable tab in the history screen.
 */
data class HistoryTab(
    val title: String,
    val icon: ImageVector,
    val badgeCount: Int? = null
)

/**
 * Modern floating capsule navigation bar for the History / Records screen.
 *
 * Features:
 *  - Floating island dock aesthetic that floats gracefully over scrolling records.
 *  - Silky-smooth sliding indicator pill that glides seamlessly across tabs without layout jumping.
 *  - Instant responsive micro-interactions with haptic tactile feedback.
 *  - Glassmorphic translucent surface container with soft depth shadow.
 *
 * @param tabs List of tabs to display
 * @param selectedIndex Currently active tab index
 * @param onTabClick Callback when a tab is selected
 * @param isVisible Controls visibility - animated enter/exit
 * @param modifier Modifier applied to the outer container
 */
@Composable
fun FloatingHistoryNavBar(
    tabs: List<HistoryTab>,
    selectedIndex: Int,
    onTabClick: (Int) -> Unit,
    isVisible: Boolean = true,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(animationSpec = tween(220, easing = LinearOutSlowInEasing)) + scaleIn(
            initialScale = 0.94f,
            animationSpec = tween(220, easing = LinearOutSlowInEasing)
        ),
        exit = fadeOut(animationSpec = tween(160, easing = FastOutLinearInEasing)) + scaleOut(
            targetScale = 0.94f,
            animationSpec = tween(160, easing = FastOutLinearInEasing)
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 6.dp, bottom = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .graphicsLayer {
                        this.shadowElevation = 8.dp.toPx()
                        this.shape = CircleShape
                        this.clip = false
                        this.ambientShadowColor = Color.Black.copy(alpha = 0.20f)
                        this.spotShadowColor = Color.Black.copy(alpha = 0.30f)
                    },
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                border = BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                ),
                tonalElevation = 3.dp,
                shadowElevation = 0.dp
            ) {
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(all = 5.dp)
                ) {
                    val tabCount = tabs.size.coerceAtLeast(1)
                    val tabWidth = maxWidth / tabCount
                    val safeIndex = selectedIndex.coerceIn(0, tabCount - 1)

                    val indicatorOffset by animateDpAsState(
                        targetValue = tabWidth * safeIndex,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        ),
                        label = "active_tab_indicator_offset"
                    )

                    // Sliding Active Pill Background
                    Box(
                        modifier = Modifier
                            .offset(x = indicatorOffset)
                            .width(tabWidth)
                            .fillMaxHeight()
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                    )

                    // Tab Items
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        tabs.forEachIndexed { index, tab ->
                            val isSelected = selectedIndex == index

                            val iconScale by animateFloatAsState(
                                targetValue = if (isSelected) 1.12f else 1.0f,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioNoBouncy,
                                    stiffness = Spring.StiffnessMedium
                                ),
                                label = "tab_icon_scale_$index"
                            )

                            val contentColor by animateColorAsState(
                                targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                                              else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                animationSpec = tween(180),
                                label = "tab_content_color_$index"
                            )

                            val iconColor by animateColorAsState(
                                targetValue = if (isSelected) MaterialTheme.colorScheme.primary
                                              else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                animationSpec = tween(180),
                                label = "tab_icon_color_$index"
                            )

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clip(CircleShape)
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) {
                                        if (!isSelected) {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        }
                                        onTabClick(index)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center,
                                    modifier = Modifier.padding(horizontal = 2.dp)
                                ) {
                                    Box(contentAlignment = Alignment.TopEnd) {
                                        Icon(
                                            imageVector = tab.icon,
                                            contentDescription = tab.title,
                                            tint = iconColor,
                                            modifier = Modifier
                                                .size(20.dp)
                                                .graphicsLayer {
                                                    scaleX = iconScale
                                                    scaleY = iconScale
                                                }
                                        )
                                        if (tab.badgeCount != null && tab.badgeCount > 0) {
                                            Box(
                                                modifier = Modifier
                                                    .offset(x = 6.dp, y = (-3).dp)
                                                    .size(7.dp)
                                                    .clip(CircleShape)
                                                    .background(MaterialTheme.colorScheme.error)
                                            )
                                        }
                                    }

                                    Spacer(Modifier.height(2.dp))

                                    Text(
                                        text = tab.title,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = contentColor,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
