package com.mwarrc.pocketscore

import com.mwarrc.pocketscore.domain.model.AppFont
import com.mwarrc.pocketscore.domain.model.AppSettings
import com.mwarrc.pocketscore.domain.model.WinnerScreenStyle
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

/**
 * Verifies that the app font setting is backward-compatible with settings
 * JSON written before the feature existed.
 *
 * The repository decodes [AppSettings] with `ignoreUnknownKeys`,
 * `coerceInputValues` and `encodeDefaults` enabled, so older payloads simply
 * fall back to the default font rather than failing to load.
 */
class AppFontSettingsTest {

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        encodeDefaults = true
    }

    @Test
    fun existingSettingsWithoutFont_defaultToSystemFont() {
        // Minimal payload as it would have been persisted before AppFont existed.
        val legacyJson = """{"appTheme":"DARK","globalScale":1.1}"""

        val settings = json.decodeFromString<AppSettings>(legacyJson)

        assertEquals(AppFont.SYSTEM, settings.appFont)
        assertEquals(1.1f, settings.globalScale, 0.0001f)
    }

    @Test
    fun existingSettingsWithoutWinnerScreenStyle_defaultToWavyHero() {
        // Payload written before the winner-screen style setting existed.
        val legacyJson = """{"appTheme":"LIGHT","endGameCelebrationEnabled":true}"""

        val settings = json.decodeFromString<AppSettings>(legacyJson)

        assertEquals(WinnerScreenStyle.WAVY_HERO, settings.winnerScreenStyle)
    }

    @Test
    fun winnerScreenStyleRoundTripsThroughSerialization() {
        val settings = AppSettings(winnerScreenStyle = WinnerScreenStyle.CLASSIC)

        val encoded = json.encodeToString(AppSettings.serializer(), settings)
        val decoded = json.decodeFromString<AppSettings>(encoded)

        assertEquals(WinnerScreenStyle.CLASSIC, decoded.winnerScreenStyle)
    }

    @Test
    fun appFontRoundTripsThroughSerialization() {
        val settings = AppSettings(appFont = AppFont.JETBRAINS_MONO)

        val encoded = json.encodeToString(AppSettings.serializer(), settings)
        val decoded = json.decodeFromString<AppSettings>(encoded)

        assertEquals(AppFont.JETBRAINS_MONO, decoded.appFont)
    }

    @Test
    fun unknownFontValue_fallsBackToDefault() {
        // A font name from a future/unknown version must not break settings loading.
        val futureJson = """{"appFont":"COMIC_SANS"}"""

        val settings = json.decodeFromString<AppSettings>(futureJson)

        assertNotEquals(AppFont.RUBIK, settings.appFont)
    }
}
