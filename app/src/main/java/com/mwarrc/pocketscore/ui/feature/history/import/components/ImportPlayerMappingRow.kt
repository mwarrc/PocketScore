package com.mwarrc.pocketscore.ui.feature.history.import_.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.MergeType
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * A single animated row representing one imported player's identity mapping.
 *
 * Visual states:
 * - **NEW** (secondary tint): The player will be created fresh in the local roster.
 * - **MERGE** (primary tint): The player will be linked to an existing local player.
 * - **AUTO** badge: The mapping was suggested automatically by fuzzy matching.
 *
 * @param importedName      The raw player name from the imported file.
 * @param existingPlayers   All local player names available for mapping.
 * @param currentMapping    The currently selected local player, or null for "New Player".
 * @param unavailablePlayers Local players already claimed by another import mapping.
 * @param isAutoMatched     Whether this mapping was auto-suggested.
 * @param onMappingChanged  Callback with the new local name, or null to create new.
 */
@Composable
fun ImportPlayerMappingRow(
    importedName: String,
    existingPlayers: List<String>,
    currentMapping: String?,
    unavailablePlayers: Set<String>,
    isAutoMatched: Boolean,
    onMappingChanged: (String?) -> Unit
) {
    var dropdownExpanded by remember { mutableStateOf(false) }
    val isMerging = currentMapping != null
    val isNewPlayer = !isMerging

    val cardColor by animateColorAsState(
        targetValue = if (isMerging)
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        else
            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.25f),
        animationSpec = tween(300),
        label = "cardColor"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isMerging)
            MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
        else
            MaterialTheme.colorScheme.secondary.copy(alpha = 0.25f),
        animationSpec = tween(300),
        label = "borderColor"
    )

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = cardColor,
        border = BorderStroke(1.dp, borderColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // ── Left: Imported identity (read-only) ──
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "FROM FILE",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 0.8.sp
                    )
                    Spacer(Modifier.height(3.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                Icons.Default.Person, null,
                                modifier = Modifier.padding(6.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = importedName,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // ── Center: Directional arrow ──
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .size(18.dp),
                    tint = if (isMerging)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                )

                // ── Right: Target identity (interactive) ──
                Surface(
                    onClick = { dropdownExpanded = true },
                    shape = RoundedCornerShape(10.dp),
                    color = if (isMerging)
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                    else
                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
                        // Badge row
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            AnimatedContent(
                                targetState = isMerging,
                                transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(200)) },
                                label = "statusBadge"
                            ) { merging ->
                                if (merging) MergeBadge() else NewBadge()
                            }
                            if (isAutoMatched && isMerging) AutoBadge()
                        }
                        Spacer(Modifier.height(3.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = currentMapping ?: "New Player",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isMerging)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.secondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                Icons.Default.ArrowDropDown,
                                null,
                                modifier = Modifier.size(18.dp),
                                tint = if (isMerging)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
            }

            // ── Dropdown ──
            ImportMappingDropdown(
                expanded = dropdownExpanded,
                onDismiss = { dropdownExpanded = false },
                isNewPlayer = isNewPlayer,
                currentMapping = currentMapping,
                existingPlayers = existingPlayers,
                unavailablePlayers = unavailablePlayers,
                onMappingChanged = onMappingChanged
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Status Badges
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun MergeBadge() {
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.MergeType, null, modifier = Modifier.size(9.dp), tint = MaterialTheme.colorScheme.primary)
            Text(
                "MERGE",
                style = MaterialTheme.typography.labelSmall,
                fontSize = 8.sp,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Composable
fun NewBadge() {
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Icon(Icons.Default.PersonAdd, null, modifier = Modifier.size(9.dp), tint = MaterialTheme.colorScheme.secondary)
            Text(
                "NEW",
                style = MaterialTheme.typography.labelSmall,
                fontSize = 8.sp,
                color = MaterialTheme.colorScheme.secondary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Composable
fun AutoBadge() {
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Icon(Icons.Default.AutoFixHigh, null, modifier = Modifier.size(9.dp), tint = MaterialTheme.colorScheme.tertiary)
            Text(
                "AUTO",
                style = MaterialTheme.typography.labelSmall,
                fontSize = 8.sp,
                color = MaterialTheme.colorScheme.tertiary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Dropdown Menu
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ImportMappingDropdown(
    expanded: Boolean,
    onDismiss: () -> Unit,
    isNewPlayer: Boolean,
    currentMapping: String?,
    existingPlayers: List<String>,
    unavailablePlayers: Set<String>,
    onMappingChanged: (String?) -> Unit
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        modifier = Modifier
            .fillMaxWidth(0.75f)
            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
    ) {
        // Option: Create as new player
        DropdownMenuItem(
            text = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f),
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            Icons.Default.PersonAdd, null,
                            modifier = Modifier.padding(6.dp),
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(
                            "Create as New Player",
                            fontWeight = if (isNewPlayer) FontWeight.Bold else FontWeight.Normal
                        )
                        Text(
                            "Add to your roster",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            onClick = { onMappingChanged(null); onDismiss() },
            colors = if (isNewPlayer) MenuDefaults.itemColors(
                textColor = MaterialTheme.colorScheme.secondary,
                leadingIconColor = MaterialTheme.colorScheme.secondary
            ) else MenuDefaults.itemColors(),
            trailingIcon = if (isNewPlayer) {
                { Icon(Icons.Default.Check, null, tint = MaterialTheme.colorScheme.secondary) }
            } else null
        )

        if (existingPlayers.isNotEmpty()) {
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
            Text(
                text = "Merge with existing player",
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            existingPlayers.forEach { player ->
                val isUnavailable = player in unavailablePlayers && player != currentMapping
                val isSelected = currentMapping == player

                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = if (isSelected)
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                else
                                    MaterialTheme.colorScheme.surfaceContainerHigh,
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    if (isSelected) Icons.Default.Check else Icons.Default.Person,
                                    null,
                                    modifier = Modifier.padding(6.dp),
                                    tint = if (isSelected)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = player,
                                    color = when {
                                        isUnavailable -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                                        isSelected -> MaterialTheme.colorScheme.primary
                                        else -> MaterialTheme.colorScheme.onSurface
                                    },
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                                if (isUnavailable) {
                                    Text(
                                        "Already mapped",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }
                    },
                    enabled = !isUnavailable,
                    onClick = { onMappingChanged(player); onDismiss() }
                )
            }
        }
    }
}
