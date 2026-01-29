package com.mwarrc.pocketscore.ui.feature.setup

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mwarrc.pocketscore.ui.feature.setup.components.PlayerNameRow
import com.mwarrc.pocketscore.ui.feature.setup.components.SetupGhostHeader
import com.mwarrc.pocketscore.ui.feature.setup.components.SetupValidationBanner

@Composable
fun SetupScreen(
    onStartGame: (List<String>) -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToRoadmap: () -> Unit,
    onNavigateToAbout: () -> Unit,
    maxPlayers: Int = 8,
    showComingSoonFeatures: Boolean = true
) {
    var playerNames by remember { mutableStateOf(listOf("", "")) }
    var hasStartedEditing by remember { mutableStateOf(false) }

    LaunchedEffect(playerNames) {
        if (playerNames.any { it.isNotEmpty() }) {
            hasStartedEditing = true
        }
    }

    val trimmedNames = playerNames.map { it.trim() }
    val hasEmptyNames = trimmedNames.any { it.isEmpty() }
    val hasDuplicateNames = trimmedNames.size != trimmedNames.toSet().size
    val isValid = !hasEmptyNames && !hasDuplicateNames && playerNames.size >= 2

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {},
        bottomBar = {},
        floatingActionButton = {
            AnimatedVisibility(
                visible = isValid,
                enter = scaleIn() + fadeIn(),
                exit = scaleOut() + fadeOut()
            ) {
                ExtendedFloatingActionButton(
                    onClick = { onStartGame(trimmedNames) },
                    icon = { Icon(Icons.Default.PlayArrow, null) },
                    text = { Text("Start Game") },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .consumeWindowInsets(padding)
                .statusBarsPadding()
                .imePadding()
        ) {
            SetupGhostHeader(
                onNavigateToHistory = onNavigateToHistory,
                onNavigateToSettings = onNavigateToSettings,
                onNavigateToAbout = onNavigateToAbout,
                modifier = Modifier.fillMaxWidth()
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 116.dp)
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        "Setup Players",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Add players to begin",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(Modifier.height(24.dp))

                if (!isValid && hasStartedEditing) {
                    val message = when {
                        hasEmptyNames -> "All player names must be filled"
                        hasDuplicateNames -> "Player names must be unique"
                        else -> "Add at least 2 players"
                    }
                    SetupValidationBanner(message = message)
                    Spacer(Modifier.height(16.dp))
                }

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    itemsIndexed(playerNames) { index, name ->
                        val trimmed = name.trim()
                        val isDuplicate = trimmedNames.count { it == trimmed && it.isNotEmpty() } > 1
                        val isEmpty = trimmed.isEmpty()
                        val hasError = (isDuplicate || isEmpty) && hasStartedEditing

                        PlayerNameRow(
                            index = index,
                            name = name,
                            hasError = hasError,
                            duplicateNameError = isDuplicate && !isEmpty,
                            allowRemove = playerNames.size > 2,
                            onNameChange = { newName ->
                                playerNames =
                                    playerNames.toMutableList().apply { this[index] = newName }
                            },
                            onRemove = {
                                playerNames =
                                    playerNames.toMutableList().apply { removeAt(index) }
                            }
                        )
                    }

                    item {
                        if (playerNames.size < maxPlayers) {
                            Spacer(Modifier.height(8.dp))
                            FilledTonalButton(
                                onClick = { playerNames = playerNames + "" },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(vertical = 16.dp)
                            ) {
                                Icon(Icons.Default.Add, null, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Add Player", style = MaterialTheme.typography.labelLarge)
                            }
                        }
                    }

                    if (showComingSoonFeatures) {
                        item {
                            Spacer(Modifier.height(12.dp))
                            OutlinedButton(
                                onClick = onNavigateToRoadmap,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(vertical = 12.dp),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                )
                            ) {
                                Icon(
                                    Icons.Default.Groups,
                                    null,
                                    modifier = Modifier.size(18.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "Switch to Teams (Coming Soon)",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    item {
                        Spacer(Modifier.height(80.dp))
                    }
                }
            }
        }
    }
}

