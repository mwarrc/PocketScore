package com.mwarrc.pocketscore.util

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.PersistentCacheSettings
import com.google.firebase.firestore.Source
import java.text.SimpleDateFormat
import java.util.*

/**
 * Custom Cloud Analytics Manager that records events directly to Firestore
 * to provide a structured, non-chaotic alternative to Firebase Analytics.
 */
object CloudAnalyticsManager {
    private val firestore by lazy { FirebaseProvider.getFirestore() }
    private const val APP_VERSION = "0.1.2 Expressive"

    /**
     * Records a new installation or confirms an existing one.
     */
    fun logInstallation(analyticsId: String, isOffline: Boolean = false) {
        val data = hashMapOf(
            "analyticsId" to analyticsId,
            "installTimestamp" to FieldValue.serverTimestamp(),
            "deviceModel" to android.os.Build.MODEL,
            "androidVersion" to android.os.Build.VERSION.RELEASE,
            "appVersion" to APP_VERSION,
            "platform" to "Android",
            "initially_offline" to isOffline
        )
        // Using set(data, SetOptions.merge()) if we wanted to preserve first install, 
        // but for a simple "installs" collection, we can just use the ID as doc name.
        firestore.collection("analytics_installs").document(analyticsId).set(data)
    }

    /**
     * Records a daily app open event.
     */
    fun logDailyOpen(analyticsId: String, isOffline: Boolean = false) {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        val docId = "${today}_$analyticsId"
        
        val data = hashMapOf(
            "analyticsId" to analyticsId,
            "date" to today,
            "timestamp" to FieldValue.serverTimestamp(),
            "appVersion" to APP_VERSION,
            "initially_offline" to isOffline
        )
        
        firestore.collection("analytics_daily_activity")
            .document(docId)
            .set(data)
    }

    /**
     * Records match statistics. Data is anonymized (no player names).
     */
    fun logMatchPlayed(
        analyticsId: String,
        playerCount: Int,
        totalTurns: Int,
        isFinalized: Boolean,
        durationMillis: Long,
        maxScore: Int,
        isOffline: Boolean = false
    ) {
        val matchData = hashMapOf(
            "analyticsId" to analyticsId,
            "timestamp" to FieldValue.serverTimestamp(),
            "playerCount" to playerCount,
            "totalTurns" to totalTurns,
            "isFinalized" to isFinalized,
            "durationMinutes" to (durationMillis / 60000.0),
            "maxScore" to maxScore,
            "appVersion" to APP_VERSION,
            "initially_offline" to isOffline
        )
        
        firestore.collection("analytics_matches").add(matchData)
    }

    /**
     * Records generic feature usage or interactions.
     */
    fun logFeatureUsage(analyticsId: String, featureName: String, metadata: Map<String, Any> = emptyMap(), isOffline: Boolean = false) {
        val eventData = hashMapOf(
            "analyticsId" to analyticsId,
            "feature" to featureName,
            "timestamp" to FieldValue.serverTimestamp(),
            "metadata" to metadata,
            "appVersion" to APP_VERSION,
            "initially_offline" to isOffline
        )
        firestore.collection("analytics_events").add(eventData)
    }

    /**
     * Smart synchronization check - can be called to ensure pending writes are handled
     */
    fun ensureSync(onComplete: (Boolean) -> Unit) {
        firestore.waitForPendingWrites()
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }
}
