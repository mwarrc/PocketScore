package com.mwarrc.pocketscore.util

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics

object AnalyticsManager {
    private var firebaseAnalytics: FirebaseAnalytics? = null
    private var appContext: Context? = null

    fun initialize(context: Context) {
        appContext = context.applicationContext
        if (firebaseAnalytics == null) {
            firebaseAnalytics = FirebaseAnalytics.getInstance(context)
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val context = appContext ?: return false
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? android.net.ConnectivityManager
        val activeNetwork = connectivityManager?.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        return capabilities.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    /**
     * Logs the app open event (Visit).
     */
    fun logAppOpen(analyticsId: String? = null) {
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
    }

    /**
     * Logs when a game is successfully started.
     */
    fun logGameStarted(playerCount: Int, isResume: Boolean = false, analyticsId: String? = null) {
        val isOffline = !isNetworkAvailable()
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.LEVEL_NAME, "Match")
            putInt("player_count", playerCount)
            putBoolean("is_resume", isResume)
            putBoolean("is_offline", isOffline) // Explicitly track offline starts
            putLong("timestamp", System.currentTimeMillis())
            analyticsId?.let { putString("analytics_id", it) }
        }
        firebaseAnalytics?.logEvent(FirebaseAnalytics.Event.LEVEL_START, bundle)
        
        analyticsId?.let {
            CloudAnalyticsManager.logFeatureUsage(it, "game_start", mapOf("player_count" to playerCount, "is_resume" to isResume), isOffline = isOffline)
        }
    }

    /**
     * Logs when a game is ended (finalized or reset).
     */
    fun logGameEnded(
        playerCount: Int, 
        totalTurns: Int, 
        isFinalized: Boolean, 
        winnerScore: Int = 0,
        durationMillis: Long = 0,
        analyticsId: String? = null
    ) {
        val isOffline = !isNetworkAvailable()
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.LEVEL_NAME, "Match")
            putInt("player_count", playerCount)
            putInt("total_turns", totalTurns)
            putInt(FirebaseAnalytics.Param.SUCCESS, if (isFinalized) 1 else 0)
            putInt("winner_score", winnerScore)
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
    }

    /**
     * Generic event logger
     */
    fun logEvent(eventName: String, params: Bundle? = null, analyticsId: String? = null) {
        val isOffline = !isNetworkAvailable()
        val finalBundle = params ?: Bundle()
        analyticsId?.let { finalBundle.putString("analytics_id", it) }
        firebaseAnalytics?.logEvent(eventName, finalBundle)
        
        analyticsId?.let {
            val metadata = mutableMapOf<String, Any>()
            finalBundle.keySet().forEach { key ->
                metadata[key] = finalBundle.get(key) ?: ""
            }
            CloudAnalyticsManager.logFeatureUsage(it, eventName, metadata, isOffline = isOffline)
        }
    }
}
