package com.mwarrc.pocketscore.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Log
import com.google.firebase.analytics.FirebaseAnalytics

/**
 * Manager for Firebase Analytics and custom analytics tracking.
 * 
 * Handles both Firebase events and custom cloud analytics.
 * Includes offline detection and error handling.
 */
object AnalyticsManager {
    
    private const val TAG = "AnalyticsManager"
    
    private var firebaseAnalytics: FirebaseAnalytics? = null
    private var appContext: Context? = null
    private var isInitialized = false

    /**
     * Initializes the analytics manager with application context.
     * 
     * Should be called once during app startup (typically in MainActivity.onCreate).
     * 
     * @param context Application context
     */
    fun initialize(context: Context) {
        try {
            appContext = context.applicationContext
            if (firebaseAnalytics == null) {
                firebaseAnalytics = FirebaseAnalytics.getInstance(context)
                isInitialized = true
                Log.d(TAG, "Analytics initialized successfully")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize analytics", e)
            isInitialized = false
        }
    }

    /**
     * Checks if network connectivity is available.
     * 
     * @return true if network is available, false otherwise
     */
    private fun isNetworkAvailable(): Boolean {
        val context = appContext ?: return false
        
        return try {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) 
                as? ConnectivityManager ?: return false
            
            val activeNetwork = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) 
                ?: return false
            
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } catch (e: Exception) {
            Log.e(TAG, "Error checking network availability", e)
            false
        }
    }

    /**
     * Logs the app open event.
     * 
     * Tracks session starts and daily opens.
     * 
     * @param analyticsId Unique user analytics ID
     */
    fun logAppOpen(analyticsId: String? = null) {
        if (!isInitialized) {
            Log.w(TAG, "Analytics not initialized, skipping logAppOpen")
            return
        }
        
        try {
            val isOffline = !isNetworkAvailable()
            val bundle = Bundle().apply {
                putString("connection_status", if (isOffline) "offline" else "online")
                putLong("timestamp", System.currentTimeMillis())
                analyticsId?.let { putString("analytics_id", it) }
            }
            
            firebaseAnalytics?.logEvent(FirebaseAnalytics.Event.APP_OPEN, bundle)
            firebaseAnalytics?.logEvent("session_start_manual", bundle)
            
            analyticsId?.let { 
                CloudAnalyticsManager.logDailyOpen(it, isOffline = isOffline)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error logging app open", e)
        }
    }

    /**
     * Logs when a game is started.
     * 
     * @param playerCount Number of players in the game
     * @param isResume Whether this is resuming an existing game
     * @param analyticsId Unique user analytics ID
     */
    fun logGameStarted(
        playerCount: Int,
        isResume: Boolean = false,
        analyticsId: String? = null
    ) {
        if (!isInitialized) {
            Log.w(TAG, "Analytics not initialized, skipping logGameStarted")
            return
        }
        
        try {
            val isOffline = !isNetworkAvailable()
            val bundle = Bundle().apply {
                putString(FirebaseAnalytics.Param.LEVEL_NAME, "Match")
                putInt("player_count", playerCount)
                putBoolean("is_resume", isResume)
                putBoolean("is_offline", isOffline)
                putLong("timestamp", System.currentTimeMillis())
                analyticsId?.let { putString("analytics_id", it) }
            }
            
            firebaseAnalytics?.logEvent(FirebaseAnalytics.Event.LEVEL_START, bundle)
            
            analyticsId?.let {
                CloudAnalyticsManager.logFeatureUsage(
                    it,
                    "game_start",
                    mapOf(
                        "player_count" to playerCount,
                        "is_resume" to isResume
                    ),
                    isOffline = isOffline
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error logging game started", e)
        }
    }

    /**
     * Logs when a game is ended.
     * 
     * @param playerCount Number of players
     * @param totalTurns Number of turns/scores recorded
     * @param isFinalized Whether game was completed normally (vs reset/abandoned)
     * @param winnerScore Winning player's score
     * @param durationMillis Game duration in milliseconds
     * @param analyticsId Unique user analytics ID
     */
    fun logGameEnded(
        playerCount: Int,
        totalTurns: Int,
        isFinalized: Boolean,
        winnerScore: Int = 0,
        durationMillis: Long = 0,
        analyticsId: String? = null
    ) {
        if (!isInitialized) {
            Log.w(TAG, "Analytics not initialized, skipping logGameEnded")
            return
        }
        
        try {
            val isOffline = !isNetworkAvailable()
            val bundle = Bundle().apply {
                putString(FirebaseAnalytics.Param.LEVEL_NAME, "Match")
                putInt("player_count", playerCount)
                putInt("total_turns", totalTurns)
                putInt(FirebaseAnalytics.Param.SUCCESS, if (isFinalized) 1 else 0)
                putInt("winner_score", winnerScore)
                putLong("duration_millis", durationMillis)
                putBoolean("is_offline", isOffline)
                putLong("timestamp", System.currentTimeMillis())
                analyticsId?.let { putString("analytics_id", it) }
            }
            
            firebaseAnalytics?.logEvent(FirebaseAnalytics.Event.LEVEL_END, bundle)
            
            analyticsId?.let {
                CloudAnalyticsManager.logMatchPlayed(
                    analyticsId = it,
                    playerCount = playerCount,
                    totalTurns = totalTurns,
                    isFinalized = isFinalized,
                    durationMillis = durationMillis,
                    maxScore = winnerScore,
                    isOffline = isOffline
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error logging game ended", e)
        }
    }

    /**
     * Logs a generic custom event.
     * 
     * @param eventName Name of the event
     * @param params Optional parameters bundle
     * @param analyticsId Unique user analytics ID
     */
    fun logEvent(
        eventName: String,
        params: Bundle? = null,
        analyticsId: String? = null
    ) {
        if (!isInitialized) {
            Log.w(TAG, "Analytics not initialized, skipping logEvent: $eventName")
            return
        }
        
        try {
            val isOffline = !isNetworkAvailable()
            val finalBundle = (params ?: Bundle()).apply {
                analyticsId?.let { putString("analytics_id", it) }
                putBoolean("is_offline", isOffline)
                putLong("timestamp", System.currentTimeMillis())
            }
            
            firebaseAnalytics?.logEvent(eventName, finalBundle)
            
            analyticsId?.let {
                val metadata = buildMetadataMap(finalBundle)
                CloudAnalyticsManager.logFeatureUsage(
                    it,
                    eventName,
                    metadata,
                    isOffline = isOffline
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error logging event: $eventName", e)
        }
    }
    
    /**
     * Converts a Bundle to a Map for cloud analytics.
     * 
     * @param bundle Bundle to convert
     * @return Map of key-value pairs
     */
    private fun buildMetadataMap(bundle: Bundle): Map<String, Any> {
        val metadata = mutableMapOf<String, Any>()
        
        try {
            @Suppress("DEPRECATION")
            bundle.keySet().forEach { key ->
                bundle.get(key)?.let { value ->
                    metadata[key] = value
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error building metadata map", e)
        }
        
        return metadata
    }
    
    /**
     * Sets a user property for analytics.
     * 
     * @param name Property name
     * @param value Property value
     */
    fun setUserProperty(name: String, value: String) {
        if (!isInitialized) {
            Log.w(TAG, "Analytics not initialized, skipping setUserProperty")
            return
        }
        
        try {
            firebaseAnalytics?.setUserProperty(name, value)
        } catch (e: Exception) {
            Log.e(TAG, "Error setting user property: $name", e)
        }
    }
    
    /**
     * Sets the user ID for analytics.
     * 
     * @param userId Unique user identifier
     */
    fun setUserId(userId: String) {
        if (!isInitialized) {
            Log.w(TAG, "Analytics not initialized, skipping setUserId")
            return
        }
        
        try {
            firebaseAnalytics?.setUserId(userId)
        } catch (e: Exception) {
            Log.e(TAG, "Error setting user ID", e)
        }
    }
}