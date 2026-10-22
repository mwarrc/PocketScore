package com.mwarrc.pocketscore.ui.feature.game.components

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.statusBars
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Shared height sizing for every bottom sheet on the game screen.
 *
 * Before this existed each sheet capped itself independently and they had all
 * drifted apart — 0.85f of the screen in three of them, and effectively the
 * full screen in the help and calculator sheets. Sheets that open to different
 * heights feel inconsistent, and the ones that reach the top of the screen
 * leave nowhere to tap to dismiss.
 *
 * The rules now, applied everywhere:
 *  - A sheet never exceeds [GAME_SHEET_MAX_SCREEN_FRACTION] of the physical
 *    screen, so at least a quarter of the screen always stays uncovered. That
 *    uncovered strip is both the tap-to-dismiss target and the context you
 *    need to keep seeing the scoreboard behind it.
 *  - The display-cutout / status-bar inset is subtracted *once* here, so a
 *    sheet can't creep under a punch-hole camera.
 */
object GameSheetSizing {
    /**
     * Maximum share of the physical screen height any game sheet may occupy.
     * The remaining 25% stays visible for context and tap-to-dismiss.
     */
    const val GAME_SHEET_MAX_SCREEN_FRACTION = 0.75f
}

/**
 * The tallest a game-screen sheet's content may be.
 *
 * Reads the *physical* screen height rather than a Compose inset: in immersive
 * mode the window insets report zero, so DisplayMetrics is the only reliable
 * source for how much screen there actually is.
 *
 * @param fraction Share of the screen the sheet may use; defaults to the
 *   shared 75% cap. Sheets with short content simply size to their content and
 *   never reach this, so passing a larger value never makes a small sheet big.
 */
@Composable
fun rememberGameSheetMaxHeight(
    fraction: Float = GameSheetSizing.GAME_SHEET_MAX_SCREEN_FRACTION,
): Dp {
    val density = LocalDensity.current
    val context = LocalContext.current

    val trueScreenHeightDp = with(density) {
        context.resources.displayMetrics.heightPixels.toDp()
    }

    // Both insets are read because which one is non-zero depends on the device
    // and whether the status bar is currently hidden; maxOf keeps the guard
    // correct in either case without stacking them.
    val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val cutoutHeight = WindowInsets.displayCutout.asPaddingValues().calculateTopPadding()
    val topInset = maxOf(statusBarHeight, cutoutHeight)

    return trueScreenHeightDp * fraction - topInset
}
