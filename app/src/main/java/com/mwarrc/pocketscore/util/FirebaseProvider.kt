package com.mwarrc.pocketscore.util

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.PersistentCacheSettings

/**
 * Centralized Firebase configuration and provider.
 * 
 * Handles Firestore initialization with optimized offline sync settings.
 * Thread-safe singleton implementation.
 */
object FirebaseProvider {
    
    private const val TAG = "FirebaseProvider"
    private const val CACHE_SIZE_BYTES = 100L * 1024 * 1024 // 100 MB
    
    @Volatile
    private var isInitialized = false
    private val lock = Any()

    /**
     * Gets or initializes the Firestore instance.
     * 
     * Configures persistent cache for offline support on first access.
     * Thread-safe double-checked locking pattern.
     * 
     * @return Configured FirebaseFirestore instance
     */
    fun getFirestore(): FirebaseFirestore {
        val firestore = FirebaseFirestore.getInstance()
        
        if (!isInitialized) {
            synchronized(lock) {
                if (!isInitialized) {
                    setupFirestore(firestore)
                    isInitialized = true
                }
            }
        }
        
        return firestore
    }

    /**
     * Configures Firestore settings for optimal offline support.
     * 
     * @param firestore Firestore instance to configure
     */
    private fun setupFirestore(firestore: FirebaseFirestore) {
        try {
            val settings = FirebaseFirestoreSettings.Builder()
                .setLocalCacheSettings(
                    PersistentCacheSettings.newBuilder()
                        .setSizeBytes(CACHE_SIZE_BYTES)
                        .build()
                )
                .build()
            
            firestore.firestoreSettings = settings
            Log.d(TAG, "Firestore configured with ${CACHE_SIZE_BYTES / 1024 / 1024}MB cache")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to configure Firestore settings", e)
        }
    }
    
    /**
     * Resets initialization state.
     * For testing purposes only.
     */
    internal fun resetForTesting() {
        synchronized(lock) {
            isInitialized = false
        }
    }
}