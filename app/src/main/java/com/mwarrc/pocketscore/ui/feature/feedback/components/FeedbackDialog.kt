package com.mwarrc.pocketscore.ui.feature.feedback.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.mwarrc.pocketscore.util.FeedbackManager
import com.mwarrc.pocketscore.util.FeedbackType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedbackDialog(
    onDismissRequest: () -> Unit,
    onSuccess: () -> Unit
) {
    var selectedType by remember { mutableStateOf(FeedbackType.BUG_REPORT) }
    var message by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            tonalElevation = 2.dp,
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Share Your Thoughts",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    "Help us make PocketScore better. Report a bug or suggest a new feature!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Type Selection
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    FeedbackType.entries.filter { it != FeedbackType.OTHER }.forEachIndexed { index, type ->
                        val label = when (type) {
                            FeedbackType.BUG_REPORT -> "Bug"
                            FeedbackType.FEATURE_REQUEST -> "Idea"
                            FeedbackType.CONTACT -> "Chat"
                            else -> type.name
                        }
                        val icon = when (type) {
                            FeedbackType.BUG_REPORT -> Icons.Default.BugReport
                            FeedbackType.FEATURE_REQUEST -> Icons.Default.Lightbulb
                            FeedbackType.CONTACT -> Icons.AutoMirrored.Filled.Chat
                            else -> Icons.AutoMirrored.Filled.Chat
                        }
                        
                        SegmentedButton(
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = 3),
                            onClick = { selectedType = type },
                            selected = selectedType == type,
                            label = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(icon, null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text(label, style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        )
                    }
                }

                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    label = { Text("Your Message") },
                    placeholder = { Text("What's on your mind?") },
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    shape = RoundedCornerShape(16.dp)
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Contact Email (Optional)") },
                    placeholder = { Text("email@example.com") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true
                )

                if (errorMessage != null) {
                    Text(
                        errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelSmall
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TextButton(
                        onClick = onDismissRequest,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            if (message.isBlank()) {
                                errorMessage = "Message cannot be empty"
                                return@Button
                            }
                            isSubmitting = true
                            FeedbackManager.submitFeedback(
                                type = selectedType,
                                message = message,
                                email = email,
                                onSuccess = {
                                    isSubmitting = false
                                    onSuccess()
                                },
                                onFailure = {
                                    isSubmitting = false
                                    errorMessage = "Submission failed. Check connection."
                                }
                            )
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !isSubmitting,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text("Submit")
                        }
                    }
                }
            }
        }
    }
}
