package com.mwarrc.pocketscore.ui.feature.settings.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * A reusable informational dialog for settings.
 *
 * @param title The dialog title.
 * @param description The detailed explanation text.
 * @param icon The icon to display at the top.
 * @param onDismiss Request to close the dialog.
 */
@Composable
fun SettingInfoDialog(
    title: String,
    description: String,
    icon: ImageVector,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(icon, null, tint = MaterialTheme.colorScheme.primary) },
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Got it", fontWeight = FontWeight.Bold)
            }
        },
        shape = RoundedCornerShape(28.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp
    )
}
