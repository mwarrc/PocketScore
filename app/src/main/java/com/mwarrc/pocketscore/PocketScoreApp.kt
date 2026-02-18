package com.mwarrc.pocketscore

import android.app.Application
import com.mwarrc.pocketscore.util.AnalyticsManager

/**
 * Application class for PocketScore.
 * 
 * Responsible for initializing global singletons like Analytics.
 */
class PocketScoreApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize Analytics
        AnalyticsManager.initialize(this)
    }
}
