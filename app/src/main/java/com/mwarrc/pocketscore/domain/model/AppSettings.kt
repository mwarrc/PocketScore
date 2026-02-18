package com.mwarrc.pocketscore.domain.model

import kotlinx.serialization.Serializable

/**
 * Layout options for the scoreboard display.
 */
@Serializable
enum class ScoreboardLayout {
    /** Linear vertical list */
    LIST,
    /** Grid layout */
    GRID
}

/**
 * Options for sorting the player roster.
 */
@Serializable
enum class RosterSortOption {
    /** User-defined order */
    MANUAL,
    /** Alphabetically by name */
    ALPHABETICAL,
    /** Lowest scores first */
    LOSERS_FIRST,
    /** Highest scores first */
    WINNERS_FIRST,
    /** Random shuffle */
    RANDOM,
    /** Most frequently played players first */
    MOST_PLAYED
}

/**
 * Methods for splitting match costs.
 */
@Serializable
enum class SettlementMethod {
    /** Everyone except the winner(s) pays */
    LOSERS_PAY,
    /** Everyone pays equally */
    ALL_SPLIT,
    /** Bottom X players pay */
    LAST_N_PAY
}

@Serializable
enum class AppTheme {
    /** Follow system theme */
    SYSTEM,
    /** Force light theme */
    LIGHT,
    /** Force dark theme */
    DARK
}

/**
 * Keyboard color theme options.
 */
@Serializable
enum class KeyboardTheme {
    /** Follows system theme */
    AUTO,
    /** Light theme */
    LIGHT,
    /** Dark theme */
    DARK
}

/**
 * Keyboard height presets.
 */
@Serializable
enum class KeyboardHeight {
    /** Compact size */
    COMPACT,
    /** Medium size (default) */
    MEDIUM,
    /** Large size */
    LARGE
}

/**
 * Application settings and user preferences.
 * 
 * This data class contains all configurable options for the app.
 * All fields have sensible defaults for new installations.
 */
