package com.mwarrc.pocketscore.ui.feature.history.components

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Data representation for a navigatable tab in the history screen.
 */
data class HistoryTab(
    val title: String,
    val icon: ImageVector
)

/**
 * Material 3 expressive NavigationBar for the History screen.
 *
 * Always shows labels, uses M3 NavigationBar with animated indicator pill,
 * and supports hiding during bulk selection mode.
 *
 * @param tabs List of tabs to display
 * @param selectedIndex Currently active tab index
 * @param onTabClick Callback when a tab is selected
 * @param isVisible Controls visibility — hidden during selection mode
 */
@Composable
fun FloatingHistoryNavBar(
    tabs: List<HistoryTab>,
    selectedIndex: Int,
    onTabClick: (Int) -> Unit,
    isVisible: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
        exit = fadeOut() + slideOutVertically(targetOffsetY = { it }),
        modifier = modifier.fillMaxWidth()
    ) {
        NavigationBar(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            tonalElevation = 0.dp,
        ) {
            tabs.forEachIndexed { index, tab ->
                val selected = selectedIndex == index
                NavigationBarItem(
                    selected = selected,
                    onClick = { onTabClick(index) },
                    icon = {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = tab.title
                        )
                    },
                    label = {
                        Text(
                            text = tab.title,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    alwaysShowLabel = true,
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        selectedTextColor = MaterialTheme.colorScheme.onSurface,
                        indicatorColor = MaterialTheme.colorScheme.secondaryContainer,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }
    }
}
