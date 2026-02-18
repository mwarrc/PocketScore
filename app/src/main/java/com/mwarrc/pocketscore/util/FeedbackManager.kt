package com.mwarrc.pocketscore.util

import android.util.Log
import com.google.firebase.firestore.FieldValue
import com.mwarrc.pocketscore.BuildConfig
import java.util.*

/**
 * Types of user feedback.
 */
enum class FeedbackType {
    BUG_REPORT,
    FEATURE_REQUEST,
    CONTACT,
    OTHER
}

/**
 * User feedback data structure.
 * 
 * @property id Unique feedback identifier
 * @property type Category of feedback
 * @property message User's feedback message
 * @property contactEmail Optional contact email
 * @property deviceModel Device model information
 * @property androidVersion Android OS version
 * @property appVersion Current app version
 * @property timestamp Submission timestamp
 */
data class UserFeedback(
    val id: String = UUID.randomUUID().toString(),
    val type: FeedbackType,
    val message: String,
    val contactEmail: String? = null,
    val deviceModel: String = android.os.Build.MODEL,
    val androidVersion: String = android.os.Build.VERSION.RELEASE,
    val appVersion: String = BuildConfig.VERSION_NAME,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Manager for submitting user feedback to Firestore.
 */
object FeedbackManager {
    
    private const val TAG = "FeedbackManager"
    private const val COLLECTION_NAME = "feedback"
    
    private val firestore by lazy { FirebaseProvider.getFirestore() }

    /**
     * Submits user feedback to Firestore.
     * 
     * @param type Type of feedback
     * @param message Feedback message
     * @param email Optional contact email
     * @param onSuccess Callback on successful submission
     * @param onFailure Callback on failure with exception
     */
    fun submitFeedback(
        type: FeedbackType,
        message: String,
        email: String?,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        if (message.isBlank()) {
            onFailure(IllegalArgumentException("Feedback message cannot be empty"))
            return
        }
        
        try {
            val feedback = UserFeedback(
                type = type,
                message = message.trim(),
                contactEmail = email?.trim()?.takeIf { it.isNotBlank() && isValidEmail(it) }
            )

            val feedbackMap = mapOf(
                "id" to feedback.id,
                "type" to feedback.type.name,
                "message" to feedback.message,
                "contactEmail" to (feedback.contactEmail ?: ""),
                "deviceModel" to feedback.deviceModel,
                "androidVersion" to feedback.androidVersion,
                "appVersion" to feedback.appVersion,
                "timestamp" to FieldValue.serverTimestamp(),
                "clientTimestamp" to feedback.timestamp,
                "status" to "pending"
            )

            firestore.collection(COLLECTION_NAME)
                .document(feedback.id)
                .set(feedbackMap)
                .addOnSuccessListener {
                    Log.d(TAG, "Feedback submitted successfully: ${feedback.id}")
                    onSuccess()
                }
                .addOnFailureListener { exception ->
                    Log.e(TAG, "Failed to submit feedback", exception)
                    onFailure(exception)
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error creating feedback object", e)
            onFailure(e)
        }
    }
    
    /**
     * Basic email validation.
     * 
     * @param email Email string to validate
     * @return true if email format is valid
     */
    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}

