package com.mwarrc.pocketscore.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.mwarrc.pocketscore.domain.model.AppFont

/**
 * Dark color scheme for PocketScore.
 */
private val DarkColorScheme = darkColorScheme(
    primary = PrimaryDark,
    secondary = SecondaryDark,
    tertiary = TertiaryDark
)

/**
 * Light color scheme for PocketScore.
 */
private val LightColorScheme = lightColorScheme(
    primary = PrimaryLight,
    secondary = SecondaryLight,
    tertiary = TertiaryLight
)

/**
 * PocketScore Material 3 theme.
 * 
 * Supports:
 * - Light and dark themes
 * - Dynamic color on Android 12+ (Material You)
 * - App-wide font selection with system font as the default
 * - Edge-to-edge display with proper system bar styling
 * 
 * @param darkTheme Whether to use dark theme (defaults to system setting)
 * @param dynamicColor Whether to use dynamic colors on Android 12+
 * @param appFont The selected app-wide font (defaults to the system font)
 * @param content Content to theme
 */
@Composable
fun PocketScoreTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    appFont: AppFont = AppFont.SYSTEM,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) {
                dynamicDarkColorScheme(context)
            } else {
                dynamicLightColorScheme(context)
            }
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val typography = remember(appFont) { pocketScoreTypography(appFont.fontFamily) }
    
    // Update system bars for edge-to-edge display
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? android.app.Activity)?.window
            window?.let {
                // System bar transparency is handled by enableEdgeToEdge() in Activity
                
                WindowCompat.getInsetsController(it, view).apply {
                    isAppearanceLightStatusBars = !darkTheme
                    isAppearanceLightNavigationBars = !darkTheme
                }
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        content = {
            Surface(
                color = MaterialTheme.colorScheme.background,
                content = content
            )
        }
    )
}