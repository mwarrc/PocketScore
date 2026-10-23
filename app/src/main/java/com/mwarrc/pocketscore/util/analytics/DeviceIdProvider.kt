package com.mwarrc.pocketscore.util.analytics

import android.content.Context
import android.content.SharedPreferences
import java.util.UUID

class DeviceIdProvider(context: Context) {
    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences("pocketscore_analytics", Context.MODE_PRIVATE)

    @Volatile
    private var cachedId: String? = null

    @Synchronized
    fun getDeviceId(): String {
        cachedId?.let { return it }
        val stored = prefs.getString(KEY_DEVICE_ID, null)
        if (!stored.isNullOrBlank()) {
            cachedId = stored
            return stored
        }
        val newId = UUID.randomUUID().toString()
        prefs.edit().putString(KEY_DEVICE_ID, newId).apply()
        cachedId = newId
        return newId
    }

    fun migrateFromLegacy(legacyAnalyticsId: String?) {
        if (legacyAnalyticsId.isNullOrBlank()) return
        val existing = prefs.getString(KEY_DEVICE_ID, null)
        if (existing.isNullOrBlank()) {
            prefs.edit().putString(KEY_DEVICE_ID, legacyAnalyticsId).apply()
            cachedId = legacyAnalyticsId
        }
    }

    companion object {
        private const val KEY_DEVICE_ID = "device_id"
    }
}
