package com.mwarrc.pocketscore.ui.feature.home.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * A section for helpful tips.
 * 
 * Includes:
 * 1. **Pro Tips**: Informational cards to help users get the most out of PocketScore.
 * 
 * @param showTip Whether to display the "Pro Tip" card about player names.
 * @param onDismissTip Callback to permanently hide the Pro Tip card.
 * @param modifier Modifier for the section container.
 */
@Composable
fun HomePromoSection(
    showTip: Boolean,
    onDismissTip: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (showTip) {
        Column(
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            IdentityProTip(onDismissTip)
        }
    }
}

@Composable
private fun IdentityProTip(onDismiss: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f))
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .padding(end = 24.dp),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CloudSync, 
                    contentDescription = null, 
                    modifier = Modifier.size(20.dp).padding(top = 2.dp), 
                    tint = MaterialTheme.colorScheme.tertiary
                )
                Column {
                    Text(
                        text = "Pro Tip",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Use your unique 'Pro Name' for consistent match history and accurate stats across all your devices!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )
                }
            }
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.align(Alignment.TopEnd).padding(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Dismiss Tip",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
        }
    }
}
