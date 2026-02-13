package com.mwarrc.pocketscore.util

import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

enum class FeedbackType {
    BUG_REPORT,
    FEATURE_REQUEST,
    CONTACT,
    OTHER
}

data class UserFeedback(
    val id: String = UUID.randomUUID().toString(),
    val type: FeedbackType,
    val message: String,
    val contactEmail: String? = null,
    val deviceModel: String = android.os.Build.MODEL,
    val androidVersion: String = android.os.Build.VERSION.RELEASE,
    val appVersion: String = "0.1.2 Expressive",
    val timestamp: Long = System.currentTimeMillis()
)

object FeedbackManager {
    private val firestore by lazy { FirebaseProvider.getFirestore() }

    fun submitFeedback(
        type: FeedbackType,
        message: String,
        email: String?,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val feedback = UserFeedback(
            type = type,
            message = message,
            contactEmail = email?.trim()?.ifBlank { null }
        )

        // Map object to a simple map for Firestore
        val feedbackMap = hashMapOf(
            "id" to feedback.id,
            "type" to feedback.type.name,
            "message" to feedback.message,
            "contactEmail" to (feedback.contactEmail ?: ""),
            "deviceModel" to feedback.deviceModel,
            "androidVersion" to feedback.androidVersion,
            "appVersion" to feedback.appVersion,
            "timestamp" to feedback.timestamp
        )

        firestore.collection("feedback")
            .document(feedback.id)
            .set(feedbackMap)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }
}
