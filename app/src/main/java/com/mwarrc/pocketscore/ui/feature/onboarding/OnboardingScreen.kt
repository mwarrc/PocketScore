package com.mwarrc.pocketscore.ui.feature.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mwarrc.pocketscore.R
import com.mwarrc.pocketscore.ui.feature.onboarding.components.FeltBallStrip
import com.mwarrc.pocketscore.ui.feature.onboarding.components.GetStartedButton
import com.mwarrc.pocketscore.ui.feature.onboarding.components.OnboardingHaptics
import com.mwarrc.pocketscore.ui.feature.onboarding.components.PointsSystemRow

// --- DATA MODEL ---

/**
 * Represents a selectable ball points system option during onboarding.
 */
data class PointsSystemOption(
    val name: String,
    val tagline: String,
    val description: String,
    val presetName: String,
    val keyValues: Map<Int, Int>,       // ball number → point value, shown in the ball strip
    val isDefault: Boolean = false
)

/**
 * The three supported systems, in display order. The default (6-17) is first and is
 * preselected on launch.
 */
val PointsSystemOptions = listOf(
    PointsSystemOption(
        name = "6-17",
        tagline = "Most popular",
        description = "Balls 3–6 are worth 6 points each. Ball 1 = 16, ball 2 = 17.",
        presetName = "6-17",
        keyValues = mapOf(1 to 16, 2 to 17, 3 to 6, 4 to 6, 5 to 6, 6 to 6, 7 to 7, 8 to 8),
        isDefault = true
    ),
    PointsSystemOption(
        name = "3-17",
        tagline = "Classic street rules",
        description = "Balls 3–7 are worth face value. Ball 1 = 16, ball 2 = 17.",
        presetName = "3-17",
        keyValues = mapOf(1 to 16, 2 to 17, 3 to 3, 4 to 4, 5 to 5, 6 to 6, 7 to 7, 8 to 8)
    ),
    PointsSystemOption(
        name = "Face Value",
        tagline = "Keep it simple",
        description = "Every ball is worth exactly its number.",
        presetName = "Face Value",
        keyValues = mapOf(1 to 1, 2 to 2, 3 to 3, 4 to 4, 5 to 5, 6 to 6, 7 to 7, 8 to 8)
    )
)

// --- MAIN SCREEN ---

/** Delay between each staggered element of the entrance sequence. */
private const val StaggerMillis = 70

/**
 * Onboarding screen.
 *
 * - Hero (theme `primary`): logo, welcome copy and a ball strip that previews the selected
 *   system. Only the point values that differ between systems animate.
 * - Middle: three equal-size radio rows. The middle section scrolls on very small screens,
 *   so the CTA is never pushed off screen.
 * - CTA pinned to the bottom.
 *
 * Every color comes from [MaterialTheme.colorScheme], so the screen follows the app theme,
 * dynamic color and dark mode automatically.
 */
@Composable
fun OnboardingScreen(
    onComplete: (chosenPresetName: String) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val haptic = LocalHapticFeedback.current

    // The hero is a *soft* wash of the brand colour rather than a solid slab of
    // full-saturation `primary`. Large areas of raw `primary` read as harsh —
    // especially under Material You, where the wallpaper can produce a very
    // loud hue. Blending almost all the way to `surface` keeps the colour
    // identity while dropping the intensity to something comfortable.
    val heroColor = lerp(colorScheme.primary, colorScheme.surface, 0.88f)

    var selectedIndex by rememberSaveable {
        mutableStateOf(PointsSystemOptions.indexOfFirst { it.isDefault }.coerceAtLeast(0))
    }
    val selectedOption = PointsSystemOptions[selectedIndex.coerceIn(PointsSystemOptions.indices)]

    // Drives a single staggered entrance: strip → rows → button.
    val entered = remember { MutableTransitionState(false).apply { targetState = true } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.surface)
    ) {
        // ── HERO ──────────
        Surface(
            color = heroColor,
            contentColor = colorScheme.onSurface,
            shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 22.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = colorScheme.primary.copy(alpha = 0.12f),
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_pocket_logo),
                            contentDescription = "PocketScore",
                            tint = colorScheme.primary,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }

                Spacer(Modifier.height(14.dp))

                Text(
                    text = "Welcome to PocketScore",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface
                )

                Spacer(Modifier.height(6.dp))

                Text(
                    text = "Pick the points system your crew plays with. You can change it any time in Settings.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(18.dp))

                AnimatedVisibility(
                    visibleState = entered,
                    enter = fadeIn(tween(380, delayMillis = 60)) +
                        slideInVertically(tween(380, delayMillis = 60, easing = FastOutSlowInEasing)) { it / 6 }
                ) {
                    FeltBallStrip(option = selectedOption)
                }
            }
        }

        // ── OPTIONS (flexible middle) ─────────────────────────────────────────
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 18.dp)
                .selectableGroup(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Scoring system",
                style = MaterialTheme.typography.labelLarge,
                color = colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 14.dp, bottom = 2.dp)
            )

            PointsSystemOptions.forEachIndexed { index, option ->
                val delay = 140 + index * StaggerMillis
                AnimatedVisibility(
                    visibleState = entered,
                    enter = fadeIn(tween(340, delayMillis = delay)) +
                        slideInVertically(tween(340, delayMillis = delay, easing = FastOutSlowInEasing)) { it / 4 }
                ) {
                    PointsSystemRow(
                        option = option,
                        selected = index == selectedIndex,
                        onSelect = {
                            // Haptic + state change only when the selection really changes.
                            if (index != selectedIndex) {
                                haptic.performHapticFeedback(OnboardingHaptics.Select)
                                selectedIndex = index
                            }
                        }
                    )
                }
            }
        }

        // ── CTA (pinned) ──
        val ctaDelay = 140 + PointsSystemOptions.size * StaggerMillis
        AnimatedVisibility(
            visibleState = entered,
            enter = fadeIn(tween(340, delayMillis = ctaDelay)) +
                slideInVertically(tween(340, delayMillis = ctaDelay, easing = FastOutSlowInEasing)) { it / 2 }
        ) {
            GetStartedButton(
                onClick = { onComplete(selectedOption.presetName) },
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .padding(top = 12.dp, bottom = 20.dp)
                    .navigationBarsPadding()
            )
        }
    }
}