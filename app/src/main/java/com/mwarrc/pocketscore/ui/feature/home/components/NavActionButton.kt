package com.mwarrc.pocketscore.ui.feature.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Shared secondary action button used by the home screen's navigation surfaces.
 *
 * Rendered identically whether the user is mid-match or idle, so Records and
 * Settings never change appearance based on unrelated state.
 *
 * @param title Button label
 * @param icon Leading icon
 * @param onClick Click handler
 * @param showBadge Draws a small attention dot on the icon
 * @param containerColor Background; pass transparent for a flat look
 * @param modifier Modifier for sizing/weighting
 */
@Composable
fun NavActionButton(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    showBadge: Boolean = false,
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
    modifier: Modifier = Modifier,
) {
    TextButton(
        onClick = onClick,
        modifier = modifier.height(44.dp),
        colors = ButtonDefaults.textButtonColors(
            containerColor = containerColor,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(horizontal = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                if (showBadge) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 4.dp, y = (-2).dp)
                            .size(6.dp)
                            .background(MaterialTheme.colorScheme.error, CircleShape)
                    )
                }
            }
            Spacer(Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.3.sp
            )
        }
    }
}

/**
 * The standard Records + Settings pair shown together on the home screen.
 *
 * @param onNavigateToHistory Callback for the Records action
 * @param onNavigateToSettings Callback for the Settings action
 * @param showSettingsBadge Draws the attention dot on the Settings action
 * @param modifier Modifier for the containing row
 */
@Composable
fun RecordsSettingsRow(
    onNavigateToHistory: () -> Unit,
    onNavigateToSettings: () -> Unit,
    showSettingsBadge: Boolean = false,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        NavActionButton(
            title = "Records",
            icon = Icons.Default.History,
            onClick = onNavigateToHistory,
            modifier = Modifier.weight(1f)
        )
        NavActionButton(
            title = "Settings",
            icon = Icons.Default.Settings,
            onClick = onNavigateToSettings,
            showBadge = showSettingsBadge,
            modifier = Modifier.weight(1f)
        )
    }
}
