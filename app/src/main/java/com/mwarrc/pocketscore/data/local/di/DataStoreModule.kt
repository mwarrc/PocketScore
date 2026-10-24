package com.mwarrc.pocketscore.data.local.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * Preferences DataStore for settings and legacy persistence.
 *
 * Declared as a single top-level property so Android creates exactly one
 * [DataStore] instance per process. A second instance pointing at the same file
 * would throw at runtime, so every consumer must go through [DataStoreModule].
 *
 * The IO dispatcher keeps file reads/writes off the main thread, and the
 * [SupervisorJob] prevents one failed write from cancelling the store's scope.
 */
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "game_prefs",
    scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
)

/**
 * Provides process-wide access to the app's preferences store.
 *
 * Historically this store held games, history and settings as JSON blobs. Games
 * now live in Room (see `AppDatabase`); this store remains the source of truth
 * for [com.mwarrc.pocketscore.domain.model.AppSettings] and for the one-time
 * migration handshake performed by `DataStoreToRoomMigrator`.
 */
object DataStoreModule {

    /**
     * Returns the shared preferences store for [context].
     *
     * @param context Any context; the application context is used internally so
     *   callers cannot leak an Activity.
     * @return The single [DataStore] instance for this process.
     */
    fun getDataStore(context: Context): DataStore<Preferences> {
        return context.applicationContext.dataStore
    }
}
