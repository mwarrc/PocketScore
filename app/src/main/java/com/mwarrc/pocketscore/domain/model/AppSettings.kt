package com.mwarrc.pocketscore.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class ScoreboardLayout {
    LIST, GRID
}

@Serializable
data class AppSettings(
    val hapticFeedbackEnabled: Boolean = true,
    val leaderSpotlightEnabled: Boolean = true,
    val defaultLayout: ScoreboardLayout = ScoreboardLayout.GRID,
    val isDarkMode: Boolean? = false, // Default to Light Mode
    val maxPlayers: Int = 8, // Default 8, can be overridden up to 32 (To Do - replace the slider)
    val strictTurnMode: Boolean = false, // When true, only current player can edit scores
    val enforceStrictMode: Boolean = false, // When true, Strict Mode cannot be disabled in-game
    val showHelpInNavBar: Boolean = false, // Toggle visibility of Help in the bottom nav
    val hasSeenOnboarding: Boolean = false, // Track if user has completed onboarding
    val showComingSoonFeatures: Boolean = false, // Toggle visibility of unimplemented features
    val showStrictModeBanner: Boolean = true, // Display banner when strict mode is off
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
    val matchSplitEnabled: Boolean = true, // Toggle for match fee splitting feature
    val matchCost: Double = 20.0, // Cost per match session
    val currencySymbol: String = "KSh", // Default currency (e.g., KSh for Kenya)
    val winnersPay: Boolean = false, // If true, winners also split the cost
    val useCustomKeyboard: Boolean = false, // Toggle for the custom minimal numpad
    val customDeviceName: String? = null, // Personalized name for this device (e.g., "Jacob's Phone")
    val keyboardTheme: KeyboardTheme = KeyboardTheme.AUTO, // Keyboard color theme
    val keyboardTextSize: Float = 24f, // Number text size (20-32sp)
    val keyboardHeight: KeyboardHeight = KeyboardHeight.MEDIUM // Keyboard button height
)

@Serializable
enum class KeyboardTheme {
    AUTO, LIGHT, DARK
}

@Serializable
enum class KeyboardHeight {
    COMPACT, MEDIUM, LARGE
}
