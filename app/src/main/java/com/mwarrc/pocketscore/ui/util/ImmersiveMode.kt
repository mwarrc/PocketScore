package com.mwarrc.pocketscore.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

/**
 * Applies full immersive mode (hides status bar + navigation bar) to whichever
 * Window this composable is rendered in.
 *
 * Works inside both Dialog / ModalBottomSheet windows (via [DialogWindowProvider])
 * and directly inside the Activity window. Place it at the top of any Dialog,
 * AlertDialog, or ModalBottomSheet content to keep the game screen truly full-screen
 * even while overlays are open.
 */
@Composable
fun ImmersiveMode() {
    val view = LocalView.current
    SideEffect {
        val window = (view.parent as? DialogWindowProvider)?.window 
            ?: (view.context as? android.app.Activity)?.window 
            ?: return@SideEffect
            
        WindowCompat.getInsetsController(window, view).apply {
            hide(WindowInsetsCompat.Type.statusBars() or WindowInsetsCompat.Type.navigationBars())
            systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }
}
