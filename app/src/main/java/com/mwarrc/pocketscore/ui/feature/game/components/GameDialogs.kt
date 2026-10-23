package com.mwarrc.pocketscore.ui.feature.game.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mwarrc.pocketscore.domain.model.AppSettings
import com.mwarrc.pocketscore.domain.model.GameEvent
import com.mwarrc.pocketscore.domain.model.Player
import com.mwarrc.pocketscore.domain.model.RosterSortOption
import com.mwarrc.pocketscore.ui.theme.getMaterialPlayerColor

/**
 * Expressive M3 end-of-match dialog triggered from the End Game button.
 *
 * Design principles:
 *  - Hero block at the top anchors the dialog with a tonal icon + match-end headline
 *  - Each action is a full-width Card with a bold icon, title, and subtitle
 *  - Colors are dynamic: primary for finish, secondary for play again, surface for pause
 *  - Guest mode checkbox integrates inline without a separate row
 *
 * @param settings          Current application settings
 * @param onDismiss         Callback to close the dialog without action
 * @param onFinishAndArchive Callback to end session and save (with guest-override flag)
 * @param onRestartMatch    Callback to archive and start fresh with same players
 * @param onResumeLater     Callback to return home without finalizing
 * @param onModifyRoster    Callback to open the player roster manager
 */
@Composable
fun ResetGameDialog(
    settings: AppSettings,
    onDismiss: () -> Unit,
    onFinishAndArchive: (Boolean) -> Unit,
    onRestartMatch: (Boolean) -> Unit,
    onResumeLater: () -> Unit,
    onModifyRoster: () -> Unit
) {
    val isGuestRestricted = settings.isGuestSession && !settings.guestSaveRecords
    var overrideGuest by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .fillMaxWidth(0.93f)
            .statusBarsPadding()
            .displayCutoutPadding(),
        shape = RoundedCornerShape(32.dp),
        containerColor = if (isGuestRestricted)
            MaterialTheme.colorScheme.surfaceColorAtElevation(10.dp)
        else
            MaterialTheme.colorScheme.surfaceContainerHigh,
        confirmButton = {},
        dismissButton = {},
        title = null,
        icon = null,
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                // ── Hero Block --──
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = if (isGuestRestricted)
                            MaterialTheme.colorScheme.tertiaryContainer
                        else
                            MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(72.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (isGuestRestricted) Icons.Default.Groups else Icons.Default.EmojiEvents,
                                contentDescription = null,
                                modifier = Modifier.size(36.dp),
                                tint = if (isGuestRestricted)
                                    MaterialTheme.colorScheme.onTertiaryContainer
                                else
                                    MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                    Text(
                        text = if (isGuestRestricted) "End Guest Session?" else "Match Complete!",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        text = if (isGuestRestricted)
                            "Guest sessions don't save to History by default."
                        else
                            "What would you like to do next?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // ── Guest override toggle ─────────────
                if (isGuestRestricted) {
                    Surface(
                        onClick = { overrideGuest = !overrideGuest },
                        shape = RoundedCornerShape(16.dp),
                        color = if (overrideGuest)
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                        else
                            MaterialTheme.colorScheme.surfaceContainerHighest,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Checkbox(checked = overrideGuest, onCheckedChange = { overrideGuest = it })
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text(
                                    "Save this match to History",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = if (overrideGuest) MaterialTheme.colorScheme.onPrimaryContainer
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    "Override Guest Mode for this match only",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }

                // ── Action Cards --

                // 1. Finish Match (PRIMARY / ALERT)
                val isDiscarding = isGuestRestricted && !overrideGuest
                EndGameActionCard(
                    icon = if (isDiscarding) Icons.Default.DeleteForever else Icons.Default.Check,
                    title = if (isDiscarding) "Discard Session" else "Finish Match",
                    subtitle = if (isDiscarding) "Close & permanently delete" else "Lock scores & return home",
                    containerColor = if (isDiscarding)
                        MaterialTheme.colorScheme.error
                    else
                        MaterialTheme.colorScheme.primary,
                    contentColor = if (isDiscarding)
                        MaterialTheme.colorScheme.onError
                    else
                        MaterialTheme.colorScheme.onPrimary,
                    iconBgColor = if (isDiscarding)
                        MaterialTheme.colorScheme.onError.copy(alpha = 0.15f)
                    else
                        MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.15f),
                    onClick = { onFinishAndArchive(overrideGuest) }
                )

                Spacer(Modifier.height(10.dp))

                // 2. Play Again (SECONDARY)
                EndGameActionCard(
                    icon = Icons.Default.Refresh,
                    title = "Play Again",
                    subtitle = "Save this match & start a new round",
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    iconBgColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f),
                    onClick = { onRestartMatch(overrideGuest) }
                )

                Spacer(Modifier.height(30.dp))

                // 3. Pause & Exit (SURFACE)
                EndGameActionCard(
                    icon = Icons.Default.Pause,
                    title = "Pause & Exit",
                    subtitle = "Save progress and come back later",
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    iconBgColor = MaterialTheme.colorScheme.surfaceVariant,
                    onClick = onResumeLater
                )

                // ── Dismiss link --
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Text(
                        "Cancel",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (isGuestRestricted)
                            MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        }
    )
}

/**
 * Reusable expressive action card used inside [ResetGameDialog].
 * Full-width, filled container color, icon on the left, title+subtitle stacked.
 */
@Composable
private fun EndGameActionCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    containerColor: androidx.compose.ui.graphics.Color,
    contentColor: androidx.compose.ui.graphics.Color,
    iconBgColor: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = containerColor,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = iconBgColor,
                modifier = Modifier.size(52.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(26.dp),
                        tint = contentColor
                    )
                }
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = contentColor
                )
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = contentColor.copy(alpha = 0.75f)
                )
            }
            Icon(
                Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = contentColor.copy(alpha = 0.6f)
            )
        }
    }
}

