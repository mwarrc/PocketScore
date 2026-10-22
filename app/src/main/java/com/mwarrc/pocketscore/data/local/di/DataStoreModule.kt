package com.mwarrc.pocketscore.data.local.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "game_prefs",
    scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
)

object DataStoreModule {
    fun getDataStore(context: Context): DataStore<Preferences> {
        return context.applicationContext.dataStore
    }
}
