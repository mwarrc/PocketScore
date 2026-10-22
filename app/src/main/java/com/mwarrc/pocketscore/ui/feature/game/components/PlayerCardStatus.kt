package com.mwarrc.pocketscore.ui.feature.game.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Group
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * The single source of truth for how a player's state is presented on the
 * game screen.
 *
 * Both [ActivePlayerCard] (the focused/list card) and [PassivePlayerCard]
 * (the grid tile) resolve their status through this, so the two cards can no
 * longer drift apart in label, colour, or shape. Previously each card carried
 * its own copy of this logic and they had diverged — different badge shapes,
 * different zero colours, and different icon choices for the same states.
 */
@Immutable
data class PlayerCardStatus(
    /** Short badge label, or null when the player has no notable status. */
    val label: String?,
    val icon: ImageVector?,
    val badgeContainer: Color,
    val badgeContent: Color,
    /**
     * Hairline edge for the badge. Optional, but strongly recommended whenever
     * [badgeContainer] is a surface token — surface containers barely differ
     * from each other in luminance, so an outline is what actually makes the
     * badge legible against the card in both light and dark mode.
     */
    val badgeBorder: Color? = null,
    /** Accent used for the card's border and subtle accents. */
    val accent: Color,
    /** True when this is the player whose turn it is. */
    val isTurn: Boolean = false,
    /** True when this player is last place (or tied for it). */
    val isLoser: Boolean = false,
    /** True when this player is leading (or tied for first). */
    val isLeader: Boolean = false,
    /** True when this player is out of contention (pool tracking only). */
    val isEliminated: Boolean = false,
) {
    val hasBadge: Boolean get() = label != null
}

/**
 * Resolves every status flag into one coherent presentation.
 *
 * Priority order matters and is deliberate: eliminated outranks everything
 * (that player can no longer act), then the lasting role callouts — leader,
 * loser — and only then the current turn.
 *
 * The turn comes LAST on purpose. It previously outranked the callouts, which
 * meant tapping a player (moving the turn to them) swapped their "LOSER" /
 * "LEADER" pill out for the turn presentation — the status literally changed
 * on click. A role is a lasting property of the player; whose turn it is is
 * transient, and it is already communicated by the card fill and border. The
 * role branches below still receive [isTurn] = `isCurrentTurn` so turn-aware
 * styling remains possible without erasing the role label.
 */
@Composable
fun resolvePlayerCardStatus(
    isLeader: Boolean,
    isTie: Boolean,
    isLoser: Boolean,
    isLoserTie: Boolean,
    isEliminated: Boolean,
    isCurrentTurn: Boolean,
): PlayerCardStatus {
    val scheme = MaterialTheme.colorScheme

    return when {
        // Elimination wins the status, but it is no longer a badge: the
        // "Eliminated" wording is a plain red label under the score, so
        // there is nothing here for a corner badge to say. Leaving a label
        // and a block glyph here would resurrect the old solid ELIMINATED
        // stamp the redesign removed.
        //
        // The loser flag is still carried through, because being out of the
        // match and being last place are independent facts and the cards
        // render both.
        isEliminated -> PlayerCardStatus(
            label = null,
            icon = null,
            badgeContainer = scheme.errorContainer,
            badgeContent = scheme.onErrorContainer,
            accent = scheme.error,
            isTurn = isCurrentTurn,
            isLoser = isLoser,
            isEliminated = true,
        )

        isLeader -> PlayerCardStatus(
            label = if (isTie) "TIED LEADER" else "LEADER",
            icon = if (isTie) Icons.Default.Group else Icons.Default.EmojiEvents,
            badgeContainer = scheme.secondaryContainer,
            badgeContent = scheme.onSecondaryContainer,
            accent = scheme.tertiary,
            isTurn = isCurrentTurn,
            isLeader = true,
        )

        isLoser -> PlayerCardStatus(
            label = if (isLoserTie) "TIED LOSER" else "LOSER",
            icon = Icons.AutoMirrored.Filled.TrendingDown,
            // Grey-on-grey trap: this used to be surfaceVariant — the exact
            // same token the loser's card fill uses, so the badge vanished
            // into its own background. The surfaceContainer* tokens sit
            // within a few percent of each other in both modes, so stacking
            // one on another can never guarantee contrast either.
            //
            // Instead the fill keeps surfaceVariant (solid) and the outline
            // token does the separating: it's contrast-checked against
            // surfaces by definition, so this stays legible in light and dark
            // alike.
            badgeContainer = scheme.surfaceVariant,
            badgeBorder = scheme.outline,
            badgeContent = scheme.onSurfaceVariant,
            accent = scheme.outlineVariant,
            isTurn = isCurrentTurn,
            isLoser = true,
        )

        // Plain player whose turn it is: no badge at all. The fill and border
        // carry the turn; nothing else needs to shout about it.
        isCurrentTurn -> PlayerCardStatus(
            label = null,
            icon = null,
            badgeContainer = scheme.primary,
            badgeContent = scheme.onPrimary,
            accent = scheme.primary,
            isTurn = true,
        )

        else -> PlayerCardStatus(
            label = null,
            icon = null,
            badgeContainer = scheme.surfaceVariant,
            badgeContent = scheme.onSurfaceVariant,
            accent = scheme.outlineVariant,
        )
    }
}

