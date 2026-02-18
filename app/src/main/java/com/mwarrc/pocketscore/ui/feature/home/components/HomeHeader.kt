package com.mwarrc.pocketscore.ui.feature.home.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mwarrc.pocketscore.R

/**
 * The primary branding header for the Home screen.
 * 
 * Displays the PocketScore logo, app name, and a tagline. The logo acts as a 
 * button to navigate to the "About" or "Introduction" section.
 * 
 * @param onNavigateToAbout Callback triggered when the logo is tapped.
 * @param modifier Modifier for the header row container.
 */
@Composable
fun HomeHeader(
    onNavigateToAbout: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Interactive Branding Logo
        Surface(
            onClick = onNavigateToAbout,
            modifier = Modifier.size(52.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            tonalElevation = 2.dp,
            border = androidx.compose.foundation.BorderStroke(
                width = 1.2.dp, 
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
            )
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_pocket_logo),
                    contentDescription = "About PocketScore",
                    modifier = Modifier.size(30.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
        
        // App Identification
        Column {
            Text(
                text = "PocketScore",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.5.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "The Expressive Scoreboard",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                fontWeight = FontWeight.Medium
            )
        }
    }
}
