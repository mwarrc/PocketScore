package com.mwarrc.pocketscore.ui.feature.game.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * A navigation item for the [GameBottomBar].
 * 
 * Renders an icon with an optional notification badge and a label.
 * 
 * @param icon The image vector for the item's icon
 * @param label The text label displayed below the icon
 * @param hasBadge Whether to show a notification/alert badge on the icon
 * @param onClick Callback when the item is tapped
 */
@Composable
fun BottomNavItem(
    icon: ImageVector,
    label: String,
    hasBadge: Boolean = false,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(8.dp)
    ) {
        Box {
            Icon(icon, contentDescription = label, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            if (hasBadge) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 0.dp, end = 0.dp)
                        .size(14.dp)
                        .background(
                            MaterialTheme.colorScheme.error,
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "!",
                        color = MaterialTheme.colorScheme.onError,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
    }
}

