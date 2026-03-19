package com.mwarrc.pocketscore.ui.feature.home.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Help
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mwarrc.pocketscore.R

/**
 * Material 3 home screen header for PocketScore.
 *
 * Utilizes standard M3 typography and components.
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
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        // ── Branding cluster ───────────────────────────────────────────
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.weight(1f),
        ) {
            FilledTonalIconButton(
                onClick = onNavigateToAbout,
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_pocket_logo),
                    contentDescription = "About PocketScore",
                    modifier = Modifier.size(24.dp),
                    tint = colorScheme.primary
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "PocketScore",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = colorScheme.onSurface,
                )
                Text(
                    text = "The expressive scoreboard",
                    style = MaterialTheme.typography.labelMedium,
                    color = colorScheme.onSurfaceVariant,
                )
            }
        }

        // ── Help chip --───
        AssistChip(
            onClick = onNavigateToHelp,
            label = { 
                Text(
                    text = "Help",
                    style = MaterialTheme.typography.labelLarge
                ) 
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.Help,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            },
            shape = RoundedCornerShape(24.dp),
            colors = AssistChipDefaults.assistChipColors(
                containerColor = colorScheme.secondaryContainer,
                labelColor = colorScheme.onSecondaryContainer,
                leadingIconContentColor = colorScheme.onSecondaryContainer
            ),
            border = null
        )
    }
}