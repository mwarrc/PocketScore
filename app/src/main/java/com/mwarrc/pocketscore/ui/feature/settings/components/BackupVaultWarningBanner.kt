package com.mwarrc.pocketscore.ui.feature.settings.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material3.ExperimentalMaterial3Api

/**
 * High-visibility warning banner displayed when a backup vault is not linked.
 * Acts as a critical call to action for users to protect their data.
 *
 * @param backupsFolderUri The currently linked folder URI (null if not linked).
 * @param onLinkBackupsFolder Callback to trigger the folder linking process.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupVaultWarningBanner(
    backupsFolderUri: String?,
    onLinkBackupsFolder: () -> Unit
) {
    AnimatedVisibility(
        visible = backupsFolderUri == null,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut()
    ) {
        Surface(
            onClick = onLinkBackupsFolder,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.9f),
            tonalElevation = 4.dp
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Warning,
                            null,
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.onError
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Backup Vault Unlinked",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Text(
                        "Your data is at risk. Connect a permanent folder to ensure records survive app uninstalls.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.85f)
                    )
                }
                Icon(
                    Icons.Default.PlayArrow,
                    null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.6f)
                )
            }
        }
    }
}
