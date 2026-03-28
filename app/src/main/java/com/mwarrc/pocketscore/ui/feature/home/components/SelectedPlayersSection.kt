package com.mwarrc.pocketscore.ui.feature.home.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * A compact section showing players currently selected for the match.
 * Essential when the full roster grid is hidden, so users can see and manage the active match list.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SelectedPlayersSection(
    selectedNames: List<String>,
    onRemoveName: (String) -> Unit,
    onSwap: (Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    if (selectedNames.isEmpty()) return

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Section Header
        Text(
            text = "Match Roster (${selectedNames.size})",
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            ),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
        )

        // Horizontal "Match Order" chips
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            selectedNames.forEachIndexed { index, name ->
                ActivePlayerChip(
                    name = name,
                    order = index + 1,
                    isFirst = index == 0,
                    isLast = index == selectedNames.size - 1,
                    onMoveBack = { onSwap(index, index - 1) },
                    onMoveForward = { onSwap(index, index + 1) },
                    onRemove = { onRemoveName(name) }
                )
            }
        }
    }
}

@Composable
private fun ActivePlayerChip(
    name: String,
    order: Int,
    isFirst: Boolean,
    isLast: Boolean,
    onMoveBack: () -> Unit,
    onMoveForward: () -> Unit,
    onRemove: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    
    Surface(
        shape = CircleShape,
        color = colorScheme.primaryContainer.copy(alpha = 0.35f),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            colorScheme.primary.copy(alpha = 0.12f)
        ),
        modifier = Modifier.height(44.dp)
    ) {
        Row(
            modifier = Modifier.padding(start = 6.dp, end = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Reorder Back
            if (!isFirst) {
                IconButton(onClick = onMoveBack, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Move earlier",
                        modifier = Modifier.size(14.dp),
                        tint = colorScheme.primary.copy(alpha = 0.6f)
                    )
                }
            } else {
                Spacer(Modifier.width(4.dp))
            }

            // Sequential Order Badge
            Surface(
                shape = CircleShape,
                color = colorScheme.primary,
                modifier = Modifier.size(26.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = order.toString(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black
                        ),
                        color = colorScheme.onPrimary
                    )
                }
            }

            Spacer(Modifier.width(8.dp))

            Text(
                text = name,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                ),
                color = colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.widthIn(max = 110.dp)
            )

            // Reorder Forward
            if (!isLast) {
                IconButton(onClick = onMoveForward, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Move later",
                        modifier = Modifier.size(14.dp),
                        tint = colorScheme.primary.copy(alpha = 0.6f)
                    )
                }
            }

            // Removal Action
            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Remove",
                    tint = colorScheme.error.copy(alpha = 0.5f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
