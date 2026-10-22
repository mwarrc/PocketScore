package com.mwarrc.pocketscore.ui.feature.home.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * A minimalistic row of navigation actions for primary app modules.
 *
 * Provides quick access to historical records and system configuration.
 * Shares its rendering with the active-match card so both states look identical.
 */
@Composable
fun NavigationCard(
    onNavigateToHistory: () -> Unit,
    onNavigateToSettings: () -> Unit,
    showSettingsBadge: Boolean = false,
    modifier: Modifier = Modifier
) {
    RecordsSettingsRow(
        onNavigateToHistory = onNavigateToHistory,
        onNavigateToSettings = onNavigateToSettings,
        showSettingsBadge = showSettingsBadge,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    )
}
