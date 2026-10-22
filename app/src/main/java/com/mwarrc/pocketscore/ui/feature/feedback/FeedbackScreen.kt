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
import com.mwarrc.pocketscore.ui.feature.feedback.components.*
import com.mwarrc.pocketscore.util.FeedbackManager
import com.mwarrc.pocketscore.util.FeedbackType
import kotlinx.coroutines.launch

/**
 * Screen that allows users to submit feedback, report bugs, or request features.
 * 
 * @param onNavigateBack Callback to navigate back to the previous screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedbackScreen(
    onNavigateBack: () -> Unit
) {
    var selectedType by remember { mutableStateOf(FeedbackType.CONTACT) }
    var message by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    val maxChars = 10000
    
    /**
     * Basic email validation.
     */
    fun isValidEmail(email: String): Boolean {
        if (email.isBlank()) return true // Optional field
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .imePadding(),
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
            // Encouragement header
            FeedbackHeader()

            // Category selector (Chat, Idea, Bug)
            FeedbackTypeSelector(
                selectedType = selectedType,
                onTypeSelected = { 
                    selectedType = it
                    errorMessage = null
                }
            )

            // Message description field
            FeedbackMessageInput(
                value = message,
                onValueChange = { 
                    message = it
                    errorMessage = null
                },
                maxChars = maxChars
            )

            // Optional email field
            FeedbackEmailInput(
                value = email,
                onValueChange = { 
                    email = it
                    errorMessage = null
                }
            )

            // Error display
            if (errorMessage != null) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            Spacer(Modifier.weight(1f))

            // Submission Button
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
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
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
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = "Submit Report",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}
