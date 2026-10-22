package com.mwarrc.pocketscore.ui.theme

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.mwarrc.pocketscore.R
import com.mwarrc.pocketscore.domain.model.AppFont

/**
 * Registry of the app-wide font families available in Display & Appearance.
 *
 * All bundled families are SIL Open Font License 1.1 and use latin subsets
 * so the whole set adds roughly 1 MB to the APK. Only four real weights are
 * bundled per family (Regular, Medium, Bold, ExtraBold); requests for other
 * weights resolve to the nearest available one.
 */

private val InterFamily = FontFamily(
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_medium, FontWeight.Medium),
    Font(R.font.inter_bold, FontWeight.Bold),
    Font(R.font.inter_extrabold, FontWeight.ExtraBold)
)

private val JetBrainsMonoFamily = FontFamily(
    Font(R.font.jetbrains_mono_regular, FontWeight.Normal),
    Font(R.font.jetbrains_mono_medium, FontWeight.Medium),
    Font(R.font.jetbrains_mono_bold, FontWeight.Bold),
    Font(R.font.jetbrains_mono_extrabold, FontWeight.ExtraBold)
)

private val MontserratFamily = FontFamily(
    Font(R.font.montserrat_regular, FontWeight.Normal),
    Font(R.font.montserrat_medium, FontWeight.Medium),
    Font(R.font.montserrat_bold, FontWeight.Bold),
    Font(R.font.montserrat_extrabold, FontWeight.ExtraBold)
)

private val PoppinsFamily = FontFamily(
    Font(R.font.poppins_regular, FontWeight.Normal),
    Font(R.font.poppins_medium, FontWeight.Medium),
    Font(R.font.poppins_bold, FontWeight.Bold),
    Font(R.font.poppins_extrabold, FontWeight.ExtraBold)
)

private val RubikFamily = FontFamily(
    Font(R.font.rubik_regular, FontWeight.Normal),
    Font(R.font.rubik_medium, FontWeight.Medium),
    Font(R.font.rubik_bold, FontWeight.Bold),
    Font(R.font.rubik_extrabold, FontWeight.ExtraBold)
)

/**
 * The Compose [FontFamily] for this option.
 * [AppFont.SYSTEM] resolves to the platform default font.
 */
val AppFont.fontFamily: FontFamily
    get() = when (this) {
        AppFont.SYSTEM -> FontFamily.Default
        AppFont.INTER -> InterFamily
        AppFont.JETBRAINS_MONO -> JetBrainsMonoFamily
        AppFont.MONTSERRAT -> MontserratFamily
        AppFont.POPPINS -> PoppinsFamily
        AppFont.RUBIK -> RubikFamily
    }

/**
 * Human-readable name shown in the font picker.
 */
val AppFont.displayName: String
    get() = when (this) {
        AppFont.SYSTEM -> "System (Default)"
        AppFont.INTER -> "Inter"
        AppFont.JETBRAINS_MONO -> "JetBrains Mono"
        AppFont.MONTSERRAT -> "Montserrat"
        AppFont.POPPINS -> "Poppins"
        AppFont.RUBIK -> "Rubik"
    }

/**
 * Short description shown underneath the font name in the picker.
 */
val AppFont.description: String
    get() = when (this) {
        AppFont.SYSTEM -> "Your device's own font"
        AppFont.INTER -> "Crisp and modern with superb numerals"
        AppFont.JETBRAINS_MONO -> "Monospaced digits that line up perfectly"
        AppFont.MONTSERRAT -> "Wide geometric numerals, premium feel"
        AppFont.POPPINS -> "Round, friendly geometric digits"
        AppFont.RUBIK -> "Softly rounded numerals with character"
    }

/**
 * Sample glyphs rendered in each font inside the picker.
 */
const val FONT_PREVIEW_DIGITS = "0123456789"
