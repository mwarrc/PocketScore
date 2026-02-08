package com.mwarrc.pocketscore.util

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics

object AnalyticsManager {
    private var firebaseAnalytics: FirebaseAnalytics? = null

    fun initialize(context: Context) {
        if (firebaseAnalytics == null) {
            firebaseAnalytics = FirebaseAnalytics.getInstance(context)
        }
    }

    /**
     * Logs when a game is successfully started.
     */
    fun logGameStarted(playerCount: Int, isResume: Boolean = false) {
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.LEVEL_NAME, "Match")
            putInt("player_count", playerCount)
            putBoolean("is_resume", isResume)
        }
        firebaseAnalytics?.logEvent(FirebaseAnalytics.Event.LEVEL_START, bundle)
    }

    /**
     * Logs when a game is ended (finalized or reset).
     */
    fun logGameEnded(playerCount: Int, totalTurns: Int, isFinalized: Boolean) {
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.LEVEL_NAME, "Match")
            putInt("player_count", playerCount)
            putInt("total_turns", totalTurns)
            putInt(FirebaseAnalytics.Param.SUCCESS, if (isFinalized) 1 else 0)
        }
        firebaseAnalytics?.logEvent(FirebaseAnalytics.Event.LEVEL_END, bundle)
    }

    /**
     * Generic event logger
     */
    fun logEvent(eventName: String, params: Bundle? = null) {
        firebaseAnalytics?.logEvent(eventName, params)
    }
}
