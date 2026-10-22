package com.mwarrc.pocketscore.ui.feature.settings.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * A horizontal divider for separating sections in the settings screen.
 *
 * @param alpha The opacity of the divider line.
 */
@Composable
fun SettingsDivider(alpha: Float = 1f) {
    HorizontalDivider(
        modifier = Modifier.padding(vertical = 12.dp),
        thickness = 1.dp,
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = alpha)
    )
}

