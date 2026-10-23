package com.mwarrc.pocketscore.util.analytics

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import java.util.UUID
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

/**
 * Tracks app sessions using ProcessLifecycleOwner.
 *
 * Generates a UUID-based session_id on each foreground transition and pushes it
 * to [PocketScoreAnalytics] so every subsequent event carries the same session
 * identifier. Also fires `app_session_start` / `app_session_end` events and
 * keeps running counters for screens visited and games played within the session.
 *
 * Must be initialised exactly once (in [PocketScoreApp.onCreate]) to avoid
 * duplicate lifecycle observers on Activity recreation.
 */
class SessionTracker(
    private val analytics: PocketScoreAnalytics,
    private val prefs: SharedPreferences
) : DefaultLifecycleObserver {

    private val sessionStartTime = AtomicLong(0)
    private val screensVisited = AtomicInteger(0)
    private val gamesPlayedInSession = AtomicInteger(0)
    private val isForeground = AtomicBoolean(false)

    @Volatile
    private var currentSessionId: String = ""

    fun start() {
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    override fun onStart(owner: LifecycleOwner) {
        if (isForeground.getAndSet(true)) return

        val now = System.currentTimeMillis()

        // Generate a fresh UUID session id and push it to PocketScoreAnalytics
        currentSessionId = UUID.randomUUID().toString()
        analytics.setSessionId(currentSessionId)

        sessionStartTime.set(now)
        screensVisited.set(0)
        gamesPlayedInSession.set(0)

        // First-open tracking
        val firstOpen = prefs.getLong(KEY_FIRST_OPEN, 0L)
        if (firstOpen == 0L) {
            prefs.edit().putLong(KEY_FIRST_OPEN, now).apply()
        }
        val actualFirstOpen = if (firstOpen == 0L) now else firstOpen
        val daysSinceFirst = (now - actualFirstOpen) / 86_400_000L

        val lastEnd = prefs.getLong(KEY_LAST_SESSION_END, 0L)
        val daysSinceLast: Long? = if (lastEnd == 0L) null else (now - lastEnd) / 86_400_000L
        val isReturning = lastEnd > 0L

        analytics.logSessionStart(isReturning, daysSinceFirst, daysSinceLast)
        analytics.logAppOpen()
    }

    override fun onStop(owner: LifecycleOwner) {
        if (!isForeground.getAndSet(false)) return

        val end = System.currentTimeMillis()
        val start = sessionStartTime.get()
        if (start == 0L) return

        val durationSec = (end - start) / 1000L
        val visited = screensVisited.get()
        val played = gamesPlayedInSession.get()

        analytics.logSessionEnd(durationSec, played, visited, "background")
        prefs.edit().putLong(KEY_LAST_SESSION_END, end).apply()
    }

    /** Increment the screen-visit counter for the current session. */
    fun trackScreen() {
        screensVisited.incrementAndGet()
    }

    /** Increment the games-played counter for the current session. */
    fun trackGamePlayed() {
        gamesPlayedInSession.incrementAndGet()
    }

    fun getSessionId(): String = currentSessionId

    companion object {
        private const val KEY_FIRST_OPEN = "first_open_ms"
        private const val KEY_LAST_SESSION_END = "last_session_end_ms"
    }
}

/**
 * Provides the shared SharedPreferences instance used by analytics components.
 */
class FirstOpenProvider(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("pocketscore_analytics", Context.MODE_PRIVATE)
    fun getSharedPreferences() = prefs
}
