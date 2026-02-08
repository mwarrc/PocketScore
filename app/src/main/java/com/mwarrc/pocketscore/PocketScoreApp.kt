package com.mwarrc.pocketscore

import android.app.Application
import com.mwarrc.pocketscore.util.AnalyticsManager

class PocketScoreApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize Analytics
        AnalyticsManager.initialize(this)
    }
}
