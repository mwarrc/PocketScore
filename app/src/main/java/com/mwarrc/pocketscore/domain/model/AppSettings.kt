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
    val autoNextTurn: Boolean = true, // Automatically advance turn after scoring
    val savedPlayerNames: List<String> = emptyList(), // Frequently used players (Active Roster)
    val showQuickSelectOnHome: Boolean = true, // Toggle visibility of 'Active Players' on home screen
    val lastLocalSnapshotTime: Long = 0L, // Timestamp of last snapshot
    val lastSnapshotSize: String = "0 KB", // Display string for snapshot size
    val localSnapshotsEnabled: Boolean = false, // Toggle for Daily Auto-Snapshots
    val lastAutoSnapshotDate: String = "" // Format "YYYY-MM-DD" to prevent multiple daily backups
)