/**
 * General exit confirmation dialog for hardware back press or home button.
 * 
 * @param onDismiss Callback to stay in the app
 * @param onConfirm Callback to exit the app
 */
@Composable
fun ExitConfirmationDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Default.Close,
                null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text(
                "Close App?",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                "Your game progress will be saved and you can resume later.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Close App")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Stay")
            }
        },
        shape = RoundedCornerShape(24.dp)
    )
}

/**
 * Confirmation dialog for the Undo action.
 * 
 * Shows details about what action is being reverted and who the turn will return to.
 * 
 * @param lastScoreEvent The event that will be undone
 * @param players Current list of players
 * @param onDismiss Callback to cancel undo
 * @param onConfirm Callback to proceed with undo
 */
@Composable
fun UndoConfirmationDialog(
    lastScoreEvent: GameEvent?,
    players: List<Player>,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    if (lastScoreEvent == null) {
        onDismiss()
        return
    }

    val previousPlayerName = remember(lastScoreEvent.previousPlayerId, players) {
        players.find { it.id == lastScoreEvent.previousPlayerId }?.name ?: "Unknown"
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.AutoMirrored.Filled.Undo,
                    null,
                    tint = MaterialTheme.colorScheme.error
                )
                Spacer(Modifier.width(8.dp))
                Text("Confirm Undo")
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Are you sure you want to revert the last action?")
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            "ACTION TO REVERT:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "${lastScoreEvent.playerName} scored ${lastScoreEvent.points} points",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                        Text(
                            "NEW STATE:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "• Turn will go back to: $previousPlayerName",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            "• ${lastScoreEvent.playerName}'s score will decrease by ${lastScoreEvent.points}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                Text(
                    "This is final. You can only undo the very last move.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) { Text("Revert Now") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

/**
 * Expressive dialog for starting a new match immediately.
 * 
 * Allows re-selecting players from the current match (sorted appropriately)
 * or adding existing players from the library / guest pool.
 * 
 * @param currentPlayers Players from the just-finished match
 * @param settings Current application settings
 * @param onDismiss Callback to cancel
 * @param onStartGame Callback with the IDs of the players for the next match
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun QuickRestartDialog(
    currentPlayers: List<Player>,
    settings: AppSettings,
    onDismiss: () -> Unit,
    onStartGame: (List<String>) -> Unit
) {
    // Players selected to play in the next match
    var selectedNames by remember {
        mutableStateOf(currentPlayers.filter { it.isActive }.map { it.name }.toSet())
    }

    // Custom guest players added during this session
    var customGuests by remember { mutableStateOf(listOf<String>()) }

    // Text input for adding a new player
    var newPlayerName by remember { mutableStateOf("") }
    var inputError by remember { mutableStateOf<String?>(null) }
    val focusManager = LocalFocusManager.current

    // Sorting strategy & random seed
    var sortOption by remember { mutableStateOf(RosterSortOption.LOSERS_FIRST) }
    var randomSeed by remember { mutableStateOf(System.currentTimeMillis().toInt()) }

    // Add player action
    fun handleAddPlayer() {
        val clean = newPlayerName.trim()
        if (clean.isBlank()) return

        val alreadySelected = selectedNames.any { it.equals(clean, ignoreCase = true) }
        if (alreadySelected) {
            inputError = "$clean is already in this match"
            return
        }

        // Check if there is an existing case in match or saved players
        val matchedExistingName = (currentPlayers.map { it.name } + settings.savedPlayerNames + customGuests)
            .firstOrNull { it.equals(clean, ignoreCase = true) }

        val nameToAdd = matchedExistingName ?: clean
        if (matchedExistingName == null && !customGuests.any { it.equals(clean, ignoreCase = true) }) {
            customGuests = customGuests + clean
        }

        selectedNames = selectedNames + nameToAdd
        newPlayerName = ""
        inputError = null
    }

    // Active ordered lineup
    val orderedRoster = remember(
        selectedNames,
        sortOption,
        randomSeed,
        currentPlayers
    ) {
        val selectedList = selectedNames.toList()
        if (selectedList.isEmpty()) return@remember emptyList()

        val currentMatchMap = currentPlayers.associateBy { it.name.trim().lowercase() }
        val maxScore = currentPlayers.maxOfOrNull { it.score } ?: 0
        val minScore = currentPlayers.minOfOrNull { it.score } ?: 0

        when (sortOption) {
            RosterSortOption.LOSERS_FIRST -> {
                // Lowest score breaks/starts first
                selectedList.sortedWith(
                    compareBy<String> { name ->
                        val p = currentMatchMap[name.trim().lowercase()]
                        p?.score ?: (minScore + 1)
                    }.thenBy { it.lowercase() }
                )
            }
            RosterSortOption.WINNERS_FIRST -> {
                // Highest score breaks/starts first
                selectedList.sortedWith(
                    compareByDescending<String> { name ->
                        val p = currentMatchMap[name.trim().lowercase()]
                        p?.score ?: (minScore - 1)
                    }.thenBy { it.lowercase() }
                )
            }
            RosterSortOption.RANDOM -> {
                selectedList.shuffled(java.util.Random(randomSeed.toLong()))
            }
            RosterSortOption.MANUAL -> {
                // Keep match turn order first, then added players in order
                val matchOrdered = currentPlayers.map { it.name }.filter { name ->
                    selectedNames.any { it.equals(name, ignoreCase = true) }
                }
                val otherOrdered = selectedList.filter { name ->
                    currentPlayers.none { it.name.equals(name, ignoreCase = true) }
                }
                matchOrdered + otherOrdered
            }
            else -> selectedList
        }
    }

    // Bench / Available to add:
    // 1. Unselected current match players
    val unselectedMatchPlayers = remember(currentPlayers, selectedNames) {
        currentPlayers.filter { p -> selectedNames.none { it.equals(p.name, ignoreCase = true) } }
    }

    // 2. Unselected saved players
    val unselectedSavedNames = remember(settings.savedPlayerNames, selectedNames, currentPlayers) {
        settings.savedPlayerNames.filter { saved ->
            selectedNames.none { it.equals(saved, ignoreCase = true) } &&
            currentPlayers.none { it.name.equals(saved, ignoreCase = true) }
        }
    }

    // 3. Unselected custom guests
    val unselectedGuests = remember(customGuests, selectedNames) {
        customGuests.filter { guest -> selectedNames.none { it.equals(guest, ignoreCase = true) } }
    }

    val maxScore = remember(currentPlayers) { currentPlayers.maxOfOrNull { it.score } ?: 0 }
    val minScore = remember(currentPlayers) { currentPlayers.minOfOrNull { it.score } ?: 0 }
    val scrollState = rememberScrollState()

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .fillMaxWidth(0.95f)
            .statusBarsPadding()
            .displayCutoutPadding()
            .padding(vertical = 16.dp),
        shape = RoundedCornerShape(28.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(50.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Next Match Roster",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        "Set players & starting turn sequence",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Quick toggle: Select All / Deselect
                TextButton(
                    onClick = {
                        if (selectedNames.size == currentPlayers.size) {
                            selectedNames = emptySet()
                        } else {
                            selectedNames = currentPlayers.map { it.name }.toSet()
                        }
                    },
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Text(
                        if (selectedNames.size == currentPlayers.size) "Clear" else "All",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ── 1. Starting Turn Order Selector ──
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        "STARTING ORDER",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )

                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        val options = listOf(
                            Triple(RosterSortOption.LOSERS_FIRST, "Loser", Icons.Default.History),
                            Triple(RosterSortOption.WINNERS_FIRST, "Winner", Icons.Default.EmojiEvents),
                            Triple(RosterSortOption.RANDOM, "Shuffle", Icons.Default.Shuffle),
                            Triple(RosterSortOption.MANUAL, "Order", Icons.AutoMirrored.Filled.Sort)
                        )
                        options.forEachIndexed { index, (option, label, icon) ->
                            SegmentedButton(
                                selected = sortOption == option,
                                onClick = {
                                    if (sortOption == option && option == RosterSortOption.RANDOM) {
                                        randomSeed = (System.currentTimeMillis() + java.util.Random().nextInt(10000)).toInt()
                                    } else {
                                        sortOption = option
                                        if (option == RosterSortOption.RANDOM) {
                                            randomSeed = (System.currentTimeMillis() + java.util.Random().nextInt(10000)).toInt()
                                        }
                                    }
                                },
                                shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                                icon = {
                                    Icon(icon, null, modifier = Modifier.size(16.dp))
                                }
                            ) {
                                Text(label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // ── 2. Active Starting Lineup ──
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "ACTIVE LINEUP (${orderedRoster.size})",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.sp
                        )
                        Spacer(Modifier.weight(1f))
                        if (orderedRoster.isNotEmpty()) {
                            Text(
                                "Turn order: 1 to ${orderedRoster.size}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }

                    if (orderedRoster.isEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerLow,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "No players selected yet. Tap from below or add a new player.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    } else {
                        orderedRoster.forEachIndexed { index, name ->
                            val matchPlayer = currentPlayers.find { it.name.equals(name, ignoreCase = true) }
                            val score = matchPlayer?.score
                            val isWinner = matchPlayer != null && score == maxScore && currentPlayers.size > 1 && score > minScore
                            val isLoser = matchPlayer != null && score == minScore && currentPlayers.size > 1
                            val isGuest = matchPlayer == null && settings.savedPlayerNames.none { it.equals(name, ignoreCase = true) }

                            ActiveRosterItem(
                                rank = index + 1,
                                name = name,
                                score = score,
                                isWinner = isWinner,
                                isLoser = isLoser,
                                isGuest = isGuest,
                                onRemove = {
                                    selectedNames = selectedNames - name
                                }
                            )
                        }
                    }
                }

                // ── 3. Bench / Quick Add Players (if any available) ──
                val hasAvailableToBench = unselectedMatchPlayers.isNotEmpty() || unselectedSavedNames.isNotEmpty() || unselectedGuests.isNotEmpty()
                if (hasAvailableToBench) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "TAP TO ADD",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 1.sp
                        )

                        val allAvailable = remember(unselectedMatchPlayers, unselectedSavedNames, unselectedGuests) {
                            unselectedMatchPlayers.map { it.name to it.score } +
                            unselectedSavedNames.map { it to null } +
                            unselectedGuests.map { it to null }
                        }
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            allAvailable.chunked(3).forEach { rowItems ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    rowItems.forEach { (name, score) ->
                                        AvailablePlayerChip(
                                            name = name,
                                            score = score,
                                            onAdd = { selectedNames = selectedNames + name }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // ── 4. Add Player Input ──
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.PersonAdd,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(10.dp))
                            BasicTextField(
                                value = newPlayerName,
                                onValueChange = {
                                    newPlayerName = it
                                    inputError = null
                                },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                textStyle = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Medium
                                ),
                                keyboardOptions = KeyboardOptions(
                                    capitalization = KeyboardCapitalization.Words,
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        handleAddPlayer()
                                        focusManager.clearFocus()
                                    }
                                ),
                                decorationBox = { innerTextField ->
                                    if (newPlayerName.isBlank()) {
                                        Text(
                                            "Add guest or new player...",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                        )
                                    }
                                    innerTextField()
                                }
                            )
                            if (newPlayerName.isNotBlank()) {
                                FilledTonalButton(
                                    onClick = {
                                        handleAddPlayer()
                                        focusManager.clearFocus()
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier.height(34.dp)
                                ) {
                                    Text(
                                        "Add",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    if (inputError != null) {
                        Text(
                            inputError!!,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            val canStart = orderedRoster.size >= 2
            Surface(
                onClick = {
                    if (canStart) {
                        onStartGame(orderedRoster)
                    }
                },
                enabled = canStart,
                shape = RoundedCornerShape(18.dp),
                color = if (canStart) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.PlayArrow,
                        null,
                        modifier = Modifier.size(24.dp),
                        tint = if (canStart) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.width(8.dp))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            if (canStart) "Start Match (${orderedRoster.size} Players)" else "Select at least 2 players",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (canStart) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (canStart && orderedRoster.isNotEmpty()) {
                            Text(
                                "• ${orderedRoster.first()} breaks first",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "Cancel",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    )
}

/**
 * Item in the active lineup displaying turn sequence, status, and removal action.
 */
@Composable
private fun ActiveRosterItem(
    rank: Int,
    name: String,
    score: Int?,
    isWinner: Boolean,
    isLoser: Boolean,
    isGuest: Boolean,
    onRemove: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (rank == 1)
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
        else
            MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Turn Badge
            Surface(
                shape = CircleShape,
                color = if (rank == 1)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.size(28.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "$rank",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Black,
                        color = if (rank == 1)
                            MaterialTheme.colorScheme.onPrimary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Player Avatar with Initial
            Surface(
                modifier = Modifier.size(36.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        name.take(1).uppercase(),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Name & Status
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (rank == 1) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        ) {
                            Text(
                                "Breaks First",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                when {
                    isWinner -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Default.EmojiEvents,
                                contentDescription = null,
                                modifier = Modifier.size(13.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                "Winner • $score pts",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    isLoser && score != null -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Default.History,
                                contentDescription = null,
                                modifier = Modifier.size(13.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                "Last place • $score pts",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                            )
                        }
                    }
                    score != null -> {
                        Text(
                            "$score pts last game",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }
                    isGuest -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.size(13.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                            Text(
                                "New guest player",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                            )
                        }
                    }
                    else -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Default.Bookmark,
                                contentDescription = null,
                                modifier = Modifier.size(13.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                            Text(
                                "Saved player",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }

            // Remove Button
            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Remove player",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

/**
 * Chip for tapping to add an unselected or saved player into the match.
 */
@Composable
private fun AvailablePlayerChip(
    name: String,
    score: Int?,
    onAdd: () -> Unit
) {
    Surface(
        onClick = onAdd,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = Modifier.animateContentSize()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
            Text(
                name,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (score != null) {
                Text(
                    "($score)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}