/**
 * Presentation for a recorded score change.
 *
 * Colours come from theme tokens rather than the hardcoded literals the cards
 * used to carry (`0xFF4CAF50`, `0x31F1B1FF`, `0xFFE65100`, `0xFFFBC02D`), which
 * bypassed the theme and gave the same "zero" state three different colours
 * across two files.
 *
 * Every pair below is a solid M3 container/on-container pairing — no alpha
 * blending at all. The previous version mixed containers with
 * `copy(alpha = 0.5f)` and derived its own border from the text colour at 35%
 * alpha, which is precisely the low-alpha pattern that made dark mode
 * unreadable elsewhere in this app: the resulting contrast against the card
 * behind it is unpredictable in both modes and under Material You palettes.
 */
@Immutable
data class PointsChangeStyle(
    val text: String,
    val content: Color,
    val container: Color,
    val border: Color,
)

@Composable
fun resolvePointsChangeStyle(
    pointsValue: Int,
    isUndo: Boolean,
): PointsChangeStyle {
    val scheme = MaterialTheme.colorScheme
    val actual = if (isUndo) -pointsValue else pointsValue

    val text = when {
        isUndo && actual > 0 -> "Undo +$actual"
        isUndo && actual < 0 -> "Undo $actual"
        isUndo -> "Undo 0"
        pointsValue > 0 -> "+$pointsValue"
        pointsValue < 0 -> "$pointsValue"
        else -> "0"
    }

    // Gain / loss / neutral as solid container pairs. The undo case is treated
    // as neutral rather than tinted, because an undo isn't a gain or a loss —
    // it's the reversal of one, and the text already says so.
    val container = when {
        isUndo -> scheme.surfaceVariant
        pointsValue > 0 -> scheme.tertiaryContainer
        pointsValue < 0 -> scheme.errorContainer
        else -> scheme.surfaceVariant
    }
    val content = when {
        isUndo -> scheme.onSurfaceVariant
        pointsValue > 0 -> scheme.onTertiaryContainer
        pointsValue < 0 -> scheme.onErrorContainer
        else -> scheme.onSurfaceVariant
    }

    return PointsChangeStyle(
        text = text,
        content = content,
        container = container,
        // The surface tokens barely differ in luminance from one another in
        // either mode, so the surfaceVariant chip gets the outline token as
        // its separator; the tinted containers separate on their own.
        border = if (isUndo || pointsValue == 0) scheme.outline else container,
    )
}
