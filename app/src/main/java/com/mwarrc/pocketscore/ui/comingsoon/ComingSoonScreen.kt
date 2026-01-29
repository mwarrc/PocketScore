package com.mwarrc.pocketscore.ui.comingsoon

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mwarrc.pocketscore.ui.components.ElegantConfetti

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComingSoonScreen(
    onNavigateBack: () -> Unit
) {
    var showConfetti by remember { mutableStateOf(false) }
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Coming Soon",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                windowInsets = androidx.compose.foundation.layout.WindowInsets(top = 32.dp)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier.size(80.dp),
                shape = androidx.compose.foundation.shape.CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.DateRange,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            Text(
                "The Future of PocketScore",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(8.dp))

            Text(
                "We are working hard to bring you these amazing features in the next update.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(40.dp))

            FeaturePreviewItem(
                icon = Icons.Default.Groups,
                title = "Team Support",
                description = "Group players into teams and track collective scores. Perfect for board games and team sports."
            )

            Spacer(Modifier.height(16.dp))

            FeaturePreviewItem(
                icon = Icons.Default.Sync,
                title = "Real-time Sync",
                description = "Join games with a QR code and sync scores across multiple devices instantly."
            )

            Spacer(Modifier.height(16.dp))

            FeaturePreviewItem(
                icon = Icons.AutoMirrored.Filled.List,
                title = "Advanced Statistics",
                description = "Visual graphs and deep insights into your gaming sessions and player performance."
            )

            Spacer(Modifier.height(16.dp))

            FeaturePreviewItem(
                icon = Icons.Default.Timer,
                title = "Game Timers",
                description = "Integrated turn timers and game clocks to keep the pace of your sessions."
            )

            Spacer(Modifier.height(16.dp))

            FeaturePreviewItem(
                icon = Icons.Default.Share,
                title = "Social Sharing",
                description = "Share your game results directly to Instagram, WhatsApp, and more with cool generated images."
            )

            Spacer(Modifier.height(16.dp))

            FeaturePreviewItem(
                icon = Icons.Default.EmojiEvents,
                title = "Tournament Mode",
                description = "Organize brackets and track points across multiple games in a series."
            )

            Spacer(Modifier.height(16.dp))

            FeaturePreviewItem(
                icon = Icons.Default.Palette,
                title = "Custom Themes",
                description = "More color options and scoreboard styles to match your favorite games."
            )

            Spacer(Modifier.height(48.dp))

            FilledTonalButton(
                onClick = { showConfetti = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Can't Wait!")
            }
        }
    }

    if (showConfetti) {
        ElegantConfetti(
            trigger = showConfetti,
            onAnimationFinished = onNavigateBack
        )
    }
}

@Composable
fun FeaturePreviewItem(
    icon: ImageVector,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Surface(
            modifier = Modifier.size(48.dp),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    icon,
                    null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.secondary
                )
            }
        }

        Spacer(Modifier.width(16.dp))

        Column {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 20.sp
            )
        }
    }
}

