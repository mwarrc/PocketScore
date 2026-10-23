package com.mwarrc.pocketscore.util.analytics

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.mwarrc.pocketscore.BuildConfig

class PocketScoreAnalytics private constructor(
    private val context: Context,
    private val firebaseAnalytics: FirebaseAnalytics,
    private val deviceIdProvider: DeviceIdProvider
) : AnalyticsTracker {
    @Volatile
    private var enabled: Boolean = true
    @Volatile
    private var sessionId: String = ""

    fun setEnabled(value: Boolean) {
        enabled = value
        try {
            firebaseAnalytics.setAnalyticsCollectionEnabled(value)
            if (value) {
                firebaseAnalytics.setUserId(deviceIdProvider.getDeviceId())
            } else {
                firebaseAnalytics.setUserId(null)
            }
        } catch (_: Exception) {}
    }

    fun setSessionId(id: String) {
        sessionId = id
    }

    private fun bundleWithDefaults(): Bundle {
        return Bundle().apply {
            putString("device_id", deviceIdProvider.getDeviceId())
            if (sessionId.isNotBlank()) putString("session_id", sessionId)
            putString("app_version", BuildConfig.VERSION_NAME)
            putLong("timestamp", System.currentTimeMillis())
        }
    }

    override fun logAppOpen() {
        if (!enabled) return
        try {
            val b = bundleWithDefaults()
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.APP_OPEN, b)
        } catch (_: Exception) {}
    }

    override fun logAppFirstOpen(bundle: Bundle?) {
        if (!enabled) return
        try {
            val b = bundle ?: bundleWithDefaults()
            firebaseAnalytics.logEvent("app_first_open", b)
        } catch (_: Exception) {}
    }

    override fun logAppUpdate(previousVersion: String?, newVersion: String?) {
        if (!enabled) return
        try {
            val b = bundleWithDefaults()
            previousVersion?.let { b.putString("previous_version", it) }
            newVersion?.let { b.putString("new_version", it) }
            firebaseAnalytics.logEvent("app_updated", b)
        } catch (_: Exception) {}
    }

    override fun logScreenView(screenName: String, previousScreen: String?) {
        if (!enabled) return
        try {
            val b = Bundle()
            b.putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
            previousScreen?.let { b.putString("previous_screen", it) }
            b.putString("device_id", deviceIdProvider.getDeviceId())
            if (sessionId.isNotBlank()) b.putString("session_id", sessionId)
            b.putString("app_version", BuildConfig.VERSION_NAME)
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, b)
        } catch (_: Exception) {}
    }

    override fun logSessionStart(isReturningUser: Boolean, daysSinceFirstOpen: Long, daysSinceLastSession: Long?) {
        if (!enabled) return
        try {
            val b = bundleWithDefaults()
            b.putBoolean("is_returning_user", isReturningUser)
            b.putLong("days_since_first_open", daysSinceFirstOpen)
            daysSinceLastSession?.let { b.putLong("days_since_last_session", it) }
            firebaseAnalytics.logEvent("app_session_start", b)
        } catch (_: Exception) {}
    }

    override fun logSessionEnd(sessionDurationSec: Long, gamesPlayedInSession: Int, screensVisited: Int, endReason: String) {
        if (!enabled) return
        try {
            val b = bundleWithDefaults()
            b.putLong("session_duration_sec", sessionDurationSec)
            b.putInt("games_played_in_session", gamesPlayedInSession)
            b.putInt("screens_visited", screensVisited)
            b.putString("end_reason", endReason)
            firebaseAnalytics.logEvent("app_session_end", b)
        } catch (_: Exception) {}
    }

    override fun logGameStarted(
        gameId: String,
        playerCount: Int,
        isResume: Boolean,
        resumedFromGameId: String?,
        poolBallMgmtEnabled: Boolean,
        strictModeEnabled: Boolean,
        autoNextTurnEnabled: Boolean,
        scoreboardLayout: String,
        inputMethodHint: String?
    ) {
        if (!enabled) return
        try {
            val b = bundleWithDefaults()
            b.putString("game_id", gameId)
            b.putInt("player_count", playerCount)
            b.putBoolean("is_resume", isResume)
            resumedFromGameId?.let { b.putString("resumed_from_game_id", it) }
            b.putBoolean("pool_ball_mgmt_enabled", poolBallMgmtEnabled)
            b.putBoolean("strict_mode_enabled", strictModeEnabled)
            b.putBoolean("auto_next_turn_enabled", autoNextTurnEnabled)
            b.putString("scoreboard_layout", scoreboardLayout)
            inputMethodHint?.let { b.putString("input_method_hint", it) }
            b.putString("device_model", android.os.Build.MODEL)
            firebaseAnalytics.logEvent("game_started", b)
        } catch (_: Exception) {}
    }

    override fun logGameScoreEntered(
        gameId: String,
        playerIndex: Int,
        points: Int,
        runningTotal: Int,
        turnNumber: Int,
        inputMethod: String,
        timeSinceLastScoreSec: Long?
    ) {
        if (!enabled) return
        try {
            val b = bundleWithDefaults()
            b.putString("game_id", gameId)
            b.putInt("player_index", playerIndex)
            b.putInt("points", points)
            b.putInt("running_total", runningTotal)
            b.putInt("turn_number", turnNumber)
            b.putString("input_method", inputMethod)
            timeSinceLastScoreSec?.let { b.putLong("time_since_last_score_sec", it) }
            firebaseAnalytics.logEvent("game_score_entered", b)
        } catch (_: Exception) {}
    }

    override fun logGameUndo(gameId: String, turnNumber: Int, pointsReversed: Int) {
        if (!enabled) return
        try {
            val b = bundleWithDefaults()
            b.putString("game_id", gameId)
            b.putInt("turn_number", turnNumber)
            b.putInt("points_reversed", pointsReversed)
            firebaseAnalytics.logEvent("game_undo", b)
        } catch (_: Exception) {}
    }

    override fun logGameCorrection(gameId: String, playerIndex: Int, oldScore: Int, newScore: Int) {
        if (!enabled) return
        try {
            val b = bundleWithDefaults()
            b.putString("game_id", gameId)
            b.putInt("player_index", playerIndex)
            b.putInt("old_score", oldScore)
            b.putInt("new_score", newScore)
            firebaseAnalytics.logEvent("game_correction", b)
        } catch (_: Exception) {}
    }

    override fun logGamePaused(gameId: String, durationSoFarSec: Long, turnNumber: Int) {
        if (!enabled) return
        try {
            val b = bundleWithDefaults()
            b.putString("game_id", gameId)
            b.putLong("duration_so_far_sec", durationSoFarSec)
            b.putInt("turn_number", turnNumber)
            firebaseAnalytics.logEvent("game_paused", b)
        } catch (_: Exception) {}
    }

    override fun logGameResumed(gameId: String, pauseDurationSec: Long) {
        if (!enabled) return
        try {
            val b = bundleWithDefaults()
            b.putString("game_id", gameId)
            b.putLong("pause_duration_sec", pauseDurationSec)
            firebaseAnalytics.logEvent("game_resumed", b)
        } catch (_: Exception) {}
    }

    override fun logGameCompleted(bundle: Bundle) {
        if (!enabled) return
        try {
            firebaseAnalytics.logEvent("game_completed", bundle)
        } catch (_: Exception) {}
    }

    override fun logGameAbandoned(bundle: Bundle) {
        if (!enabled) return
        try {
            firebaseAnalytics.logEvent("game_abandoned", bundle)
        } catch (_: Exception) {}
    }

    override fun logGamePoolBallRemoved(gameId: String, ballNumber: Int, ballValue: Int, isAutoRemoved: Boolean, ballsRemaining: Int) {
        if (!enabled) return
        try {
            val b = bundleWithDefaults()
            b.putString("game_id", gameId)
            b.putInt("ball_number", ballNumber)
            b.putInt("ball_value", ballValue)
            b.putBoolean("is_auto_removed", isAutoRemoved)
            b.putInt("balls_remaining", ballsRemaining)
            firebaseAnalytics.logEvent("game_pool_ball_removed", b)
        } catch (_: Exception) {}
    }

    override fun logPlayerAdded(totalSavedPlayers: Int, isNewName: Boolean) {
        if (!enabled) return
        try {
            val b = bundleWithDefaults()
            b.putInt("total_saved_players", totalSavedPlayers)
            b.putBoolean("is_new_name", isNewName)
            firebaseAnalytics.logEvent("player_added", b)
        } catch (_: Exception) {}
    }

    override fun logPlayerRemoved(totalSavedPlayers: Int) {
        if (!enabled) return
        try {
            val b = bundleWithDefaults()
            b.putInt("total_saved_players", totalSavedPlayers)
            firebaseAnalytics.logEvent("player_removed", b)
        } catch (_: Exception) {}
    }

    override fun logPlayerHidden() {
        if (!enabled) return
        try {
            firebaseAnalytics.logEvent("player_hidden", bundleWithDefaults())
        } catch (_: Exception) {}
    }

    override fun logPlayerQuickSelect(playersSelectedCount: Int) {
        if (!enabled) return
        try {
            val b = bundleWithDefaults()
            b.putInt("players_selected_count", playersSelectedCount)
            firebaseAnalytics.logEvent("player_quick_select", b)
        } catch (_: Exception) {}
    }

    override fun logRosterSortChanged(sortOption: String) {
        if (!enabled) return
        try {
            val b = bundleWithDefaults()
            b.putString("sort_option", sortOption)
            firebaseAnalytics.logEvent("roster_sort_changed", b)
        } catch (_: Exception) {}
    }

    override fun logSettlementViewed(bundle: Bundle) {
        if (!enabled) return
        try {
            firebaseAnalytics.logEvent("settlement_viewed", bundle)
        } catch (_: Exception) {}
    }

    override fun logGameShared(shareMethod: String) {
        if (!enabled) return
        try {
            val b = bundleWithDefaults()
            b.putString("share_method", shareMethod)
            firebaseAnalytics.logEvent("game_shared", b)
        } catch (_: Exception) {}
    }

    override fun logHistoryViewed(totalGamesInHistory: Int) {
        if (!enabled) return
        try {
            val b = bundleWithDefaults()
            b.putInt("total_games_in_history", totalGamesInHistory)
            firebaseAnalytics.logEvent("history_viewed", b)
        } catch (_: Exception) {}
    }

    override fun logHistoryGameOpened(gameAgeDays: Long) {
        if (!enabled) return
        try {
            val b = bundleWithDefaults()
            b.putLong("game_age_days", gameAgeDays)
            firebaseAnalytics.logEvent("history_game_opened", b)
        } catch (_: Exception) {}
    }

    override fun logHistoryGameArchived() {
        if (!enabled) return
        try {
            firebaseAnalytics.logEvent("history_game_archived", bundleWithDefaults())
        } catch (_: Exception) {}
    }

    override fun logFeedbackSubmitted(feedbackType: String, rating: Int?) {
        if (!enabled) return
        try {
            val b = bundleWithDefaults()
            b.putString("feedback_type", feedbackType)
            rating?.let { b.putInt("rating", it) }
            firebaseAnalytics.logEvent("feedback_submitted", b)
        } catch (_: Exception) {}
    }

    override fun logSnapshotCreated(trigger: String, sizeBytes: Long) {
        if (!enabled) return
        try {
            val b = bundleWithDefaults()
            b.putString("trigger", trigger)
            b.putLong("size_bytes", sizeBytes)
            firebaseAnalytics.logEvent("snapshot_created", b)
        } catch (_: Exception) {}
    }

    override fun logBallPresetChanged(presetName: String, isCustom: Boolean) {
        if (!enabled) return
        try {
            val b = bundleWithDefaults()
            b.putString("preset_name", presetName)
            b.putBoolean("is_custom", isCustom)
            firebaseAnalytics.logEvent("ball_preset_changed", b)
        } catch (_: Exception) {}
    }

    override fun logWinnerScreenShown(gameId: String, winnerScreenStyle: String) {
        if (!enabled) return
        try {
            val b = bundleWithDefaults()
            b.putString("game_id", gameId)
            b.putString("winner_screen_style", winnerScreenStyle)
            firebaseAnalytics.logEvent("winner_screen_shown", b)
        } catch (_: Exception) {}
    }

    override fun logSettingChanged(settingName: String, oldValue: String?, newValue: String?) {
        if (!enabled) return
        try {
            val b = bundleWithDefaults()
            b.putString("setting_name", settingName)
            oldValue?.let { b.putString("old_value", it) }
            newValue?.let { b.putString("new_value", it) }
            firebaseAnalytics.logEvent("setting_changed", b)
        } catch (_: Exception) {}
    }

    override fun logDeviceSnapshot(bundle: Bundle) {
        if (!enabled) return
        try {
            firebaseAnalytics.logEvent("device_snapshot", bundle)
        } catch (_: Exception) {}
    }

    override fun logConfigSnapshot(bundle: Bundle) {
        if (!enabled) return
        try {
            firebaseAnalytics.logEvent("config_snapshot", bundle)
        } catch (_: Exception) {}
    }

    override fun logOnboardingStarted() {
        if (!enabled) return
        try {
            firebaseAnalytics.logEvent("onboarding_started", bundleWithDefaults())
        } catch (_: Exception) {}
    }

    override fun logOnboardingCompleted(durationSec: Long) {
        if (!enabled) return
        try {
            val b = bundleWithDefaults()
            b.putLong("duration_sec", durationSec)
            firebaseAnalytics.logEvent("onboarding_completed", b)
        } catch (_: Exception) {}
    }

    override fun logEvent(eventName: String, bundle: Bundle?) {
        if (!enabled) return
        try {
            val b = bundle ?: Bundle()
            if (!b.containsKey("device_id")) b.putString("device_id", deviceIdProvider.getDeviceId())
            if (sessionId.isNotBlank() && !b.containsKey("session_id")) b.putString("session_id", sessionId)
            if (!b.containsKey("app_version")) b.putString("app_version", BuildConfig.VERSION_NAME)
            firebaseAnalytics.logEvent(eventName, b)
        } catch (_: Exception) {}
    }

    override fun setUserProperty(name: String, value: String) {
        if (!enabled) return
        try {
            firebaseAnalytics.setUserProperty(name, value)
        } catch (_: Exception) {}
    }

    override fun setUserId(userId: String?) {
        if (!enabled) return
        try {
            firebaseAnalytics.setUserId(userId)
        } catch (_: Exception) {}
    }

    companion object {
        @Volatile
        private var instance: PocketScoreAnalytics? = null

        fun init(context: Context): PocketScoreAnalytics {
            return instance ?: synchronized(this) {
                instance ?: run {
                    val firebase = FirebaseAnalytics.getInstance(context.applicationContext)
                    val deviceProvider = DeviceIdProvider(context)
                    val inst = PocketScoreAnalytics(context.applicationContext, firebase, deviceProvider)
                    firebase.setAnalyticsCollectionEnabled(true)
                    try {
                        firebase.setUserId(deviceProvider.getDeviceId())
                    } catch (_: Exception) {}
                    instance = inst
                    inst
                }
            }
        }

        fun get(): PocketScoreAnalytics {
            return instance ?: throw IllegalStateException("PocketScoreAnalytics not initialized")
        }

        fun deviceIdProvider(context: Context): DeviceIdProvider {
            return DeviceIdProvider(context)
        }
    }
}
