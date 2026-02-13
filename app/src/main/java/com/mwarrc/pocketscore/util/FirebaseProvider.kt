package com.mwarrc.pocketscore.util

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.PersistentCacheSettings

/**
 * Centralized Firebase configuration and provider.
 * Handles smart offline sync settings for Firestore.
 */
object FirebaseProvider {
    
    private var isInitialized = false

    fun getFirestore(): FirebaseFirestore {
        val firestore = FirebaseFirestore.getInstance()
        if (!isInitialized) {
            setupFirestore(firestore)
            isInitialized = true
        }
        return firestore
    }

    private fun setupFirestore(firestore: FirebaseFirestore) {
        // Smart Offline Sync Configuration
        // 1. Increase cache size to 100MB (default is 100MB, but let's be explicit)
        // 2. Use PersistentCacheSettings for modern Android Firestore
        val settings = FirebaseFirestoreSettings.Builder()
            .setLocalCacheSettings(
                PersistentCacheSettings.newBuilder()
                    .setSizeBytes(100 * 1024 * 1024) // 100 MB
                    .build()
            )
            .build()
            
        firestore.firestoreSettings = settings
    }
}
