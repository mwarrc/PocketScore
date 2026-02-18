package com.mwarrc.pocketscore.ui.feature.about.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mwarrc.pocketscore.R

/**
 * Top header component for the About screen.
 * 
 * Displays the app logo with an interactive "Dev Mode" unlock mechanism
 * and current version information.
 * 
 * @param version Current app version string
 * @param showDevMode Whether developer mode is currently active
 * @param onLogoClick Triggered when the logo is clicked (used for hidden feature)
 * @param onToggleDevMode Toggles developer mode visibility
 */
@Composable
fun AboutHeader(
    version: String
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(16.dp))

        // App Logo
        Surface(
            modifier = Modifier.size(80.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            shadowElevation = 4.dp
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon( // Use Icon directly with painter resource
                    painter = painterResource(id = R.drawable.ic_pocket_logo),
                    contentDescription = null,
                    modifier = Modifier.size(44.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // Version Label
        Text(
            text = version,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.alpha(0.6f)
        )
    }
}
