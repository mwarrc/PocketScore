package com.mwarrc.pocketscore.ui.feature.history.import_.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Footer info card that explains the smart merge behaviour.
 *
 * Adapts its message based on whether any duplicates were detected:
 * - With duplicates: states how many matches will be skipped.
 * - Without duplicates: confirms all records are unique.
 *
 * @param duplicateCount Number of matches already present in the local database.
 */
@Composable
fun ImportSmartMergeCard(duplicateCount: Int) {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.AutoFixHigh,
                null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = if (duplicateCount > 0)
                    "Smart Merge: $duplicateCount duplicate ${if (duplicateCount == 1) "match" else "matches"} will be automatically skipped."
                else
                    "Smart Merge active: All records are unique and will be added.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
