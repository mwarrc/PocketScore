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
    val defaultLayout: ScoreboardLayout = ScoreboardLayout.LIST,
    val isDarkMode: Boolean? = null, // null means system default
    val maxPlayers: Int = 8, // Default 8, can be overridden up to 32 (To Do - replace the slider)
    val strictTurnMode: Boolean = true, // When true, only current player can edit scores
    val showHelpInNavBar: Boolean = false, // Toggle visibility of Help in the bottom nav
    val hasSeenOnboarding: Boolean = false, // Track if user has completed onboarding
    val showComingSoonFeatures: Boolean = true // Toggle visibility of unimplemented features
)
