package com.mwarrc.pocketscore

import android.app.Application
import com.mwarrc.pocketscore.data.local.migrator.DataStoreToRoomMigrator
import com.mwarrc.pocketscore.data.local.room.AppDatabase
import com.mwarrc.pocketscore.util.analytics.PocketScoreAnalytics
import com.mwarrc.pocketscore.util.analytics.SessionTracker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Application class for PocketScore.
 * 
 * Responsible for initializing global singletons like Analytics and Room migration.
 */
class PocketScoreApp : Application() {
    
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }
    
    override fun onCreate() {
        super.onCreate()
        // Initialize Analytics
        try {
            PocketScoreAnalytics.init(this)
        } catch (_: Exception) {
        }

        // Initialize SessionTracker exactly once at the process level.
        // This avoids duplicate lifecycle observers when MainActivity is recreated.
        try {
            val analytics = PocketScoreAnalytics.get()
            val prefs = getSharedPreferences("pocketscore_analytics", MODE_PRIVATE)
            val tracker = SessionTracker(analytics, prefs)
            tracker.start()
            sessionTracker = tracker
        } catch (_: Exception) {
        }
        
        // Migrate from DataStore to Room on first launch
        applicationScope.launch {
            try {
                val migrator = DataStoreToRoomMigrator(this@PocketScoreApp, database)
                val result = migrator.migrateIfNeeded()
                when (result) {
                    is com.mwarrc.pocketscore.data.local.migrator.MigrationResult.Success -> {
                        android.util.Log.i("PocketScoreApp", "Migration successful: ${result.gamesCount} games migrated")
                    }
                    is com.mwarrc.pocketscore.data.local.migrator.MigrationResult.AlreadyCompleted -> {
                        android.util.Log.d("PocketScoreApp", "Migration already completed")
                    }
                    is com.mwarrc.pocketscore.data.local.migrator.MigrationResult.Error -> {
                        android.util.Log.e("PocketScoreApp", "Migration failed", result.exception)
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("PocketScoreApp", "Error during migration initialization", e)
            }
        }
    }

    companion object {
        /** Process-level singleton so any component can increment counters. */
        @Volatile
        var sessionTracker: SessionTracker? = null
            private set
    }
}
