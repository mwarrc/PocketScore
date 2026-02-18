package com.mwarrc.pocketscore.ui.feature.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
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
import com.mwarrc.pocketscore.ui.theme.getMaterialPlayerColor

/**
 * A specialized interactive card for defining player identities during match setup.
 * 
 * Features:
 * - **Smart Validation Feedback**: Visual indicators for duplicates, empty slots, and new vs returning players.
 * - **Identity Intelligence**: Detects if a name matches a returning player with existing history.
 * - **AutoFocus Support**: Automatically requests keyboard focus when added to the list.
 * - **Destructive Actions**: Safely allows roster trimming with a dedicated remove button.
 * - **Expressive UI**: Uses high-contrast badges and clear supporting text to guide the user.
 * 
 * @param index The position of the player in the current roster (0-indexed).
 * @param name The current text value of the player's name.
 * @param hasError Whether the current input is invalid (empty or duplicate).
 * @param isDuplicate Specific flag for when the name already exists in the current session.
 * @param isContinuing Specific flag for when the name matches a globally saved player.
 * @param isNewPlayer Specific flag for a name that exists neither in the session nor the saved roster.
 * @param allowRemove Whether this card can be deleted from the roster.
 * @param shouldFocus Whether the text field should immediately capture input focus.
 * @param onNameChange Callback triggered as the user types.
 * @param onRemove Callback triggered when the remove action is tapped.
 * @param modifier Modifier for the card container.
 */
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

    // Auto-focus logic for new entries
    LaunchedEffect(shouldFocus) {
        if (shouldFocus) {
            focusRequester.requestFocus()
        }
    }

    val accentColor = getMaterialPlayerColor(name)

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = when {
                isDuplicate -> MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                hasError -> MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
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
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Identity Badge (Index)
            InputIdentityBadge(index = index, hasError = hasError, isDuplicate = isDuplicate)

            // Dynamic Input Section
            Column(modifier = Modifier.weight(1f)) {
                PlayerTextField(
                    name = name,
                    hasError = hasError,
                    focusRequester = focusRequester,
                    onNameChange = onNameChange
                )
                
                // Smart Status Indicators
                if (hasError || isContinuing || isNewPlayer) {
                    InputStatusIndicator(
                        name = name,
                        isDuplicate = isDuplicate,
                        hasError = hasError,
                        isContinuing = isContinuing,
                        isNewPlayer = isNewPlayer,
                        accentColor = accentColor
                    )
                }
            }

            // Destructive Action
            if (allowRemove) {
                RemovePlayerIconButton(onRemove)
            } else {
                Spacer(Modifier.size(36.dp))
            }
        }
    }
}

@Composable
private fun InputIdentityBadge(index: Int, hasError: Boolean, isDuplicate: Boolean) {
    Surface(
        shape = CircleShape,
        color = if (hasError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primaryContainer,
        modifier = Modifier.size(40.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = "${index + 1}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Black,
                color = when {
                    isDuplicate -> MaterialTheme.colorScheme.error
                    hasError -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.onPrimaryContainer
                }
            )
        }
    }
}

@Composable
private fun PlayerTextField(
    name: String,
    hasError: Boolean,
    focusRequester: FocusRequester,
    onNameChange: (String) -> Unit
) {
    TextField(
        value = name,
        onValueChange = { newValue ->
            val filtered = newValue.filter { it.isLetterOrDigit() }
            onNameChange(filtered)
        },
        modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester),
        placeholder = { 
            Text(
                text = "Player Name", 
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
}

@Composable
private fun InputStatusIndicator(
    name: String,
    isDuplicate: Boolean,
    hasError: Boolean,
    isContinuing: Boolean,
    isNewPlayer: Boolean,
    accentColor: Color
) {
    Row(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        val (icon, text, color) = when {
            isDuplicate -> Triple(Icons.Default.Close, "Duplicate: ${name.trim()}", MaterialTheme.colorScheme.error)
            hasError && name.isEmpty() -> Triple(Icons.Default.Close, "Empty Slot", MaterialTheme.colorScheme.primary.copy(alpha = 0.6f))
            isContinuing -> Triple(Icons.Default.History, "Continuing Record", MaterialTheme.colorScheme.primary)
            isNewPlayer -> Triple(Icons.Default.Star, "New Player", accentColor)
            else -> Triple(Icons.Default.History, "", MaterialTheme.colorScheme.onSurfaceVariant)
        }
        
        if (text.isNotEmpty()) {
            Icon(icon, null, modifier = Modifier.size(12.dp), tint = color)
            Text(
                text = text, 
                style = MaterialTheme.typography.labelSmall, 
                color = color, 
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

@Composable
private fun RemovePlayerIconButton(onRemove: () -> Unit) {
    IconButton(
        onClick = onRemove,
        modifier = Modifier
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.error.copy(alpha = 0.1f))
            .size(36.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Remove Player",
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(18.dp)
        )
    }
}
