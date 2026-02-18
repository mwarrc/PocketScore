package com.mwarrc.pocketscore.util

import android.util.Log
import com.google.firebase.firestore.FieldValue
import com.mwarrc.pocketscore.BuildConfig
import java.text.SimpleDateFormat
import java.util.*

/**
 * Cloud analytics manager for structured event tracking via Firestore.
 * 
 * Provides an alternative to Firebase Analytics with more granular control
 * and structured data storage for analytics queries.
 */
object CloudAnalyticsManager {
    
    private const val TAG = "CloudAnalyticsManager"
    private const val APP_VERSION = BuildConfig.VERSION_NAME
    
    // Collection names
    private const val COLLECTION_INSTALLS = "analytics_installs"
    private const val COLLECTION_DAILY_ACTIVITY = "analytics_daily_activity"
    private const val COLLECTION_MATCHES = "analytics_matches"
    private const val COLLECTION_EVENTS = "analytics_events"
    
    private val firestore by lazy { FirebaseProvider.getFirestore() }
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    /**
     * Records a new installation or confirms an existing one.
     * 
     * @param analyticsId Unique analytics identifier
     * @param isOffline Whether the app is currently offline
     */
    fun logInstallation(analyticsId: String, isOffline: Boolean = false) {
        try {
            val data = mapOf(
                "analyticsId" to analyticsId,
                "installTimestamp" to FieldValue.serverTimestamp(),
                "deviceModel" to android.os.Build.MODEL,
                "androidVersion" to android.os.Build.VERSION.RELEASE,
                "appVersion" to APP_VERSION,
                "platform" to "Android",
                "initiallyOffline" to isOffline
            )
            
            firestore.collection(COLLECTION_INSTALLS)
                .document(analyticsId)
                .set(data)
                .addOnSuccessListener {
                    Log.d(TAG, "Installation logged: $analyticsId")
                }
                .addOnFailureListener { e ->
                    // Suppress verbose logging for offline sync failures
                    if (isOffline || e.message?.contains("Offline") == true) {
                        Log.v(TAG, "Installation log queued offline: $analyticsId")
                    } else {
                        Log.w(TAG, "Failed to log installation: ${e.message}")
                    }
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error creating installation log", e)
        }
    }

    /**
     * Records a daily app open event.
     * 
     * Creates one document per user per day to track daily active users.
     * 
     * @param analyticsId Unique analytics identifier
     * @param isOffline Whether the app is currently offline
     */
    fun logDailyOpen(analyticsId: String, isOffline: Boolean = false) {
        try {
            val today = dateFormat.format(Date())
            val docId = "${today}_$analyticsId"
            
            val data = mapOf(
                "analyticsId" to analyticsId,
                "date" to today,
                "timestamp" to FieldValue.serverTimestamp(),
                "appVersion" to APP_VERSION,
                "initiallyOffline" to isOffline
            )
            
            firestore.collection(COLLECTION_DAILY_ACTIVITY)
                .document(docId)
                .set(data)
                .addOnSuccessListener {
                    Log.d(TAG, "Daily open logged: $docId")
                }
                .addOnFailureListener { e ->
                     if (isOffline || e.message?.contains("Offline") == true) {
                        Log.v(TAG, "Daily open log queued offline: $docId")
                     } else {
                        Log.w(TAG, "Failed to log daily open: ${e.message}")
                     }
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error creating daily open log", e)
        }
    }

    /**
     * Records match statistics.
     * 
     * Data is anonymized - no player names are included.
     * 
     * @param analyticsId Unique analytics identifier
     * @param playerCount Number of players
     * @param totalTurns Number of scoring events
     * @param isFinalized Whether game was completed normally
     * @param durationMillis Game duration in milliseconds
     * @param maxScore Highest score achieved
     * @param isOffline Whether recorded offline
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
        try {
            val matchData = mapOf(
                "analyticsId" to analyticsId,
                "timestamp" to FieldValue.serverTimestamp(),
                "playerCount" to playerCount,
                "totalTurns" to totalTurns,
                "isFinalized" to isFinalized,
                "durationMinutes" to (durationMillis / 60000.0),
                "durationMillis" to durationMillis,
                "maxScore" to maxScore,
                "appVersion" to APP_VERSION,
                "initiallyOffline" to isOffline
            )
            
            firestore.collection(COLLECTION_MATCHES)
                .add(matchData)
                .addOnSuccessListener { docRef ->
                    Log.d(TAG, "Match logged: ${docRef.id}")
                }
                .addOnFailureListener { e ->
                     if (isOffline || e.message?.contains("Offline") == true) {
                        Log.v(TAG, "Match log queued offline")
                     } else {
                        Log.w(TAG, "Failed to log match: ${e.message}")
                     }
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error creating match log", e)
        }
    }

    /**
     * Records generic feature usage or interactions.
     * 
     * @param analyticsId Unique analytics identifier
     * @param featureName Name of the feature being used
     * @param metadata Additional contextual data
     * @param isOffline Whether recorded offline
     */
    fun logFeatureUsage(
        analyticsId: String,
        featureName: String,
        metadata: Map<String, Any> = emptyMap(),
        isOffline: Boolean = false
    ) {
        try {
            val eventData = mapOf(
                "analyticsId" to analyticsId,
                "feature" to featureName,
                "timestamp" to FieldValue.serverTimestamp(),
                "metadata" to metadata,
                "appVersion" to APP_VERSION,
                "initiallyOffline" to isOffline
            )
            
            firestore.collection(COLLECTION_EVENTS)
                .add(eventData)
                .addOnSuccessListener { docRef ->
                    Log.d(TAG, "Feature usage logged: $featureName (${docRef.id})")
                }
                .addOnFailureListener { e ->
                     if (isOffline || e.message?.contains("Offline") == true) {
                        Log.v(TAG, "Feature log queued offline: $featureName")
                     } else {
                        Log.w(TAG, "Failed to log feature usage: $featureName - ${e.message}")
                     }
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error creating feature usage log", e)
        }
    }

    /**
     * Ensures all pending writes are synchronized to Firestore.
     * 
     * Useful before app shutdown or when connectivity is restored.
     * 
     * @param onComplete Callback with success status
     */
    fun ensureSync(onComplete: (Boolean) -> Unit) {
        try {
            firestore.waitForPendingWrites()
                .addOnSuccessListener {
                    Log.d(TAG, "All pending writes synchronized")
                    onComplete(true)
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Failed to sync pending writes", e)
                    onComplete(false)
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error ensuring sync", e)
            onComplete(false)
        }
    }
    
    /**
     * Disables network for Firestore (offline mode).
     * 
     * Useful for testing offline functionality.
     */
    fun disableNetwork(onComplete: (Boolean) -> Unit = {}) {
        try {
            firestore.disableNetwork()
                .addOnSuccessListener {
                    Log.d(TAG, "Network disabled")
                    onComplete(true)
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Failed to disable network", e)
                    onComplete(false)
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error disabling network", e)
            onComplete(false)
        }
    }
    
    /**
     * Enables network for Firestore.
     */
    fun enableNetwork(onComplete: (Boolean) -> Unit = {}) {
        try {
            firestore.enableNetwork()
                .addOnSuccessListener {
                    Log.d(TAG, "Network enabled")
                    onComplete(true)
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Failed to enable network", e)
                    onComplete(false)
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error enabling network", e)
            onComplete(false)
        }
    }
}

