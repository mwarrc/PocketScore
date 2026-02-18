package com.mwarrc.pocketscore.ui.feature.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.SportsScore
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mwarrc.pocketscore.R
import com.mwarrc.pocketscore.ui.feature.onboarding.components.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// --- SCREEN SETTINGS DATA ---

// --- ONBOARDING CONFIGURATION ---

private object OnboardingConstants {
    const val AUTO_SCROLL_DELAY = 6000L
    const val BUTTON_ENTER_DURATION = 500
    const val BUTTON_EXIT_DURATION = 300
    const val SKIP_FADE_IN_DURATION = 400
    const val SKIP_FADE_OUT_DURATION = 300
}

/**
 * Data class representing a single page in the onboarding flow.
 *
 * @property title The main headline.
 * @property subtitle The small label above the title.
 * @property description Detailed explanation text.
 * @property icon Optional [ImageVector] to display.
 * @property customIconRes Optional resource ID for a custom drawable icon.
 * @property accentColor Optional color used for subtitle and icon tinting.
 */
data class OnboardingPage(
    val title: String,
    val subtitle: String,
    val description: String,
    val icon: ImageVector? = null,
    val customIconRes: Int? = null,
    val accentColor: Color? = null
)

/**
 * The static list of pages shown during the onboarding experience.
 */
val OnboardingPages = listOf(
    OnboardingPage(
        title = "Track with Purpose",
        subtitle = "THE MISSION",
        description = "Built for those who value precision. Every point matters, every game counts.",
        icon = Icons.Default.SportsScore
    ),
    OnboardingPage(
        title = "Designed to Disappear",
        subtitle = "THE PHILOSOPHY",
        description = "Minimal interface. Maximum focus. Your scoreboard shouldn't distract from the game.",
        icon = Icons.Default.Lightbulb
    ),
    OnboardingPage(
        title = "Ready When You Are",
        subtitle = "LET'S BEGIN",
        description = "No accounts. No setup. Just you, your friends, and the game ahead.",
        customIconRes = R.drawable.ic_pocket_logo
    )
)

/**
 * Data class for an animated background particle.
 */
data class Particle(
    val id: Int,
    val startX: Float,
    val startY: Float,
    val speed: Float,
    val size: Float,
    val alpha: Float
)

// --- MAIN SCREEN ---

/**
 * The main entry point for the Onboarding experience.
 *
 * This screen displays a series of educational pages to the user when they first launch the app.
 * It features auto-scrolling, interactive particles, and premium animations.
 *
 * @param onComplete Callback triggered when the user finishes onboarding or skips.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onComplete: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { OnboardingPages.size })
    val coroutineScope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    
    var isAutoScrolling by remember { mutableStateOf(true) }

    // Auto-scroll logic with haptic feedback
    LaunchedEffect(isAutoScrolling) {
        if (isAutoScrolling) {
            while (pagerState.currentPage < OnboardingPages.size - 1) {
                delay(OnboardingConstants.AUTO_SCROLL_DELAY)
                if (isAutoScrolling) {
                    pagerState.animateScrollToPage(
                        pagerState.currentPage + 1,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    )
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                }
            }
            isAutoScrolling = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        // Animated dynamic background
        AnimatedBackgroundParticles(pagerState)

        // Main Content Pager
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { 
                            isAutoScrolling = false
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        },
                        onDrag = { change, _ ->
                            change.consume()
                        }
                    )
                }
        ) { pageIndex ->
            OnboardingPageContent(
                page = OnboardingPages[pageIndex],
                pageIndex = pageIndex,
                pagerState = pagerState
            )
        }

        // --- BOTTOM NAVIGATION & CONTROLS ---
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 60.dp)
                .padding(horizontal = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Page indicators
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(bottom = 40.dp)
            ) {
                repeat(OnboardingPages.size) { index ->
                    EnhancedPageIndicator(
                        isActive = index == pagerState.currentPage,
                        isPast = index < pagerState.currentPage,
                        onClick = {
                            isAutoScrolling = false
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(
                                    index,
                                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                                )
                            }
                        }
                    )
                }
            }

            // Primary action button (appears on last page)
            AnimatedVisibility(
                visible = pagerState.currentPage == OnboardingPages.size - 1,
                enter = fadeIn(tween(OnboardingConstants.BUTTON_ENTER_DURATION)) + slideInVertically { it / 3 } + scaleIn(tween(OnboardingConstants.BUTTON_ENTER_DURATION)),
                exit = fadeOut(tween(OnboardingConstants.BUTTON_EXIT_DURATION)) + slideOutVertically { it / 3 } + scaleOut(tween(OnboardingConstants.BUTTON_EXIT_DURATION))
            ) {
                PulsingGetStartedButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onComplete()
                    }
                )
            }
        }

        // --- SECONDARY ACTIONS ---
        AnimatedVisibility(
            visible = pagerState.currentPage < OnboardingPages.size - 1,
            enter = fadeIn(tween(OnboardingConstants.SKIP_FADE_IN_DURATION)),
            exit = fadeOut(tween(OnboardingConstants.SKIP_FADE_OUT_DURATION)),
            modifier = Modifier.align(Alignment.TopEnd)
        ) {
            TextButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onComplete()
                },
                modifier = Modifier.padding(top = 48.dp, end = 16.dp)
            ) {
                Text(
                    "SKIP",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )
            }
        }
    }
}