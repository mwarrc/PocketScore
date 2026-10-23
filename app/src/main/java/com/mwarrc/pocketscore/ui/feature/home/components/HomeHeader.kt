package com.mwarrc.pocketscore.ui.feature.home.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mwarrc.pocketscore.R

/**
 * Home screen header - app identity + minimal help access.
 */
@Composable
fun HomeHeader(
    onNavigateToAbout: () -> Unit,
    onNavigateToHelp: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme

    Row(
        modifier = modifier
            .fillMaxWidth()
            // The status bar is hidden app-wide on this screen, so statusBarsPadding()
            // resolves to ~0 and the header was drawing straight into the punch-hole
            // camera cutout. The cutout inset is always reported, so use whichever is
            // larger to keep the title clear of the camera.
            .windowInsetsPadding(WindowInsets.displayCutout)
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = 4.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        // ── Logo + wordmark ───────
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            FilledTonalIconButton(
                onClick = onNavigateToAbout,
                modifier = Modifier.size(44.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_pocket_logo),
                    contentDescription = "About PocketScore",
                    modifier = Modifier.size(22.dp),
                    tint = colorScheme.primary
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                Text(
                    text = "PocketScore",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold
                    ),
                    color = colorScheme.onSurface,
                )
                Text(
                    text = "Pick your squad, start the game",
                    style = MaterialTheme.typography.labelSmall,
                    color = colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                )
            }
        }

        // ── Help icon button ──────
        IconButton(
            onClick = onNavigateToHelp,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.HelpOutline,
                contentDescription = "Help",
                modifier = Modifier.size(20.dp),
                tint = colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
            )
        }
    }
}