package com.mwarrc.pocketscore.util.analytics

import android.os.Bundle

interface AnalyticsTracker {
    fun logAppOpen()
    fun logAppFirstOpen(bundle: Bundle? = null)
    fun logAppUpdate(previousVersion: String?, newVersion: String?)
    fun logScreenView(screenName: String, previousScreen: String? = null)
    fun logSessionStart(isReturningUser: Boolean, daysSinceFirstOpen: Long, daysSinceLastSession: Long?)
    fun logSessionEnd(sessionDurationSec: Long, gamesPlayedInSession: Int, screensVisited: Int, endReason: String)
    fun logGameStarted(
        gameId: String,
        playerCount: Int,
        isResume: Boolean,
        resumedFromGameId: String?,
        poolBallMgmtEnabled: Boolean,
        strictModeEnabled: Boolean,
        autoNextTurnEnabled: Boolean,
        scoreboardLayout: String,
        inputMethodHint: String?
    )
    fun logGameScoreEntered(
        gameId: String,
        playerIndex: Int,
        points: Int,
        runningTotal: Int,
        turnNumber: Int,
        inputMethod: String,
        timeSinceLastScoreSec: Long?
    )
    fun logGameUndo(gameId: String, turnNumber: Int, pointsReversed: Int)
    fun logGameCorrection(gameId: String, playerIndex: Int, oldScore: Int, newScore: Int)
    fun logGamePaused(gameId: String, durationSoFarSec: Long, turnNumber: Int)
    fun logGameResumed(gameId: String, pauseDurationSec: Long)
    fun logGameCompleted(bundle: Bundle)
    fun logGameAbandoned(bundle: Bundle)
    fun logGamePoolBallRemoved(gameId: String, ballNumber: Int, ballValue: Int, isAutoRemoved: Boolean, ballsRemaining: Int)
    fun logPlayerAdded(totalSavedPlayers: Int, isNewName: Boolean)
    fun logPlayerRemoved(totalSavedPlayers: Int)
    fun logPlayerHidden()
    fun logPlayerQuickSelect(playersSelectedCount: Int)
    fun logRosterSortChanged(sortOption: String)
    fun logSettlementViewed(bundle: Bundle)
    fun logGameShared(shareMethod: String)
    fun logHistoryViewed(totalGamesInHistory: Int)
    fun logHistoryGameOpened(gameAgeDays: Long)
    fun logHistoryGameArchived()
    fun logFeedbackSubmitted(feedbackType: String, rating: Int?)
    fun logSnapshotCreated(trigger: String, sizeBytes: Long)
    fun logBallPresetChanged(presetName: String, isCustom: Boolean)
    fun logWinnerScreenShown(gameId: String, winnerScreenStyle: String)
    fun logSettingChanged(settingName: String, oldValue: String?, newValue: String?)
    fun logDeviceSnapshot(bundle: Bundle)
    fun logConfigSnapshot(bundle: Bundle)
    fun logOnboardingStarted()
    fun logOnboardingCompleted(durationSec: Long)
    fun logEvent(eventName: String, bundle: Bundle? = null)
    fun setUserProperty(name: String, value: String)
    fun setUserId(userId: String?)
}

object NoOpAnalytics : AnalyticsTracker {
    override fun logAppOpen() {}
    override fun logAppFirstOpen(bundle: Bundle?) {}
    override fun logAppUpdate(previousVersion: String?, newVersion: String?) {}
    override fun logScreenView(screenName: String, previousScreen: String?) {}
    override fun logSessionStart(isReturningUser: Boolean, daysSinceFirstOpen: Long, daysSinceLastSession: Long?) {}
    override fun logSessionEnd(sessionDurationSec: Long, gamesPlayedInSession: Int, screensVisited: Int, endReason: String) {}
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
    ) {}
    override fun logGameScoreEntered(
        gameId: String,
        playerIndex: Int,
        points: Int,
        runningTotal: Int,
        turnNumber: Int,
        inputMethod: String,
        timeSinceLastScoreSec: Long?
    ) {}
    override fun logGameUndo(gameId: String, turnNumber: Int, pointsReversed: Int) {}
    override fun logGameCorrection(gameId: String, playerIndex: Int, oldScore: Int, newScore: Int) {}
    override fun logGamePaused(gameId: String, durationSoFarSec: Long, turnNumber: Int) {}
    override fun logGameResumed(gameId: String, pauseDurationSec: Long) {}
    override fun logGameCompleted(bundle: Bundle) {}
    override fun logGameAbandoned(bundle: Bundle) {}
    override fun logGamePoolBallRemoved(gameId: String, ballNumber: Int, ballValue: Int, isAutoRemoved: Boolean, ballsRemaining: Int) {}
    override fun logPlayerAdded(totalSavedPlayers: Int, isNewName: Boolean) {}
    override fun logPlayerRemoved(totalSavedPlayers: Int) {}
    override fun logPlayerHidden() {}
    override fun logPlayerQuickSelect(playersSelectedCount: Int) {}
    override fun logRosterSortChanged(sortOption: String) {}
    override fun logSettlementViewed(bundle: Bundle) {}
    override fun logGameShared(shareMethod: String) {}
    override fun logHistoryViewed(totalGamesInHistory: Int) {}
    override fun logHistoryGameOpened(gameAgeDays: Long) {}
    override fun logHistoryGameArchived() {}
    override fun logFeedbackSubmitted(feedbackType: String, rating: Int?) {}
    override fun logSnapshotCreated(trigger: String, sizeBytes: Long) {}
    override fun logBallPresetChanged(presetName: String, isCustom: Boolean) {}
    override fun logWinnerScreenShown(gameId: String, winnerScreenStyle: String) {}
    override fun logSettingChanged(settingName: String, oldValue: String?, newValue: String?) {}
    override fun logDeviceSnapshot(bundle: Bundle) {}
    override fun logConfigSnapshot(bundle: Bundle) {}
    override fun logOnboardingStarted() {}
    override fun logOnboardingCompleted(durationSec: Long) {}
    override fun logEvent(eventName: String, bundle: Bundle?) {}
    override fun setUserProperty(name: String, value: String) {}
    override fun setUserId(userId: String?) {}
}
