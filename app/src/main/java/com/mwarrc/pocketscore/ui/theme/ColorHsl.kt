package com.mwarrc.pocketscore.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * The hue / saturation / lightness components of a colour.
 *
 * @param hue Hue in degrees, 0–360.
 * @param saturation Saturation, 0–1.
 * @param lightness Lightness, 0–1.
 */
data class Hsl(val hue: Float, val saturation: Float, val lightness: Float)

/**
 * Converts this colour to its HSL components.
 *
 * The conversion runs on the sRGB-encoded channels - the same space CSS's
 * `hsl()` uses - so the result round-trips through [Color.hsl] without drift.
 */
fun Color.toHsl(): Hsl {
    val r = red
    val g = green
    val b = blue
    val maxChannel = maxOf(r, g, b)
    val minChannel = minOf(r, g, b)
    val delta = maxChannel - minChannel

    val lightness = (maxChannel + minChannel) / 2f

    // A grey has no hue to speak of, so report it as fully desaturated and
    // leave the hue at zero rather than dividing by a zero delta.
    if (delta == 0f) {
        return Hsl(hue = 0f, saturation = 0f, lightness = lightness)
    }

    val saturation = if (lightness > 0.5f) {
        delta / (2f - maxChannel - minChannel)
    } else {
        delta / (maxChannel + minChannel)
    }

    val rawHue = when (maxChannel) {
        r -> 60f * (((g - b) / delta) % 6f)
        g -> 60f * (((b - r) / delta) + 2f)
        else -> 60f * (((r - g) / delta) + 4f)
    }

    return Hsl(
        hue = if (rawHue < 0f) rawHue + 360f else rawHue,
        saturation = saturation,
        lightness = lightness,
    )
}

/**
 * Returns this colour with its hue replaced, keeping saturation, lightness and
 * alpha.
 *
 * This is how a role can claim a colour of its own without freezing the app's
 * palette: the wallpaper still decides how light or saturated the colour is,
 * only the hue is pinned.
 */
fun Color.withHue(hueDegrees: Float): Color {
    val hsl = toHsl()
    return Color.hsl(hueDegrees, hsl.saturation, hsl.lightness, alpha)
}

/**
 * Returns this colour with its saturation replaced, keeping hue, lightness and
 * alpha.
 */
fun Color.withSaturation(saturation: Float): Color {
    val hsl = toHsl()
    return Color.hsl(hsl.hue, saturation.coerceIn(0f, 1f), hsl.lightness, alpha)
}

/**
 * Returns this colour with its hue and saturation both replaced, keeping
 * lightness and alpha.
 *
 * Replacing both at once matters: changing the hue of a grey leaves it grey,
 * so a saturation floor applied afterwards would land on the wrong hue.
 */
fun Color.withHueAndSaturation(hueDegrees: Float, saturation: Float): Color {
    val hsl = toHsl()
    return Color.hsl(hueDegrees, saturation.coerceIn(0f, 1f), hsl.lightness, alpha)
}
