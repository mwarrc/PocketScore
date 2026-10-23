package com.mwarrc.pocketscore.ui.feature.game.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Shared status indicator for both player cards.
 *
 * Minimal and icon-based: no label text, just the glyph in a tinted disc. The
 * label still reaches accessibility via [contentDescription]. A plain player
 * whose turn it is resolves to no badge at all - the card fill and border
 * carry the turn - so nothing extra is needed here. Leader / loser /
 * eliminated roles show through regardless of whose turn it is, because the
 * role branches in [resolvePlayerCardStatus] outrank the turn.
 */
@Composable
fun PlayerStatusBadge(
    status: PlayerCardStatus,
    modifier: Modifier = Modifier,
) {
    if (!status.hasBadge || status.icon == null) return

    // Solid container, deliberately not alpha-blended. A translucent fill on
    // top of an arbitrary card colour produces unpredictable contrast - it's
    // the exact pattern that made this app's dark mode unreadable before. Each
    // container here is paired with its matching on*Content token, so the pair
    // is guaranteed legible in light and dark mode (and under Material You
    // wallpaper palettes, since these are dynamic tokens, not fixed colours).
    Surface(
        color = status.badgeContainer,
        contentColor = status.badgeContent,
        shape = CircleShape,
        modifier = modifier,
    ) {
        Icon(
            imageVector = status.icon,
            contentDescription = status.label,
            modifier = Modifier
                .padding(4.dp)
                .size(12.dp),
        )
    }
}

/**
 * Radius of the theme's small shape, as a plain [Dp] so it can be reused for
 * the edge-tab chips (which round only one corner and therefore can't take the
 * shape itself). Falls back to M3's stock 8dp when the corner isn't a fixed
 * size (e.g. a percentage-based corner from a fully custom theme).
 */
internal fun Shape.smallCornerRadius(density: Density): Dp {
    val corner = (this as? CornerBasedShape)?.topStart ?: return 8.dp
    return with(density) { corner.toPx(Size.Unspecified, this).toDp() }
}

/**
 * A role a player can hold, independent of [PlayerCardStatus]'s single
 * "primary" status.
 */
enum class PlayerCardRole { LEADER, LOSER }

/**
 * Which corner of a card an edge tab is moulded into. Controls where the tab
 * stays square against the card's own edge.
 */
enum class PlayerRoleChipCorner {
    /** Bottom-left tab - the leader/loser role slot on a grid tile. */
    BOTTOM_START,
}

/**
 * Strokes only the top and end edges of a bottom-start corner tab.
 *
 * The tab sits flush in the card's bottom-left corner, so its left and bottom
 * edges already coincide with the card's own border. Stroking all four sides
 * there draws a second hairline just inside the card's border (and, on an
 * eliminated card, in a different colour than the card's red border). Drawing
 * only the two edges that face into the card is what makes the tab read as
 * moulded into it.
 */
private fun Modifier.topEndBorder(
    width: Dp,
    color: Color,
    topEndRadius: Dp,
): Modifier = drawWithContent {
    drawContent()
    val stroke = width.toPx()
    if (stroke <= 0f || size.width <= 0f || size.height <= 0f) return@drawWithContent
    val radius = topEndRadius.toPx().coerceAtMost((size.height - stroke).coerceAtLeast(0f))
    val inset = stroke / 2f
    val path = Path().apply {
        moveTo(0f, inset)
        lineTo(size.width - radius, inset)
        if (radius > 0f) {
            arcTo(
                rect = Rect(
                    left = size.width - radius,
                    top = inset,
                    right = size.width - inset,
                    bottom = radius + inset,
                ),
                startAngleDegrees = -90f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false,
            )
        }
        lineTo(size.width - inset, size.height)
    }
    drawPath(path = path, color = color, style = Stroke(width = stroke))
}

/**
 * Role label chip: "LEADER" / "TIED LEADER" and "LOSER" / "TIED LOSER".
 *
 * Two presentations, identical geometry and type:
 *  - **Edge tab** (the default, no [shape]): flush into the card's bottom-left
 *    corner so it reads as moulded into the card. Only the top and end edges
 *    are stroked, so the tab's outline merges with the card's own border on the
 *    two edges they share instead of drawing a second line. Geometry follows
 *    the redesigned eliminated card: 20dp tall, 8dp start / 9dp end padding,
 *    4dp icon gap, 12dp glyph, 10sp bold uppercase with 0.4sp tracking, and a
 *    10dp top-end / 13dp bottom-start corner.
 *  - **Free-standing chip** ([shape] supplied): the focused card's chips sit in
 *    the content flow, so they get a plain rounded pill and a full border.
 *
 * The text label is always rendered. An earlier version dropped it for tied
 * roles to save width on a narrow tile, which left two TIED leaders showing a
 * bare group glyph with no way to tell what they were tied for. "TIED LEADER"
 * fits the tile comfortably, so the word stays.
 *
 * @param role Which role this chip represents.
 * @param isTie Whether the role is shared with another player (renders the
 *   "TIED …" wording and the group icon instead of the single-player glyph).
 * @param corner Which card corner an edge tab is moulded into.
 */
