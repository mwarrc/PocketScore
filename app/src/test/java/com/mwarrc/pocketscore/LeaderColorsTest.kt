package com.mwarrc.pocketscore.ui.feature.game.components

import androidx.compose.ui.graphics.Color
import com.mwarrc.pocketscore.ui.theme.toHsl
import com.mwarrc.pocketscore.ui.theme.withHue
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests for the leader's colour resolution.
 *
 * The leader card has to be told apart from the active player's card, and the
 * active player's card follows the wallpaper. These tests pin the two rules
 * that make that possible: violet is the default, and it steps aside when the
 * primary is already in the violet family.
 */
class LeaderColorsTest {

    @Test
    fun `violet is the leader hue when the primary is far away`() {
        // A green wallpaper - the case that started all this.
        assertEquals(285f, resolveLeaderHue(140f), 0.01f)
    }

    @Test
    fun `violet is the leader hue when the primary is grey`() {
        // The app's non-dynamic fallback primary is almost black.
        assertEquals(285f, resolveLeaderHue(0f), 0.01f)
    }

    @Test
    fun `the leader hue moves away from a violet primary`() {
        val hue = resolveLeaderHue(285f)
        assertTrue("expected the leader to leave the violet family, was $hue", hue != 285f)
    }

    @Test
    fun `the leader hue moves away from a magenta primary`() {
        val hue = resolveLeaderHue(320f)
        assertTrue("expected the leader to leave the primary's family, was $hue", hue != 285f)
    }

    @Test
    fun `the leader hue stays a usable distance from the primary`() {
        val primaries = listOf(0f, 30f, 90f, 140f, 180f, 220f, 260f, 285f, 320f, 350f)
        primaries.forEach { primary ->
            val leader = resolveLeaderHue(primary)
            val diff = kotlin.math.abs(leader - primary) % 360f
            val distance = minOf(diff, 360f - diff)
            assertTrue(
                "leader hue $leader is only $distance degrees from primary $primary",
                distance >= 40f,
            )
        }
    }

    @Test
    fun `the leader hue survives out of range input`() {
        // -220 and 500 are the same hue as 140 once normalised.
        assertEquals(resolveLeaderHue(140f), resolveLeaderHue(-220f), 0.01f)
        assertEquals(resolveLeaderHue(140f), resolveLeaderHue(500f), 0.01f)
    }

    @Test
    fun `a colour converts to hsl`() {
        val red = Color(0xFFFF0000).toHsl()
        assertEquals(0f, red.hue, 0.5f)
        assertEquals(1f, red.saturation, 0.01f)
        assertEquals(0.5f, red.lightness, 0.01f)

        val lime = Color(0xFF00FF00).toHsl()
        assertEquals(120f, lime.hue, 0.5f)

        val blue = Color(0xFF0000FF).toHsl()
        assertEquals(240f, blue.hue, 0.5f)
    }

    @Test
    fun `a grey has no saturation`() {
        val grey = Color(0xFF808080).toHsl()
        assertEquals(0f, grey.saturation, 0.01f)
    }

    @Test
    fun `hsl round trips through the hue helper`() {
        val original = Color(0xFF3F51B5)
        val rotated = original.withHue(285f)
        assertEquals(285f, rotated.toHsl().hue, 0.5f)
        // Only the hue moves - the wallpaper still decides how light or
        // saturated the leader's colour is.
        assertEquals(original.toHsl().lightness, rotated.toHsl().lightness, 0.01f)
        assertEquals(original.toHsl().saturation, rotated.toHsl().saturation, 0.01f)
    }

    @Test
    fun `the leader tint keeps the base colour underneath`() {
        val leader = LeaderColors(
            container = Color(0xFF7C4DFF),
            content = Color(0xFF2A0A57),
            chipContainer = Color(0xFF2A0A57),
            chipContent = Color(0xFF7C4DFF),
            accent = Color(0xFF7C4DFF),
            starTint = Color(0xFF2A0A57),
        )
        val base = Color(0xFFA8F0C0)
        val tinted = leader.tint(base)

        // The wash is meant to be a tint: the base colour still shows through.
        assertTrue("tint should not be the base", tinted != base)
        assertTrue("tint should not be the full leader colour", tinted != leader.container)
        // Green survives the violet wash.
        assertTrue("green channel should survive the wash", tinted.green > tinted.red)
    }
}
