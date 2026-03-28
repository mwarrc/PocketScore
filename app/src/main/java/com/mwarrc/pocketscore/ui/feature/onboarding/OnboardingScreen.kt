package com.mwarrc.pocketscore.ui.feature.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mwarrc.pocketscore.R
import com.mwarrc.pocketscore.domain.model.AppSettings
import com.mwarrc.pocketscore.domain.model.BallValuePreset
import com.mwarrc.pocketscore.ui.feature.onboarding.components.PointsSystemCard
import com.mwarrc.pocketscore.ui.feature.onboarding.components.PulsingGetStartedButton
import kotlinx.coroutines.delay

// --- DATA MODEL ---

/**
 * Represents a selectable ball points system option during onboarding.
 */
data class PointsSystemOption(
    val name: String,
    val tagline: String,
    val description: String,
    val presetName: String,
    val keyValues: Map<Int, Int>,       // ball number → point value, shown on card
    val isDefault: Boolean = false
)

/**
 * The three supported systems — 6-17 first as default, then 3-17, then Face Value.
 */
val PointsSystemOptions = listOf(
    PointsSystemOption(
        name = "6-17",
        tagline = "Most popular",
        description = "Balls 3–6 worth 6 pts each · Ball 1 = 16 · Ball 2 = 17",
        presetName = "6-17",
        keyValues = mapOf(1 to 16, 2 to 17, 3 to 6, 4 to 6, 5 to 6, 6 to 6, 7 to 7, 8 to 8),
        isDefault = true
    ),
    PointsSystemOption(
        name = "3-17",
        tagline = "Classic street rules",
        description = "Balls 3–7 face value · Ball 1 = 16 · Ball 2 = 17",
        presetName = "3-17",
        keyValues = mapOf(1 to 16, 2 to 17, 3 to 3, 4 to 4, 5 to 5, 6 to 6, 7 to 7, 8 to 8)
    ),
    PointsSystemOption(
        name = "Face Value",
        tagline = "Keep it simple",
        description = "Every ball is worth exactly its number — straight & pure",
        presetName = "Face Value",
        keyValues = mapOf(1 to 1, 2 to 2, 3 to 3, 4 to 4, 5 to 5, 6 to 6, 7 to 7, 8 to 8)
    )
)

// --- MAIN SCREEN ---

@Composable
fun OnboardingScreen(
    onComplete: (chosenPresetName: String) -> Unit
) {
    var selectedOption by remember {
        mutableStateOf(PointsSystemOptions.first { it.isDefault })
    }

    var headerVisible by remember { mutableStateOf(false) }
    var cardsVisible by remember { mutableStateOf(false) }
    var buttonVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(80)
        headerVisible = true
        delay(300)
        cardsVisible = true
        delay(200)
        buttonVisible = true
    }

    val colorScheme = MaterialTheme.colorScheme

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.surface)
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(48.dp))

        // ── HEADER ─────────────────────────────────────────────────────────────
        AnimatedVisibility(
            visible = headerVisible,
            enter = fadeIn(tween(480)) + slideInVertically(tween(480, easing = FastOutSlowInEasing)) { -40 }
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = colorScheme.primaryContainer,
                    modifier = Modifier.size(72.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_pocket_logo),
                            contentDescription = "PocketScore",
                            tint = colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))

                Text(
                    text = "Welcome to PocketScore",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "Pick the points system your crew plays with.\nYou can change this any time in Settings.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp
                )
            }
        }

        Spacer(Modifier.height(32.dp))

        // ── SECTION LABEL ──────────────────────────────────────────────────────
        AnimatedVisibility(
            visible = cardsVisible,
            enter = fadeIn(tween(400)) + slideInVertically(tween(400)) { 24 }
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "CHOOSE YOUR SCORING SYSTEM",
                    style = MaterialTheme.typography.labelMedium,
                    color = colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
                )

                // ── OPTION CARDS ────────────────────────────────────────────
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    PointsSystemOptions.forEach { option ->
                        PointsSystemCard(
                            option = option,
                            isSelected = selectedOption.presetName == option.presetName,
                            onClick = { selectedOption = option }
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "You can always change this later in Settings → Ball Values.",
                    style = MaterialTheme.typography.bodySmall,
                    color = colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(Modifier.weight(1f))
        Spacer(Modifier.height(24.dp))

        // ── CTA ────────────────────────────────────────────────────────────────
        AnimatedVisibility(
            visible = buttonVisible,
            enter = fadeIn(tween(400)) + slideInVertically(tween(400)) { 48 }
        ) {
            PulsingGetStartedButton(
                enabled = true,
                onClick = { onComplete(selectedOption.presetName) }
            )
        }

        Spacer(Modifier.height(32.dp))
    }
}