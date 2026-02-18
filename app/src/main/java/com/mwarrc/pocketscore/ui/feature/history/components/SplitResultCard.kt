package com.mwarrc.pocketscore.ui.feature.history.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * A card displaying the total amount owed by a specific player.
 * 
 * @param name Player's name
 * @param amount Amount owed
 * @param matchCount Number of matches this player is paying for
 * @param currencySymbol Currency symbol to display
 */
@Composable
fun SplitResultCard(
    name: String,
    amount: Double,
    matchCount: Int,
    currencySymbol: String
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Player Initial Circle
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = name.firstOrNull()?.toString()?.uppercase() ?: "?", 
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            Spacer(Modifier.width(16.dp))
            
            // Player Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name, 
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold 
                )
                Text(
                    text = "Split across $matchCount matches",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Amount
            Text(
                text = "$currencySymbol ${String.format("%.0f", amount)}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
