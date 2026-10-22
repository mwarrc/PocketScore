package com.mwarrc.pocketscore.ui.feature.history.components

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
 * Designed with a sleek floating island aesthetic:
 *  - Elevated rounded pill dock with tonal depth and soft shadow.
 *  - Animated active tab pill with spring bounce micro-interactions on icons.
 *  - Haptic feedback on tab changes for tactile feel.
 *  - Borderless, clean Material 3 design conforming to PocketScore aesthetics.
 *  - Seamlessly pairs with [MatchSelectionBar] during bulk selection mode.
 *
 * @param tabs List of tabs to display
 * @param selectedIndex Currently active tab index
 * @param onTabClick Callback when a tab is selected
 * @param isVisible Controls visibility — animated enter/exit
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
        enter = fadeIn(animationSpec = tween(220)) + slideInVertically(
            animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow)
        ) { it },
        exit = fadeOut(animationSpec = tween(160)) + slideOutVertically(
            animationSpec = tween(160)
        ) { it },
        modifier = modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(66.dp),
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.96f),
                tonalElevation = 6.dp,
                shadowElevation = 10.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 6.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    tabs.forEachIndexed { index, tab ->
                        val isSelected = selectedIndex == index

                        val iconScale by animateFloatAsState(
                            targetValue = if (isSelected) 1.15f else 1.0f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            ),
                            label = "tab_scale_$index"
                        )

                        val bgColor by animateColorAsState(
                            targetValue = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                          else Color.Transparent,
                            animationSpec = tween(200),
                            label = "tab_bg_$index"
                        )

                        val iconTint by animateColorAsState(
                            targetValue = if (isSelected) MaterialTheme.colorScheme.primary
                                          else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                            animationSpec = tween(180),
                            label = "tab_icon_$index"
                        )

                        val textColor by animateColorAsState(
                            targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                                          else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                            animationSpec = tween(180),
                            label = "tab_text_$index"
                        )

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(22.dp))
                                .background(bgColor)
                                .clickable {
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
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Box(contentAlignment = Alignment.TopEnd) {
                                    Icon(
                                        imageVector = tab.icon,
                                        contentDescription = tab.title,
                                        tint = iconTint,
                                        modifier = Modifier
                                            .size(22.dp)
                                            .graphicsLayer {
                                                scaleX = iconScale
                                                scaleY = iconScale
                                            }
                                    )
                                    if (tab.badgeCount != null && tab.badgeCount > 0) {
                                        Box(
                                            modifier = Modifier
                                                .offset(x = 6.dp, y = (-4).dp)
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.error)
                                        )
                                    }
                                }

                                Spacer(Modifier.height(3.dp))

                                Text(
                                    text = tab.title,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = textColor,
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
