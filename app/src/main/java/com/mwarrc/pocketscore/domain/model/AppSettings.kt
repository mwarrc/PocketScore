package com.mwarrc.pocketscore.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class ScoreboardLayout {
    LIST, GRID
}

@Serializable
enum class RosterSortOption {
    MANUAL,
    ALPHABETICAL,
    LOSERS_FIRST,
    WINNERS_FIRST,
    RANDOM,
    MOST_PLAYED
}

@Serializable
enum class SettlementMethod {
    LOSERS_PAY,    // Everyone except the winner(s)
    ALL_SPLIT,     // Everyone pays equally
    LAST_N_PAY     // Bottom X players pay
}

@Serializable
data class AppSettings(
    val hapticFeedbackEnabled: Boolean = true,
    val leaderSpotlightEnabled: Boolean = true,
    val loserSpotlightEnabled: Boolean = true,
    val defaultLayout: ScoreboardLayout = ScoreboardLayout.GRID,
    val isDarkMode: Boolean? = false, // Default to Light Mode
    val maxPlayers: Int = 8, // Default 8, can be overridden up to 32 (To Do - replace the slider)
    val strictTurnMode: Boolean = false, // When true, only current player can edit scores
    val enforceStrictMode: Boolean = false, // When true, Strict Mode cannot be disabled in-game
    val showHelpInNavBar: Boolean = false, // Toggle visibility of Help in the bottom nav
    val hasSeenOnboarding: Boolean = false, // Track if user has completed onboarding
    val showComingSoonFeatures: Boolean = false, // Toggle visibility of unimplemented features
    val showStrictModeBanner: Boolean = false, // Display banner when strict mode is off
    val autoScrollToActivePlayer: Boolean = true, // Auto-scroll to current player
    val autoNextTurn: Boolean = false, // Automatically advance turn after scoring
    val savedPlayerNames: List<String> = emptyList(), // Frequently used players (Active Roster)
    val showQuickSelectOnHome: Boolean = true, // Toggle visibility of 'Active Players' on home screen
    val lastLocalSnapshotTime: Long = 0L, // Timestamp of last snapshot
    val lastSnapshotSize: String = "0 KB", // Display string for snapshot size
    val localSnapshotsEnabled: Boolean = true, // Toggle for Daily Auto-Snapshots
    val lastAutoSnapshotDate: String = "", // Format "YYYY-MM-DD" to prevent multiple daily backups
    val showIdentityTip: Boolean = true, // Toggle visibility of the Identity Tip on home screen
    val gamesPlayedCount: Int = 0, // Count of games played to auto-hide tips
    val hiddenPlayers: List<String> = emptyList(), // Players hidden from the leaderboard
    val matchSplitEnabled: Boolean = false, // Toggle for match fee splitting feature
    val matchCost: Double = 20.0, // Cost per match session
    val currencySymbol: String = "KSh", // Default currency (e.g., KSh for Kenya)
    val settlementMethod: SettlementMethod = SettlementMethod.LOSERS_PAY,
    val lastLosersCount: Int = 2, // Number of bottom players to pay if SettlementMethod.LAST_N_PAY
    val useCustomKeyboard: Boolean = false, // Toggle for the custom minimal numpad
    val customDeviceName: String? = null, // Personalized name for this device (e.g., "Jacob's Phone")
    val keyboardTheme: KeyboardTheme = KeyboardTheme.AUTO, // Keyboard color theme
    val keyboardTextSize: Float = 24f, // Number text size (20-32sp)
    val keyboardHeight: KeyboardHeight = KeyboardHeight.MEDIUM, // Keyboard button height
    val allowEliminatedInput: Boolean = false, // If true, eliminated players aren't auto-skipped in non-strict mode
    val rosterSortOption: RosterSortOption = RosterSortOption.MANUAL,
    val analyticsId: String? = null, // Unique ID for database analytics
    val poolBallManagementEnabled: Boolean = true, // Toggle for pool-specific features
    val isIncognitoMode: Boolean = false, // Master toggle for privacy mode
    val incognitoSaveRecords: Boolean = false, // If true, saves game history even in incognito
    val incognitoSavePlayers: Boolean = false // If true, saves player names even in incognito
)

@Serializable
enum class KeyboardTheme {
    AUTO, LIGHT, DARK
}

@Serializable
enum class KeyboardHeight {
    COMPACT, MEDIUM, LARGE
}
