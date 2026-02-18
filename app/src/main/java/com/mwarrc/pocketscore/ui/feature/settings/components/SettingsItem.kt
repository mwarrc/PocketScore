package com.mwarrc.pocketscore.ui.feature.settings.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

import androidx.compose.material3.ExperimentalMaterial3Api

/**
 * A standard list item for settings screens.
 *
 * @param title The main title text.
 * @param subtitle The smaller description text.
 * @param icon The icon to display on the left.
 * @param modifier Optional [Modifier] for the item.
 * @param onIconClick Optional callback when the icon itself is clicked.
 * @param onClick Optional callback when the entire item is clicked.
 * @param trailing A composable for the trailing content (e.g., a Toggle or Arrow).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onIconClick: (() -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    trailing: @Composable () -> Unit = { }
) {
    Surface(
        onClick = { onClick?.invoke() },
        enabled = onClick != null,
        color = Color.Transparent,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
        if (onIconClick != null) {
            Surface(
                onClick = onIconClick,
                color = Color.Transparent,
                shape = CircleShape,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
                }
            }
        } else {
            Icon(
                icon,
                null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        trailing()
        }
    }
}