@Composable
fun PlayerRoleChip(
    role: PlayerCardRole,
    isTie: Boolean = false,
    modifier: Modifier = Modifier,
    shape: Shape? = null,
    corner: PlayerRoleChipCorner = PlayerRoleChipCorner.BOTTOM_START,
) {
    val scheme = MaterialTheme.colorScheme

    // Leader inverts against the card: the leader's own fill IS the leader
    // container, so a same-token chip would vanish into it. Using the card's
    // ink as the chip fill and the container as its ink keeps the pair
    // contrast-checked in both modes and reads as the card's deliberate accent
    // (the reference does the same inversion on its leader card). Loser keeps a
    // neutral surface chip, separated by its outline the same way the status
    // badge is.
    //
    // The leader chip takes its colours from the same resolved palette the
    // card is filled with, so the chip and the card can never disagree about
    // which hue leads.
    val leaderColors = rememberLeaderColors()

    val container: Color
    val content: Color
    val border: Color
    val label: String
    val icon: ImageVector

    when (role) {
        PlayerCardRole.LEADER -> {
            container = leaderColors.chipContainer
            content = leaderColors.chipContent
            border = leaderColors.accent.copy(alpha = 0.35f)
            label = if (isTie) "TIED LEADER" else "LEADER"
            icon = if (isTie) Icons.Default.Group else Icons.Default.EmojiEvents
        }

        PlayerCardRole.LOSER -> {
            container = scheme.surfaceContainerHighest
            content = scheme.onSurfaceVariant
            border = scheme.outlineVariant
            label = if (isTie) "TIED LOSER" else "LOSER"
            icon = if (isTie) Icons.Default.Group else Icons.AutoMirrored.Filled.TrendingDown
        }
    }

    val isEdgeTab = shape == null
    val topEndRadius = 10.dp
    val resolvedShape = shape ?: when (corner) {
        PlayerRoleChipCorner.BOTTOM_START -> RoundedCornerShape(
            topEnd = topEndRadius,
            bottomStart = 13.dp,
        )
    }

    Surface(
        color = container,
        contentColor = content,
        shape = resolvedShape,
        // The tab draws its own two-sided border below; a full Surface border
        // would double up with the card's border on the shared edges.
        border = if (isEdgeTab) null else BorderStroke(1.dp, border),
        modifier = modifier
            .then(if (isEdgeTab) Modifier.height(20.dp) else Modifier)
            .then(
                if (isEdgeTab) {
                    Modifier.topEndBorder(1.dp, border, topEndRadius)
                } else {
                    Modifier
                }
            ),
    ) {
        Row(
            modifier = Modifier.padding(
                start = 8.dp,
                end = 9.dp,
                // The edge tab has a fixed 20dp height and centres its row
                // inside it; the free-standing pill sizes to its content and
                // needs its own vertical padding.
                top = if (isEdgeTab) 0.dp else 3.dp,
                bottom = if (isEdgeTab) 0.dp else 3.dp,
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(12.dp),
            )
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.4.sp,
                maxLines = 1,
            )
        }
    }
}

/**
 * Shared chip for the most recent score change on a player.
 *
 * Same geometry everywhere, so the "last points" indicator reads identically
 * in the focused card and in a grid tile. Both variants are solid M3
 * container/on-container pairs from [resolvePointsChangeStyle] - the older
 * version alpha-blended its fills and derived a border from the text colour,
 * which is why it looked washed out next to everything else on the card.
 */
@Composable
fun PointsChangeChip(
    style: PointsChangeStyle,
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.small,
) {
    Surface(
        color = style.container,
        contentColor = style.content,
        shape = shape,
        border = BorderStroke(1.dp, style.border),
        modifier = modifier,
    ) {
        Text(
            text = style.text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        )
    }
}

/**
 * Watermark for an eliminated player: a large outlined lock centred behind the
 * card's content, tinted with the accent red at 11% opacity.
 *
 * Shared by [ActivePlayerCard] and [PassivePlayerCard] so both layouts mark
 * elimination identically.
 *
 * This deliberately replaced a 104dp solid block glyph at 40–45% alpha plus a
 * full-tile film. That version was the loudest thing on the scoreboard: it read
 * as an error alert, competed with the score, and required the tile to fade its
 * own text to stay legible. Elimination is a settled, permanent state rather
 * than an alarm, so the mark is now quiet - a lock (the player is out of the
 * game, points are locked) at a whisper of the accent red, with no film over
 * the card at all.
 *
 * Decoration only: no layout impact, no interaction, and excluded from the
 * accessibility tree because the "Eliminated" label beside it already states
 * the fact in words.
 *
 * @param isCompact Smaller glyph for a grid tile, which has far less room than
 *   the focused card.
 */
@Composable
fun BoxScope.EliminatedWatermark(isCompact: Boolean) {
    Icon(
        imageVector = Icons.Outlined.Lock,
        contentDescription = null,
        modifier = Modifier
            .align(Alignment.Center)
            // Nudged just above the optical centre, matching the reference:
            // dead-centre reads as slightly low once the score sits below it.
            .offset(y = if (isCompact) (-2).dp else (-4).dp)
            .size(if (isCompact) 68.dp else 104.dp)
            .alpha(0.11f),
        tint = MaterialTheme.colorScheme.error,
    )
}

