package com.mwarrc.pocketscore.ui.feature.feedback

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mwarrc.pocketscore.util.FeedbackManager
import com.mwarrc.pocketscore.util.FeedbackType
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedbackScreen(
    onNavigateBack: () -> Unit
) {
    var selectedType by remember { mutableStateOf(FeedbackType.BUG_REPORT) }
    var message by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    val maxChars = 10000
    
    fun isValidEmail(email: String): Boolean {
        if (email.isBlank()) return true // Optional
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize().imePadding(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Feedback & Support",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                windowInsets = WindowInsets(top = 32.dp)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Header Section
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Help us improve",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    "Your feedback helps us build a better experience for everyone. Whether it's a bug or a brilliant idea, we want to hear it.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 20.sp
                )
            }

            // Type Selection
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Category",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
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
                                    Icon(icon, null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text(label, style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        )
                    }
                }
            }

            // Message Input
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Description",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = message,
                        onValueChange = { 
                            if (it.length <= maxChars) {
                                message = it
                                if (it.isNotEmpty()) errorMessage = null
                            }
                        },
                        placeholder = { Text("What's on your mind? Be as detailed as you can. We love stories!") },
                        modifier = Modifier.fillMaxWidth().height(240.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                        )
                    )
                    Text(
                        text = "${message.length} / $maxChars",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (message.length > maxChars * 0.9) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(bottom = 12.dp, end = 16.dp)
                    )
                }
            }

            // Email Input
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Contact Email (Optional)",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    placeholder = { Text("email@example.com") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    )
                )
                Text(
                    "We'll only use this to follow up on your report if needed.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.padding(start = 4.dp)
                )
            }

            if (errorMessage != null) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        errorMessage!!,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            Spacer(Modifier.weight(1f))

            Button(
                onClick = {
                    if (message.isBlank()) {
                        errorMessage = "Please enter a message before submitting."
                        return@Button
                    }
                    if (!isValidEmail(email)) {
                        errorMessage = "Please enter a valid email address."
                        return@Button
                    }
                    isSubmitting = true
                    FeedbackManager.submitFeedback(
                        type = selectedType,
                        message = message,
                        email = email,
                        onSuccess = {
                            isSubmitting = false
                            errorMessage = null
                            message = ""
                            email = ""
                            scope.launch {
                                snackbarHostState.showSnackbar("Thank you! Feedback submitted.")
                            }
                        },
                        onFailure = {
                            isSubmitting = false
                            errorMessage = "Failed to submit. Please check your connection."
                        }
                    )
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = !isSubmitting,
                shape = RoundedCornerShape(16.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 3.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(Icons.AutoMirrored.Filled.Send, null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "Submit Report",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}
