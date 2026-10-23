package com.mwarrc.pocketscore.ui.feature.game.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.mwarrc.pocketscore.domain.logic.EliminationRules
import com.mwarrc.pocketscore.domain.model.Player
import com.mwarrc.pocketscore.ui.components.AutoSizeText

/**
 * Compact read-only card for a player in Grid layout.
 *
 * Refresh notes: the decorative star field (seven hand-positioned icons with
 * magic offsets that only lined up at one width) is gone in favour of a single
 * proportional accent. Status and points change now come from the shared
 * [PlayerStatusBadge] / [PointsChangeChip] so a tile and the focused card can't
 * disagree. The height is strict and identical for every tile, derived from the
 * theme so it scales rather than clipping.
 */
@Composable
fun PassivePlayerCard(
    player: Player,
    isLeader: Boolean,
    isTie: Boolean = false,
    isLoser: Boolean = false,
    isLoserTie: Boolean = false,
    isCurrent: Boolean,
    isActualTurn: Boolean,
    onClick: () -> Unit,
    /**
     * Whether the tile can be pressed. Strict turn mode turns the grid into a
     * read-only board: the turn decides who is being scored and the header
     * card is the only place you can enter points, so a tile that isn't the
     * turn holder has nothing to offer. Leaving it tappable meant tapping
     * "the other player" and landing on a card you were not allowed to score.
     */
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
    lastPoints: Pair<Int, Boolean>? = null,
    isLastUpdated: Boolean = false,
    leaderScore: Int = 0,
    tableSum: Int = 0,
    poolBallManagementEnabled: Boolean = true,
    scoreInput: String = "",
    /**
     * Everyone still in the match. Required by [EliminationRules] to apply the
     * "nobody has scored yet" and tie-for-the-lead guards. Defaults to just this
     * player so the tile still behaves sensibly if a caller omits it.
     */
    activePlayers: List<Player> = listOf(player),
) {
    // Every tile is exactly this tall. Derived from the theme's score size so
    // it grows with the user's UI scale instead of clipping, and so a leader
    // tile (chip) and an ordinary tile end up identical. The extra space at the
    // end is the third line - the "Eliminated" label - which only some tiles
    // show. It is reserved on every tile rather than only on eliminated ones so
    // a grid row can never go ragged.
    val tileHeight = with(LocalDensity.current) {
        MaterialTheme.typography.headlineMedium.fontSize.toDp() * 1.7f + 68.dp
    }

    // Contender math: the shared rule also handles the empty-table and
    // nobody-has-scored cases, which the old inline version here did not - so a
    // tile could read ELIMINATED while the turn rotation still used it.
    val isEliminated = EliminationRules.isEliminated(
        player = player,
        activePlayers = activePlayers,
        tableSum = tableSum,
        leaderScore = leaderScore,
    ) && poolBallManagementEnabled

    val status = resolvePlayerCardStatus(
        isLeader = isLeader,
        isTie = isTie,
        isLoser = isLoser,
        isLoserTie = isLoserTie,
        isEliminated = isEliminated,
        isCurrentTurn = isActualTurn,
    )

    val scheme = MaterialTheme.colorScheme

    // Eliminated surface: a quiet red tint of the ordinary tile surface rather
    // than either the old solid grey or a solid error container. Resolved in
    // one place so both cards agree.
    val eliminated = rememberEliminatedColors(baseContainer = scheme.surfaceContainerLow)

    // The leader claims a hue of its own - violet by default - so a leader
    // card can never be mistaken for the active player's card. The two used to
    // share the primary's family (Material You derives secondary as a
    // near-neighbour of primary), which made them impossible to tell apart.
    val leader = rememberLeaderColors()

    // A leader who is also the active player keeps the active player's colour
    // and takes a shade of the leader's violet on top. A straight 50/50 mix
    // went muddy and a gradient across the card read as a third, louder
    // state; a light wash is the quiet way to say "both".
    val isLeaderAndTurn = isLeader && !isEliminated && isActualTurn

    // Cross-fade the state colours so moving the selection between tiles reads
    // as one settling change rather than an instant cut on both tiles.
    //
    // Eliminated is a red *tint*, not a red fill: the tile still reads as a
    // surface with a hint of warning, and its name/score stay legible instead
    // of having to be faded out on top of a saturated container. The loud part
    // of elimination is the lock watermark and the "Eliminated" label; the fill
    // only has to say "out of the running".
    val containerColor by animateColorAsState(
        targetValue = when {
            isEliminated -> eliminated.container
            isLeaderAndTurn -> leader.tint(scheme.primaryContainer)
            isLeader -> leader.container
            isActualTurn -> scheme.primaryContainer
            isLoser -> scheme.surfaceVariant
            isCurrent -> scheme.surfaceContainerHigh
            else -> scheme.surfaceContainerLow
        },
        animationSpec = tween(180),
        label = "tile_container",
    )

    // Eliminated ink is a muted on-surface variant - readable, but visibly
    // stepped back from the players still in the match. The score sits a little
    // quieter again than the name so the tile's hierarchy survives.
    val contentColor by animateColorAsState(
        targetValue = when {
            isEliminated -> eliminated.nameInk
            // On the blended card the ink stays the active player's, so the
            // wash reads as a tint on a familiar surface.
            isLeaderAndTurn -> scheme.onPrimaryContainer
            isLeader -> leader.content
            isActualTurn -> scheme.onPrimaryContainer
            isLoser -> scheme.onSurfaceVariant
            else -> scheme.onSurface
        },
        animationSpec = tween(180),
        label = "tile_content",
    )

    val scoreColor by animateColorAsState(
        targetValue = if (isEliminated) eliminated.scoreInk else contentColor,
        animationSpec = tween(180),
        label = "tile_score",
    )

    // Constant 1dp width; only the colour varies, so focus changes don't nudge
    // the interior by a pixel.
    val borderColor by animateColorAsState(
        targetValue = when {
            // Checked before the selection border: an eliminated tile keeps its
            // own quiet red hairline rather than borrowing the selection accent.
            isEliminated -> eliminated.border
            isLeader -> leader.accent.copy(alpha = 0.6f)
            isCurrent || isActualTurn -> status.accent
            else -> scheme.outlineVariant
        },
        animationSpec = tween(180),
        label = "tile_border",
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            // The reference states the eliminated card's accessibility
            // description as a single sentence - "Name, 14 points, eliminated"
            // - rather than letting a screen reader wander over three
            // separate text nodes. Merging the tile's contents and giving the
            // card one description does exactly that, and the description
            // takes precedence over the merged text for the announcement.
            .then(
                if (isEliminated) {
                    Modifier.semantics(mergeDescendants = true) {
                        contentDescription = "${player.name}, ${player.score} points, eliminated"
                    }
                } else {
                    Modifier
                }
            ),
        onClick = onClick,
        enabled = enabled,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, borderColor),
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor,
            // A disabled tile must still paint its own surface. Without these
            // it silently falls back to the stock M3 disabled tokens and the
            // leader/loser/turn colours are replaced by a flat grey card.
            disabledContainerColor = containerColor,
            disabledContentColor = contentColor,
        ),
        shape = RoundedCornerShape(16.dp),
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Elimination watermark - a quiet lock, matching the focused card.
            if (isEliminated) {
                EliminatedWatermark(isCompact = true)
            }

            // Leader star flourish, matching the focused card's vocabulary.
            // Same treatment in both places so leadership reads consistently.
            //
            // Gated on isLeader alone. It previously also required
            // !isActualTurn, which killed the stars as soon as the leader
            // became the current turn - leadership is a lasting property, not a
            // turn state.
            if (isLeader && !isEliminated) {
                LeaderStarField(
                    isCurrentTurn = isActualTurn || isCurrent,
                    isCompact = true,
                )
            }

            // Leading accent bar only for players without a role chip - the
            // chip carries the leader/loser signal at the bottom-left now, and
            // stacking two markers on the left edge reads as clutter.
            // Eliminated is excluded as well: its label and lock watermark
            // already say it, and an extra error-coloured bar on the left edge
            // makes the tile read as one solid red slab.
            if (!status.isLeader && !status.isLoser && !status.isEliminated) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .size(width = 3.dp, height = 44.dp)
                        .background(
                            status.accent.copy(alpha = if (status.hasBadge) 0.85f else 0.25f),
                            RoundedCornerShape(topEnd = 3.dp, bottomEnd = 3.dp),
                        )
                )
            }

            // Role chip - LEADER / TIED LEADER / LOSER / TIED LOSER - flush to
            // the bottom-left edge as an integrated tab instead of floating
            // mid-edge. The tab shape is square against the card's left and
            // bottom edges, so the chip and the card corner read as one
            // moulded piece.
            //
            // Role membership comes from the raw flags, not from [status]:
            // an eliminated player resolves to the ELIMINATED status, which
            // would otherwise swallow their LOSER role - and "bottom of the
            // table" is still true (and still worth showing) after a player is
            // out of the match. Elimination has its own label under the score,
            // so the two facts coexist instead of competing for this slot.
            //
            // The word is always shown, ties included. Dropping it for tied
            // roles left a bare glyph with no way to tell what the player was
            // tied for; "TIED LEADER" fits the tile.
            //
            // Alignment is flush to the corner with no inset, so the tab's
            // two-sided border merges into the card's own border.
            // Leader is suppressed for an eliminated player. A player on the
            // leading score can never be eliminated, so this is belt and
            // braces - but if the two ever disagree, "out of the match" is the
            // stronger claim and the tile shouldn't carry both stamps.
            val tileRole = when {
                isLeader && !isEliminated -> PlayerCardRole.LEADER
                isLoser -> PlayerCardRole.LOSER
                else -> null
            }
            if (tileRole != null) {
                PlayerRoleChip(
                    role = tileRole,
                    isTie = if (tileRole == PlayerCardRole.LEADER) isTie else isLoserTie,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .zIndex(1f),
                )
            }

            // Status mark for a plain player whose turn it is - no badge at
            // all otherwise, since the fill and border carry the turn. Leader
            // and loser are skipped because their role tab already says it, and
            // eliminated carries its own label under the score.
            if (!status.isLeader && !status.isLoser && !status.isEliminated) {
                PlayerStatusBadge(
                    status = status,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 5.dp, end = 5.dp),
                )
            }

            // Most-recent input, flush to the trailing edge as a corner
            // annotation rather than a chip floating in the middle of the tile.
            // zIndex so the leader star field drawn earlier can't paint over it.
            //
            // On an eliminated tile it drops the filled chip and becomes plain
            // text. A saturated chip is one of the loudest things on the card,
            // and the one job an eliminated card must not fail is staying
            // quiet; the value is still readable, just no longer competing with
            // the score.
            if (lastPoints != null) {
                val (pointsValue, isUndo) = lastPoints
                val style = resolvePointsChangeStyle(
                    pointsValue = pointsValue,
                    isUndo = isUndo,
                )
                if (isEliminated) {
                    Text(
                        text = style.text,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        fontWeight = FontWeight.Medium,
                        color = eliminated.nameInk,
                        maxLines = 1,
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 8.dp)
                            .zIndex(1f),
                    )
                } else {
                    PointsChangeChip(
                        style = style,
                        shape = RoundedCornerShape(
                            topStart = MaterialTheme.shapes.small.smallCornerRadius(LocalDensity.current),
                            topEnd = 0.dp,
                            bottomStart = MaterialTheme.shapes.small.smallCornerRadius(LocalDensity.current),
                            bottomEnd = 0.dp,
                        ),
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .zIndex(1f),
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    // Strict fixed height: every tile is exactly the same size
                    // regardless of name length, badge, score or last-points
                    // chip. Previously tiles sized to their content, so a
                    // leader tile (badge + status) was taller than an ordinary
                    // one and the grid rows went ragged.
                    .height(tileHeight)
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.Center,
            ) {
                AutoSizeText(
                    text = player.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = if (isActualTurn || isLeader) FontWeight.Bold else FontWeight.Medium,
                    // No extra alpha override here any more: the eliminated
                    // muting now lives in contentColor itself, so an override
                    // on the name alone would stack with it and leave the name
                    // visibly fainter than the score right below it.
                    color = contentColor,
                    modifier = Modifier.padding(horizontal = 20.dp),
                )

                AutoSizeText(
                    text = "${player.score}",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                    color = scoreColor,
                    modifier = Modifier.padding(horizontal = 20.dp),
                )

                // Plain red "Eliminated" label directly under the score. This
                // replaces the solid ELIMINATED tab that used to be bolted into
                // the top-right corner: same fact, stated once, in the reading
                // order rather than shouted from an edge.
                if (isEliminated) {
                    Text(
                        text = "Eliminated",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp),
                        fontWeight = FontWeight.Medium,
                        color = eliminated.accent,
                        maxLines = 1,
                        modifier = Modifier.padding(top = 1.dp),
                    )
                }

                // The 0-points warning used to be a separate third colour
                // (0xFFFBC02D) here and something else again on the focused
                // card. It's now just the neutral chip colour.
                if (scoreInput == "0" && !isEliminated) {
                    Spacer(Modifier.size(3.dp))
                    Text(
                        text = "no points entered",
                        style = MaterialTheme.typography.labelSmall,
                        color = scheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium,
                        fontSize = 9.sp,
                    )
                }
            }

            if (isLastUpdated) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                        .size(6.dp)
                        .background(scheme.primary, CircleShape),
                )
            }
        }
    }
}
