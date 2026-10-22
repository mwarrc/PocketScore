package com.mwarrc.pocketscore.ui.feature.game.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.*
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import com.mwarrc.pocketscore.ui.components.AutoSizeText
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.mwarrc.pocketscore.domain.logic.EliminationRules
import com.mwarrc.pocketscore.domain.model.Player
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import kotlinx.coroutines.delay

/**
 * Interactive card for a player whose score can be currently modified.
 * 
 * This component is used in the List layout for every player and as the 
 * header in Grid layout for the focused player.
 * 
 * @param player The player data for this card
 * @param isLeader Whether this player is currently winning
 * @param isTie Whether the lead is shared with others
 * @param isLoser Whether this player is currently in last place
 * @param isLoserTie Whether the last place is shared with others
 * @param isCurrentTurn Whether it is currently this player's active turn
 * @param isStrictTurnMode Whether the app is enforcing turn-based input
 * @param allowEliminatedInput Whether players out of contention can still receive scores
 * @param onAdd Callback for adding a specific amount of points
 * @param onSubtract Callback for subtracting a specific amount of points
 * @param onSetTurn Optional callback to set this player as the active turn holder
 * @param scoreInput Current text buffered for the "next points" entry
 * @param onScoreInputChange Callback when entry text changes
 * @param onFocus Callback when the card is interacted with (to show numpad)
 * @param useCustomKeyboard Whether to use the in-app numpad vs system IME
 * @param modifier Root modifier for the card
 * @param lastPoints The last score change recorded for this player
 * @param alwaysShowControls If true, entry fields are visible even if it's not their turn
 * @param leaderScore Current leader's score for elimination logic
 * @param tableSum Current sum on table for elimination logic
 * @param poolBallManagementEnabled Whether to perform contender math
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ActivePlayerCard(
    player: Player,
    isLeader: Boolean,
    isTie: Boolean = false,
    isLoser: Boolean = false,
    isLoserTie: Boolean = false,
    isCurrentTurn: Boolean,
    isStrictTurnMode: Boolean,
    allowEliminatedInput: Boolean = false,
    onAdd: (Int) -> Unit,
    onSubtract: (Int) -> Unit,
    onSetTurn: (() -> Unit)? = null,
    scoreInput: String = "",
    onScoreInputChange: (String) -> Unit = {},
    onFocus: () -> Unit = {},
    useCustomKeyboard: Boolean = true,
    modifier: Modifier = Modifier,
    lastPoints: Pair<Int, Boolean>? = null,
    isLastUpdated: Boolean = false,
    alwaysShowControls: Boolean = false,
    leaderScore: Int = 0,
    tableSum: Int = 0,
    poolBallManagementEnabled: Boolean = true,
    /**
     * Everyone still in the match. Required by [EliminationRules] for the
     * "nobody has scored yet" and tie-for-the-lead guards. Defaults to just this
     * player so existing call sites without the list still behave sensibly.
     */
    activePlayers: List<Player> = listOf(player),
) {
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    
    // Calculate if player is eliminated - Only if pool management is enabled.
    // Delegates to EliminationRules, the same rule the turn rotation uses, so the
    // ELIMINATED label and the skip logic can no longer disagree.
    val isEliminated = poolBallManagementEnabled && EliminationRules.isEliminated(
        player = player,
        activePlayers = activePlayers,
        tableSum = tableSum,
        leaderScore = leaderScore,
    )
    
    // Logic for editing:
    // 1. An eliminated player is locked out entirely unless the "allow
    //    eliminated input" setting is on, in which case they can be scored
    //    like anyone else.
    // 2. In Strict Mode the turn still gates everyone else, and strict mode
    //    always wins over the setting — the toggle is hidden there, so the
    //    setting can only be true if it was enabled beforehand.
    // 3. In Non-Strict Mode everyone else can edit freely.
    //
    // The rule itself lives in EliminationRules so this card, the shared
    // numpad and the turn rotation can't disagree about who may be scored.
    val isInputLocked = EliminationRules.isInputLocked(
        isEliminated = isEliminated,
        allowEliminatedInput = allowEliminatedInput,
        strictTurnMode = isStrictTurnMode,
    )
    val canEdit = when {
        isInputLocked -> false
        isStrictTurnMode -> isCurrentTurn
        else -> true
    }

    // A card the strict turn has locked must not look like it is accepting
    // text. The buffered digits belong to the turn holder; showing them on a
    // card they no longer own says "type here" for a player nobody is allowed
    // to score. An eliminated card shows its own "Out of the game" label
    // instead, so this only ever applies to the strict-turn lock.
    val showsBufferedInput = scoreInput.isNotEmpty() && canEdit


    // Snappy auto-focus for system keyboard to prevent it from closing on player switch
    LaunchedEffect(isCurrentTurn, useCustomKeyboard) {
        if (isCurrentTurn && !useCustomKeyboard && canEdit) {
            focusRequester.requestFocus()
        }
    }

    // Single resolved status, shared with the grid tile.
    val status = resolvePlayerCardStatus(
        isLeader = isLeader,
        isTie = isTie,
        isLoser = isLoser,
        isLoserTie = isLoserTie,
        isEliminated = isEliminated,
        isCurrentTurn = isCurrentTurn,
    )

    // Eliminated surface, resolved in one place so this card and the grid tile
    // can't drift apart.
    val eliminated = rememberEliminatedColors(baseContainer = MaterialTheme.colorScheme.surface)

    // The leader claims a hue of its own — violet by default — so a leader
    // card can never be mistaken for the active player's card. The two used to
    // share the primary's family (Material You derives secondary as a
    // near-neighbour of primary), which made them impossible to tell apart.
    val leader = rememberLeaderColors()

    // A leader who is also the active player keeps the active player's colour
    // and takes a shade of the leader's violet on top. A straight 50/50 mix
    // went muddy and a gradient across the card read as a third, louder
    // state; a light wash is the quiet way to say "both".
    val isLeaderAndTurn = isLeader && !isEliminated && isCurrentTurn

    val targetContainerColor = when {
        isEliminated -> eliminated.container
        isLeaderAndTurn -> leader.tint(MaterialTheme.colorScheme.primaryContainer)
        isLeader -> leader.container
        isCurrentTurn -> MaterialTheme.colorScheme.primaryContainer
        isLoser -> MaterialTheme.colorScheme.surfaceVariant
        else -> MaterialTheme.colorScheme.surface
    }

    val targetContentColor = when {
        isEliminated -> eliminated.nameInk
        // On the blended card the ink stays the active player's, so the wash
        // reads as a tint on a familiar surface.
        isLeaderAndTurn -> MaterialTheme.colorScheme.onPrimaryContainer
        isLeader -> leader.content
        isCurrentTurn -> MaterialTheme.colorScheme.onPrimaryContainer
        isLoser -> MaterialTheme.colorScheme.onSurfaceVariant
        else -> MaterialTheme.colorScheme.onSurface
    }

    // Cross-fade the state colours rather than snapping between them. Instant
    // container/ink swaps on tap were a large part of the card feeling abrupt
    // when moving between players; the layout no longer moves, and now the
    // colour settles into place instead of cutting.
    val containerColor by animateColorAsState(
        targetValue = targetContainerColor,
        animationSpec = tween(180),
        label = "card_container",
    )
    val contentColor by animateColorAsState(
        targetValue = targetContentColor,
        animationSpec = tween(180),
        label = "card_content",
    )

    // On an eliminated card the score steps back a little further than the name,
    // matching the grid tile so both layouts share the same hierarchy.
    val scoreColor by animateColorAsState(
        targetValue = if (isEliminated) eliminated.scoreInk else contentColor,
        animationSpec = tween(180),
        label = "card_score",
    )

    val targetBorderColor = when {
        isEliminated -> eliminated.border
        isLeader -> leader.accent.copy(alpha = 0.6f)
        isCurrentTurn -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
    }
    val animatedBorderColor by animateColorAsState(
        targetValue = targetBorderColor,
        animationSpec = tween(180),
        label = "card_border",
    )

    // Constant 1dp width in every state. A 2dp "current turn" border used to
    // change the stroke width, which re-laid-out the card interior by 1dp on
    // each side and nudged all the content when the turn moved.
    val borderStroke = BorderStroke(width = 1.dp, color = animatedBorderColor)

    Card(
        modifier = modifier
            .fillMaxWidth()
            // Same single-sentence description the grid tile uses, so an
            // eliminated player reads identically in either layout:
            // "Name, 14 points, eliminated".
            .then(
                if (isEliminated) {
                    Modifier.semantics(mergeDescendants = true) {
                        contentDescription = "${player.name}, ${player.score} points, eliminated"
                    }
                } else {
                    Modifier
                }
            ),
        onClick = {
            // In strict mode the turn decides who plays. Handing the turn to
            // whoever happened to be tapped quietly undid that, and it is how
            // the previous player stayed reachable after their turn had ended.
            if (!isStrictTurnMode) {
                onSetTurn?.invoke()
            }
            if (!useCustomKeyboard) {
                focusRequester.requestFocus()
                keyboardController?.show()
            }
            onFocus()
        },
        enabled = canEdit,
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = borderStroke,
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor,
            // A disabled card (an eliminated player whose input is locked)
            // must still paint the eliminated surface. Without these, the
            // disabled state silently falls back to the stock M3 disabled
            // tokens and the whole redesign is replaced by a flat grey card.
            disabledContainerColor = containerColor,
            disabledContentColor = contentColor,
        )
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Eliminated watermark — a quiet lock, shared with the grid tile so
            // both layouts mark elimination the same way.
            if (isEliminated) {
                EliminatedWatermark(isCompact = false)
            }
            
            // Leader decoration. You asked for the stars back, so here they are
            // — but rebuilt properly, clipped to the card, with proportional
            // placement instead of the old fixed offsets.
            //
            // Note the condition: this used to also require !isCurrentTurn, so
            // the stars disappeared the moment you tapped the leader (which
            // makes them the header's current turn). Leadership is a lasting
            // property of the player, not of whose turn it is, so it's the only
            // flag that gates the decoration now.
            if (isLeader && !isEliminated) {
                LeaderStarField(
                    isCurrentTurn = isCurrentTurn,
                    isCompact = false,
                )
            }

            // (No floating overlays on this card. The recent-input chip lives
            // under the score and the loser chip under the name, both inline —
            // floating them at the vertical centre used to drop them straight
            // on top of the entry field and the +/- buttons.)

            Column(
                modifier = Modifier
                    .padding(12.dp)
                    .fillMaxWidth()
                    // SpaceBetween, but with a much tighter header height than
                    // the original. This gives a comfortable breathing gap
                    // between the name block and the input row without the
                    // oversized void the first version had.
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        AutoSizeText(
                            text = player.name,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = if (isLeader || isCurrentTurn) FontWeight.Black else FontWeight.Bold,
                            color = contentColor
                        )
                        // Role chips, inline under the name so nothing floats
                        // over the entry field. Leader and loser can both show
                        // side by side; leadership and last place are
                        // independent facts.
                        //
                        // Elimination is deliberately NOT a chip. It is plain
                        // red text in the same slot, so the fact is stated in
                        // words rather than as a second solid stamp — and it no
                        // longer swallows the LOSER chip, which used to make it
                        // impossible to tell who was last once several players
                        // were out.
                        val roleChips = buildList {
                            // Leader is suppressed for an eliminated player: a
                            // player on the leading score can never be
                            // eliminated, so this is belt and braces, but if
                            // the two ever disagree "out of the match" is the
                            // stronger claim.
                            if (isLeader && !isEliminated) add(PlayerCardRole.LEADER to isTie)
                            if (isLoser) add(PlayerCardRole.LOSER to isLoserTie)
                        }
                        if (roleChips.isNotEmpty() || isEliminated) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(top = 3.dp),
                            ) {
                                roleChips.forEach { (role, tied) ->
                                    PlayerRoleChip(
                                        role = role,
                                        isTie = tied,
                                        shape = MaterialTheme.shapes.extraLarge,
                                    )
                                }
                                // Plain red text, not a chip. It replaces the
                                // solid ELIMINATED pill that used to sit in this
                                // slot and no longer swallows the LOSER chip
                                // beside it — being out of the match and being
                                // last place are independent facts.
                                if (isEliminated) {
                                    Text(
                                        text = "Eliminated",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp),
                                        fontWeight = FontWeight.Medium,
                                        color = eliminated.accent,
                                        maxLines = 1,
                                    )
                                }
                            }
                        }
                        // No per-card mode label. There used to be a "Turn
                        // Locked" line here for players who weren't on turn
                        // and a "Tap to Play" line for the ones who were, and
                        // the first of them was suppressed for leaders and
                        // losers — so the same rule was stated on some cards
                        // and missing on others. The mode is now stated once,
                        // by the banner at the top of the screen, and the card
                        // can just be a card.
                    }

                    // Right column is the score column, end-aligned: status
                    // badge beside the score, and the most-recent input chip
                    // directly under it. Inline placement instead of a floating
                    // edge chip keeps it off the +/- buttons.
                    Column(
                        horizontalAlignment = Alignment.End,
                        modifier = Modifier.padding(start = 12.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            // The corner disc is for statuses not already
                            // covered by a labelled role chip under the name.
                            // Eliminated carries its own red label there, so it
                            // is excluded here — two marks for the same fact on
                            // one card is clutter.
                            if (!isLeader && !isLoser && !isEliminated) {
                                PlayerStatusBadge(status = status)
                            }

                            // Score, anchored to the trailing edge.
                            AutoSizeText(
                                text = "${player.score}",
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Black,
                                color = scoreColor,
                            )
                        }

                        if (lastPoints != null) {
                            val (pointsValue, isUndo) = lastPoints
                            val style = resolvePointsChangeStyle(
                                pointsValue = pointsValue,
                                isUndo = isUndo,
                            )
                            // A filled chip is one of the loudest things on the
                            // card, so the eliminated variant drops it and keeps
                            // only the value, in the same muted ink as the name.
                            if (isEliminated) {
                                Text(
                                    text = style.text,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    fontWeight = FontWeight.Medium,
                                    color = eliminated.nameInk,
                                    maxLines = 1,
                                    modifier = Modifier.padding(top = 1.dp),
                                )
                            } else {
                                PointsChangeChip(
                                    style = style,
                                    shape = MaterialTheme.shapes.small,
                                    modifier = Modifier.padding(top = 2.dp),
                                )
                            }
                        }
                    }
                }

                // In grid the header card always shows its controls, so animating
                // the height would replay an expand-from-zero on every player
                // switch (the card is recreated for a new player). Skip the
                // height animation entirely in that case; only the List layout,
                // where the row genuinely opens and closes, animates.
                val animateControls = !alwaysShowControls
                androidx.compose.animation.AnimatedVisibility(
                    visible = isCurrentTurn || alwaysShowControls,
                    enter = if (animateControls) {
                        androidx.compose.animation.expandVertically(animationSpec = tween(200)) +
                                androidx.compose.animation.fadeIn(animationSpec = tween(200))
                    } else {
                        androidx.compose.animation.EnterTransition.None
                    },
                    exit = if (animateControls) {
                        androidx.compose.animation.shrinkVertically(animationSpec = tween(200)) +
                                androidx.compose.animation.fadeOut(animationSpec = tween(200))
                    } else {
                        androidx.compose.animation.ExitTransition.None
                    }
                ) {
                    Column {
                        Spacer(Modifier.height(4.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            val haptic = LocalHapticFeedback.current
                            var isInputError by remember { mutableStateOf(false) }

                            LaunchedEffect(isInputError) {
                                if (isInputError) {
                                    delay(350)
                                    isInputError = false
                                }
                            }

                            val borderColor by animateColorAsState(
                                targetValue = if (isInputError) MaterialTheme.colorScheme.error
                                              else MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                                label = "borderColorFlash"
                            )
                            val backgroundColor by animateColorAsState(
                                targetValue = if (isInputError) MaterialTheme.colorScheme.error.copy(alpha = 0.08f)
                                              else MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = if (canEdit) 1f else 0.5f),
                                label = "backgroundColorFlash"
                            )

                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp),
                                shape = RoundedCornerShape(12.dp),
                                onClick = {
                                    if (!isStrictTurnMode) {
                                        onSetTurn?.invoke()
                                    }
                                    onFocus()
                                    if (!useCustomKeyboard) {
                                        focusRequester.requestFocus()
                                        keyboardController?.show()
                                    }
                                },
                                enabled = canEdit,
                                color = backgroundColor,
                                border = BorderStroke(1.dp, borderColor)
                            ) {
                                if (!useCustomKeyboard && canEdit) {
                                    BasicTextField(
                                        value = scoreInput,
                                        onValueChange = { newValue: String ->
                                            if ((newValue.isEmpty() || newValue.all { it.isDigit() }) && newValue.length <= 4) {
                                                onScoreInputChange(newValue)
                                                isInputError = false
                                            } else if (newValue.length > 4) {
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                isInputError = true
                                            } else {
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                isInputError = true
                                            }
                                        },
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .focusRequester(focusRequester),
                                        textStyle = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            fontSize = 22.sp
                                        ),
                                        keyboardOptions = KeyboardOptions(
                                            keyboardType = KeyboardType.Number,
                                            imeAction = ImeAction.Done
                                        ),
                                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                        decorationBox = { innerTextField ->
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .padding(horizontal = 16.dp),
                                                contentAlignment = Alignment.CenterStart
                                            ) {
                                                if (scoreInput.isEmpty()) {
                                                    Text(
                                                        if (isInputError) "Max 4 digits" else "+ pts",
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        color = if (isInputError) MaterialTheme.colorScheme.error
                                                                else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                                    )
                                                }
                                                innerTextField()
                                                if (scoreInput == "0") {
                                                    ZeroPointsTooltip(
                                                        modifier = Modifier.align(Alignment.CenterEnd)
                                                    )
                                                }
                                            }
                                        }
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(horizontal = 16.dp),
                                        contentAlignment = Alignment.CenterStart
                                    ) {
                                        if (isInputLocked) {
                                            // Points cannot be entered at all, so
                                            // this must not look like a text field
                                            // waiting for input. Previously the
                                            // field still showed "+ pts" and the
                                            // numpad could still type digits into
                                            // the shared buffer — they just could
                                            // not be applied.
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    Icons.Default.Lock,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(14.dp),
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                                )
                                                Spacer(Modifier.width(6.dp))
                                                Text(
                                                    text = "Out of the game",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                                )
                                            }
                                        } else {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                if (!showsBufferedInput) {
                                                    Text(
                                                        "+ pts",
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                                            alpha = 0.5f
                                                        )
                                                    )
                                                } else {
                                                    Text(
                                                        scoreInput,
                                                        style = MaterialTheme.typography.titleLarge,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        fontSize = 22.sp,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                }

                                                // Blinking cursor
                                                if (isCurrentTurn) {
                                                    var cursorVisible by remember { mutableStateOf(true) }
                                                    LaunchedEffect(Unit) {
                                                        while (true) {
                                                            kotlinx.coroutines.delay(500)
                                                            cursorVisible = !cursorVisible
                                                        }
                                                    }

                                                    Box(
                                                        modifier = Modifier
                                                            .padding(start = 2.dp)
                                                            .width(2.dp)
                                                            .height(24.dp)
                                                            .alpha(if (cursorVisible) 1f else 0f)
                                                            .background(MaterialTheme.colorScheme.primary)
                                                    )
                                                }
                                            }

                                            if (showsBufferedInput && scoreInput == "0") {
                                                ZeroPointsTooltip(
                                                    modifier = Modifier.align(Alignment.CenterEnd)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                 FilledIconButton(
                                    onClick = {
                                        if (!isStrictTurnMode && !isCurrentTurn) onSetTurn?.invoke()
                                        val points = scoreInput.toIntOrNull() ?: 0
                                        onSubtract(points)
                                        onScoreInputChange("")
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight(),
                                    shape = RoundedCornerShape(12.dp),
                                    enabled = canEdit && scoreInput.isNotEmpty(),
                                    colors = IconButtonDefaults.filledIconButtonColors(
                                        // Was branching on isSystemInDarkTheme() with
                                        // hardcoded 0xFF8C1D18 / 0xFFFFB4AB. The M3
                                        // error token is already mode-aware, and the
                                        // system check ignored the in-app theme choice
                                        // and dynamic colour.
                                        containerColor = MaterialTheme.colorScheme.error,
                                        contentColor = MaterialTheme.colorScheme.onError,
                                        disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                                        disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                                    )
                                ) {
                                    Icon(Icons.Outlined.Remove, "Subtract")
                                }

                                FilledIconButton(
                                    onClick = {
                                        if (!isStrictTurnMode && !isCurrentTurn) onSetTurn?.invoke()
                                        val points = scoreInput.toIntOrNull() ?: 0
                                        onAdd(points)
                                        onScoreInputChange("")
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight(),
                                    shape = RoundedCornerShape(12.dp),
                                    enabled = canEdit && scoreInput.isNotEmpty(),
                                    colors = IconButtonDefaults.filledIconButtonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = MaterialTheme.colorScheme.onPrimary,
                                        disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                                        disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                                    )
                                ) {
                                    Icon(Icons.Outlined.Add, "Add")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Compact inline tooltip displayed inside the score input box when "0" is entered.
 * Informs the user that a 0-point turn will be registered without altering card height
 * or covering input controls.
 */
@Composable
private fun ZeroPointsTooltip(modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.85f),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.35f)),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                modifier = Modifier.size(11.dp),
                tint = MaterialTheme.colorScheme.secondary
            )
            Spacer(Modifier.width(3.dp))
            Text(
                text = "0 pts turn",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp),
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}