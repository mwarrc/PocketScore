package com.mwarrc.pocketscore.ui.feature.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.material3.surfaceColorAtElevation
import com.mwarrc.pocketscore.ui.theme.getMaterialPlayerColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PlayerInputCard(
    index: Int,
    name: String,
    hasError: Boolean,
    isDuplicate: Boolean = false,
    isContinuing: Boolean = false,
    isNewPlayer: Boolean = false,
    allowRemove: Boolean = false,
    shouldFocus: Boolean = false,
    onNameChange: (String) -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(shouldFocus) {
        if (shouldFocus) {
            focusRequester.requestFocus()
        }
    }

    val accentColor = getMaterialPlayerColor(name)
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = when {
            hasError -> MaterialTheme.colorScheme.errorContainer
            else -> MaterialTheme.colorScheme.surface
        },
        tonalElevation = 0.dp,
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = when {
                hasError -> MaterialTheme.colorScheme.error
                name.isNotEmpty() -> MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                else -> MaterialTheme.colorScheme.outlineVariant
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp) // More spacing for avatar
        ) {
            // Player Index Avatar - High Contrast
            Surface(
                shape = CircleShape,
                color = when {
                    hasError -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.primaryContainer
                },
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        "${index + 1}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Black,
                        color = when {
                            hasError -> MaterialTheme.colorScheme.onError
                            else -> MaterialTheme.colorScheme.onPrimaryContainer
                        }
                    )
                }
            }

            // Input Field
            Column(modifier = Modifier.weight(1f)) {
                TextField(
                    value = name,
                    onValueChange = onNameChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    placeholder = { 
                        Text(
                            "Player Name", 
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        ) 
                    },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        errorContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        errorIndicatorColor = Color.Transparent,
                        cursorColor = MaterialTheme.colorScheme.primary
                    ),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.2.sp
                    ),
                    isError = hasError
                )
                
                // Detailed supporting text
                if (hasError || isContinuing || isNewPlayer) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val (icon, text, color) = when {
                            isDuplicate -> Triple(Icons.Default.Close, "Already in match", MaterialTheme.colorScheme.error)
                            hasError && name.isEmpty() -> Triple(Icons.Default.Close, "Name required", MaterialTheme.colorScheme.error)
                            isContinuing -> Triple(Icons.Default.History, "Continuing Player's Record", MaterialTheme.colorScheme.primary)
                            isNewPlayer -> Triple(Icons.Default.Star, "New Player", accentColor)
                            else -> Triple(Icons.Default.History, "", MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        
                        if (text.isNotEmpty()) {
                            Icon(icon, null, modifier = Modifier.size(12.dp), tint = color)
                            Text(text, style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }
            }

            if (allowRemove) {
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.error.copy(alpha = 0.1f))
                        .size(36.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        "Remove",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                }
            } else {
                Spacer(Modifier.size(36.dp))
            }
        }
    }
}