/**
 * One star in the leader's field.
 *
 * @param x Horizontal position as a fraction of the card's width.
 * @param y Vertical position as a fraction of the card's height.
 * @param size Diameter.
 * @param alpha Opacity.
 */
private data class LeaderStar(
    val x: Float,
    val y: Float,
    val size: Dp,
    val alpha: Float,
)

/**
 * Stars for the focused card, written in the composition's *start-dense*
 * arrangement and mirrored by [flipped] before they are drawn.
 *
 * Positions are fractions of the card rather than fixed offsets, so the same
 * layout scales from a narrow phone to a tablet, and so the field can be kept
 * clear of the two things that must stay legible: the role chip under the name
 * and the score-change chip under the score.
 */
private val LeaderStars = listOf(
    LeaderStar(x = 0.30f, y = 0.10f, size = 18.dp, alpha = 0.40f),
    LeaderStar(x = 0.20f, y = 0.26f, size = 11.dp, alpha = 0.30f),
    LeaderStar(x = 0.40f, y = 0.22f, size = 9.dp, alpha = 0.26f),
    LeaderStar(x = 0.26f, y = 0.42f, size = 14.dp, alpha = 0.34f),
    LeaderStar(x = 0.48f, y = 0.36f, size = 10.dp, alpha = 0.26f),
    LeaderStar(x = 0.06f, y = 0.88f, size = 13.dp, alpha = 0.36f),
    LeaderStar(x = 0.95f, y = 0.60f, size = 12.dp, alpha = 0.32f),
    LeaderStar(x = 0.70f, y = 0.62f, size = 9.dp, alpha = 0.24f),
    LeaderStar(x = 0.88f, y = 0.86f, size = 10.dp, alpha = 0.26f),
)

/**
 * Stars for the grid tile - the same mirrored arrangement, scaled down.
 *
 * The tile is far smaller than the focused card, so the field is sparser and
 * every star is smaller. It is also kept clear of the bottom-left corner,
 * where the role chip is moulded into the tile, and of the right-hand edge
 * around the vertical centre, where the score-change chip sits.
 */
private val CompactLeaderStars = listOf(
    LeaderStar(x = 0.12f, y = 0.12f, size = 14.dp, alpha = 0.38f),
    LeaderStar(x = 0.26f, y = 0.20f, size = 9.dp, alpha = 0.28f),
    LeaderStar(x = 0.10f, y = 0.34f, size = 10.dp, alpha = 0.30f),
    LeaderStar(x = 0.34f, y = 0.44f, size = 12.dp, alpha = 0.34f),
    LeaderStar(x = 0.18f, y = 0.66f, size = 11.dp, alpha = 0.30f),
    LeaderStar(x = 0.28f, y = 0.84f, size = 9.dp, alpha = 0.26f),
    LeaderStar(x = 0.78f, y = 0.14f, size = 9.dp, alpha = 0.28f),
    LeaderStar(x = 0.88f, y = 0.56f, size = 10.dp, alpha = 0.30f),
)

/**
 * Mirrors a star layout across the card's vertical axis.
 *
 * The field used to be pinned to the start edge, which put its biggest star
 * right on top of the role chip in the tile's bottom-left corner. Mirroring it
 * moves the weight to the trailing side and leaves the chip - and the score
 * it has to stay legible - alone.
 */
private fun List<LeaderStar>.flipped(): List<LeaderStar> = map { it.copy(x = 1f - it.x) }

/**
 * Decorative star field for leaders.
 *
 * Rendered in both [ActivePlayerCard] and [PassivePlayerCard] when a player is
 * leading. The stars take the leader palette's ink so they always belong to
 * the card they sit on, and their placement is proportional to the card, so
 * the field holds together from a small grid tile up to a full-width focused
 * card.
 */
@Composable
fun BoxScope.LeaderStarField(
    isCurrentTurn: Boolean,
    modifier: Modifier = Modifier,
    isCompact: Boolean = false,
) {
    val leaderColors = rememberLeaderColors()
    val starTint = leaderColors.starTint

    val stars = remember(isCompact) {
        (if (isCompact) CompactLeaderStars else LeaderStars).flipped()
    }

    // A leader who is also the active player gets a card that blends the two
    // colours, so the stars are nudged up a little to stay readable on it.
    val alphaBoost = if (isCurrentTurn) 1.15f else 1f

    BoxWithConstraints(
        modifier = modifier
            .matchParentSize()
            .clip(RoundedCornerShape(16.dp))
    ) {
        val width = maxWidth
        val height = maxHeight
        if (width != Dp.Infinity && height != Dp.Infinity) {
            stars.forEach { star ->
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    modifier = Modifier
                        .offset(
                            x = width * star.x - star.size / 2,
                            y = height * star.y - star.size / 2,
                        )
                        .size(star.size)
                        .alpha(star.alpha * alphaBoost),
                    tint = starTint,
                )
            }
        }
    }
}