@Serializable
data class AppSettings(
    // UI & Feedback
    val hapticFeedbackEnabled: Boolean = true,
    val leaderSpotlightEnabled: Boolean = true,
    val loserSpotlightEnabled: Boolean = true,
    val appTheme: AppTheme = AppTheme.SYSTEM,
    val defaultLayout: ScoreboardLayout = ScoreboardLayout.GRID,
    val autoScrollToActivePlayer: Boolean = true,
    
    // Game Rules
    val maxPlayers: Int = 8,
    val strictTurnMode: Boolean = false,
    val enforceStrictMode: Boolean = false,
    val autoNextTurn: Boolean = false,
    
    // Navigation & Help
    val showHelpInNavBar: Boolean = false,
    val hasSeenOnboarding: Boolean = false,
    val showStrictModeBanner: Boolean = false,
    val showIdentityTip: Boolean = true,
    val gamesPlayedCount: Int = 0,
    
    // Player Management
    val savedPlayerNames: List<String> = emptyList(),
    val showQuickSelectOnHome: Boolean = true,
    val hiddenPlayers: List<String> = emptyList(),
    val rosterSortOption: RosterSortOption = RosterSortOption.MANUAL,
    
    // Snapshots & Backups
    val lastLocalSnapshotTime: Long = 0L,
    val lastSnapshotSize: String = "0 KB",
    val localSnapshotsEnabled: Boolean = true,
    val lastAutoSnapshotDate: String = "",
    val snapshotAfterGameEnabled: Boolean = true,
    val randomSnapshotsEnabled: Boolean = true,
    val redundantSyncEnabled: Boolean = true,
    val backupsFolderUri: String? = null,
    
    // Match Splitting
    val matchSplitEnabled: Boolean = true,
    val matchCost: Double = 20.0,
    val currencySymbol: String = "KSh",
    val settlementMethod: SettlementMethod = SettlementMethod.LOSERS_PAY,
    val lastLosersCount: Int = 2,
    
    // Custom Keyboard
    val useCustomKeyboard: Boolean = false,
    val keyboardTheme: KeyboardTheme = KeyboardTheme.AUTO,
    val keyboardTextSize: Float = 24f,
    val keyboardHeight: KeyboardHeight = KeyboardHeight.MEDIUM,
    
    // Advanced Settings
    val customDeviceName: String? = null,
    val allowEliminatedInput: Boolean = false,
    val analyticsId: String? = null,
    
    // Pool-Specific
    val poolBallManagementEnabled: Boolean = true,
    val ballValues: Map<Int, Int> = DEFAULT_BALL_VALUES,
    val ballValuePresets: List<BallValuePreset> = DEFAULT_PRESETS,
    
    // Guest Mode
    val isGuestSession: Boolean = false,
    val guestSaveRecords: Boolean = false,
    val guestSavePlayers: Boolean = false
) {
    companion object {
        /**
         * Minimum allowed value for keyboardTextSize.
         */
        const val MIN_KEYBOARD_TEXT_SIZE = 20f
        
        /**
         * Maximum allowed value for keyboardTextSize.
         */
        const val MAX_KEYBOARD_TEXT_SIZE = 32f
        
        /**
         * Minimum allowed value for maxPlayers.
         */
        const val MIN_PLAYERS = 2
        
        /**
         * Maximum allowed value for maxPlayers.
         */
        const val MAX_PLAYERS_LIMIT = 32
        
        /**
         * Default ball values for pool scoring.
         */
        val DEFAULT_BALL_VALUES = mapOf(
            1 to 16, 2 to 17, 3 to 3, 4 to 4, 5 to 5,
            6 to 6, 7 to 7, 8 to 8, 9 to 9, 10 to 10,
            11 to 11, 12 to 12, 13 to 13, 14 to 14, 15 to 15
        )
        
        /**
         * Preset configurations for ball values.
         */
        val DEFAULT_PRESETS = listOf(
            BallValuePreset(
                "Default",
                mapOf(
                    1 to 16, 2 to 17, 3 to 3, 4 to 4, 5 to 5,
                    6 to 6, 7 to 7, 8 to 8, 9 to 9, 10 to 10,
                    11 to 11, 12 to 12, 13 to 13, 14 to 14, 15 to 15
                )
            ),
            BallValuePreset(
                "Face Value",
                (1..15).associateWith { it }
            ),
            BallValuePreset(
                "Classic",
                mapOf(
                    1 to 16, 2 to 17, 3 to 6, 4 to 6, 5 to 6,
                    6 to 6, 7 to 7, 8 to 8, 9 to 9, 10 to 10,
                    11 to 11, 12 to 12, 13 to 13, 14 to 14, 15 to 15
                )
            )
        )
    }
    
    /**
     * Validates the settings and returns a copy with corrected values if needed.
     * 
     * @return Validated copy of settings
     */
    fun validate(): AppSettings {
        return copy(
            maxPlayers = maxPlayers.coerceIn(MIN_PLAYERS, MAX_PLAYERS_LIMIT),
            keyboardTextSize = keyboardTextSize.coerceIn(MIN_KEYBOARD_TEXT_SIZE, MAX_KEYBOARD_TEXT_SIZE),
            lastLosersCount = lastLosersCount.coerceAtLeast(1),
            matchCost = matchCost.coerceAtLeast(0.0)
        )
    }
}

/**
 * Preset configuration for ball values in pool scoring.
 * 
 * @property name Display name for the preset
 * @property values Map of ball number to point value
 * @property isDefault Whether this is a default preset (cannot be deleted)
 * @property id Unique identifier
 */
@Serializable
data class BallValuePreset(
    val name: String,
    val values: Map<Int, Int>,
    val isDefault: Boolean = true,
    val id: String = generateId()
) {
    companion object {
        /**
         * Generates a unique ID for presets.
         * Using a function allows for better testing.
         */
        fun generateId(): String = java.util.UUID.randomUUID().toString()
    }
    
    /**
     * Validates that all ball values are positive and within range.
     * 
     * @return true if valid, false otherwise
     */
    fun isValid(): Boolean {
        return values.all { (ball, value) ->
            ball in 1..15 && value > 0
        }
    }
}