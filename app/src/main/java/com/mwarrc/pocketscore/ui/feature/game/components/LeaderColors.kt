package com.mwarrc.pocketscore.ui.feature.game.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import com.mwarrc.pocketscore.ui.theme.toHsl
import kotlin.math.abs

/**
 * Hues the leader card may claim, in order of preference.
 *
 * Violet comes first because it is the one hue the app never hands the active
 * player: the active card is `primaryContainer`, which follows the wallpaper,
 * and Material You rarely produces a violet primary. It is still only a
 * preference - see [resolveLeaderHue].
 */
private val LeaderHueCandidates = listOf(
    285f, // violet
    45f,  // amber
    200f, // cyan
    330f, // magenta
)

/**
 * How far apart two hues have to be before they read as different colours.
 */
private const val MinLeaderHueDistance = 55f

/**
 * Floor for the leader palette's saturation, so the card stays cheerful even
 * when the wallpaper's palette is washed out.
 */
private const val MinLeaderSaturation = 0.40f

/**
 * The leader card's palette.
 *
 * The leader used to be filled with `secondaryContainer`, which Material You
 * generates as a near-neighbour of the primary - on a green wallpaper that put
 * the leader and the active player in the same family and the two cards became
 * impossible to tell apart. The leader now claims a hue of its own.
 */
@Immutable
data class LeaderColors(
    /** The card's surface. */
    val container: Color,
    /** Text ink on the container. */
    val content: Color,
    /** Rank chip fill - the ink, so the chip inverts against the card. */
    val chipContainer: Color,
    /** Rank chip text - the container colour. */
    val chipContent: Color,
    /** Card border. */
    val accent: Color,
    /** Tint for the decorative star field. */
    val starTint: Color,
)

/**
 * Chooses the hue the leader card should use.
 *
 * Violet is the default because it is the one hue the app never gives the
 * active player. But the wallpaper owns the primary, so a device themed in
 * violet would hand the leader and the active player the same family - the
 * exact confusion the leader colour exists to fix. When that happens the next
 * candidate hue is used instead.
 *
 * @param primaryHue Hue of the theme's primary colour, in degrees.
 * @return The hue to use for the leader, in degrees.
 */
internal fun resolveLeaderHue(primaryHue: Float): Float {
    val normalizedPrimary = ((primaryHue % 360f) + 360f) % 360f
    return LeaderHueCandidates.firstOrNull { candidate ->
        hueDistance(candidate, normalizedPrimary) >= MinLeaderHueDistance
    } ?: LeaderHueCandidates.maxBy { hueDistance(it, normalizedPrimary) }
}

/**
 * Shortest distance between two hues, in degrees.
 */
private fun hueDistance(a: Float, b: Float): Float {
    val diff = abs(a - b) % 360f
    return minOf(diff, 360f - diff)
}

/**
 * Resolves the leader card's palette from the current theme.
 *
 * Nothing here is a fixed colour: the wallpaper still decides how light or
 * saturated the leader's hue is, and Material You still decides whether the
 * card is light or dark. Only the hue is claimed, and only so the leader can
 * never be mistaken for the active player.
 */
@Composable
fun rememberLeaderColors(): LeaderColors {
    val scheme = MaterialTheme.colorScheme
    return remember(scheme) {
        val hue = resolveLeaderHue(scheme.primary.toHsl().hue)

        val containerSeed = scheme.tertiaryContainer.toHsl()
        val inkSeed = scheme.onTertiaryContainer.toHsl()
        val accentSeed = scheme.tertiary.toHsl()

        val container = Color.hsl(
            hue = hue,
            saturation = maxOf(containerSeed.saturation, MinLeaderSaturation),
            lightness = containerSeed.lightness,
        )
        val content = Color.hsl(
            hue = hue,
            saturation = maxOf(inkSeed.saturation, MinLeaderSaturation),
            lightness = inkSeed.lightness,
        )
        val accent = Color.hsl(
            hue = hue,
            saturation = maxOf(accentSeed.saturation, MinLeaderSaturation),
            lightness = accentSeed.lightness,
        )

        LeaderColors(
            container = container,
            content = content,
            chipContainer = content,
            chipContent = container,
            accent = accent,
            starTint = content,
        )
    }
}

/**
 * Mixes a shade of the leader's violet into [base].
 *
 * Used when the leader is also the active player. The card keeps the active
 * player's colour and takes a tint of the leader's on top, so neither state
 * replaces the other and neither shouts: a gradient between the two read as
 * a third, louder colour, and a straight 50/50 mix turned the card muddy.
 * A light violet washed over the active colour is the quietest way to say
 * "this player is both".
 */
fun LeaderColors.tint(base: Color, amount: Float = LeaderTintAmount): Color =
    container.copy(alpha = amount).compositeOver(base)

/**
 * How much of the leader's violet is washed into the active player's colour.
 *
 * Deliberately small. The leader still has to be obvious, but the card should
 * look like the active card with a violet cast, not like a third state.
 */
private const val LeaderTintAmount = 0.28f
