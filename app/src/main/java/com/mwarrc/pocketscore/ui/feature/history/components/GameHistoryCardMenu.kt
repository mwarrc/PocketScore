package com.mwarrc.pocketscore.ui.feature.history.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.mwarrc.pocketscore.domain.model.GameState

/**
 * Dropdown menu for game history card actions.
 * 
 * Provides options to:
 * - Archive/Unarchive the match
 * - Share the match record
 * - Delete the match permanently
 * 
 * @param expanded Whether the menu is currently visible
 * @param isArchived Whether the game is currently archived
 * @param onDismiss Callback when menu should be dismissed
 * @param onArchive Callback to toggle archive status
 * @param onShare Callback to share the match
 * @param onDelete Callback to delete the match
 */
@Composable
fun GameHistoryCardMenu(
    expanded: Boolean,
    isArchived: Boolean,
    onDismiss: () -> Unit,
    onArchive: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit,
    onSelect: () -> Unit
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        offset = DpOffset((-8).dp, 0.dp),
        modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        DropdownMenuItem(
            text = { Text("Select") },
            leadingIcon = { Icon(Icons.Default.CheckBox, contentDescription = null) },
            onClick = {
                onSelect()
                onDismiss()
            }
        )
        
        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

        DropdownMenuItem(
            text = { Text(if (isArchived) "Unarchive Match" else "Archive Match") },
            leadingIcon = { 
                Icon(
                    if (isArchived) Icons.Default.Unarchive else Icons.Default.Archive, 
                    contentDescription = null
                ) 
            },
            onClick = {
                onArchive()
                onDismiss()
            }
        )
        
        DropdownMenuItem(
            text = { Text("Share Record") },
            leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) },
            onClick = {
                onShare()
                onDismiss()
            }
        )
        
        HorizontalDivider(
            modifier = Modifier.padding(vertical = 4.dp),
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
        )
        
        DropdownMenuItem(
            text = { 
                Text(
                    "Delete Permanently", 
                    color = MaterialTheme.colorScheme.error
                ) 
            },
            leadingIcon = { 
                Icon(
                    Icons.Default.DeleteForever, 
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                ) 
            },
            onClick = {
                onDelete()
                onDismiss()
            }
        )
    }
}

/**
 * Menu button that triggers the game history card actions menu.
 * 
 * @param onMenuToggle Callback when menu button is clicked
 */
@Composable
fun GameHistoryCardMenuButton(
    onMenuToggle: () -> Unit
) {
    IconButton(onClick = onMenuToggle) {
        Icon(
            Icons.Default.MoreVert,
            contentDescription = "Options",
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
