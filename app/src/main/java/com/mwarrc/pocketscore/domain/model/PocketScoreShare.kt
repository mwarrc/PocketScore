package com.mwarrc.pocketscore.domain.model

import kotlinx.serialization.Serializable

/**
 * Portable data package for sharing PocketScore records.
 * 
 * Used for both individual match sharing and full backups.
 * Designed to be serialized to JSON for .pscore files.
 * 
 * @property version Data format version for backward compatibility
 * @property sourceDevice Device where the share was created
 * @property shareDate Share creation timestamp
 * @property friends List of player names from the source
 * @property games List of game states being shared
 */
@Serializable
data class PocketScoreShare(
    val version: Int = CURRENT_VERSION,
    val sourceDevice: String? = null,
    val shareDate: Long = System.currentTimeMillis(),
    val friends: List<String> = emptyList(),
    val games: List<GameState> = emptyList()
) {
    /**
     * Validates the share data.
     */
    fun isValid(): Boolean {
        return version > 0 &&
               shareDate > 0 &&
               games.all { it.isValid() }
    }
    
    /**
     * Gets unique player names from all games.
     */
    fun getAllPlayerNames(): Set<String> {
        return games.flatMap { game ->
            game.players.map { it.name }
        }.toSet()
    }
    
    /**
     * Gets total number of matches in the share.
     */
    fun getMatchCount(): Int = games.size
    
    /**
     * Gets the date range of games in the share.
     */
    fun getDateRange(): Pair<Long, Long>? {
        if (games.isEmpty()) return null
        val earliest = games.minOfOrNull { it.startTime } ?: return null
        val latest = games.maxOfOrNull { it.lastUpdate } ?: return null
        return earliest to latest
    }
    
    /**
     * Checks if this share is compatible with the current version.
     */
    fun isCompatible(): Boolean = version <= CURRENT_VERSION
    
    companion object {
        /**
         * Current data format version.
         */
        const val CURRENT_VERSION = 1
        
        /**
         * Creates a share from a single game.
         */
        fun fromGame(game: GameState, deviceInfo: String? = null): PocketScoreShare {
            return PocketScoreShare(
                sourceDevice = deviceInfo,
                friends = game.players.map { it.name },
                games = listOf(game)
            )
        }
        
        /**
         * Creates a full backup share from games and friends list.
         */
        fun createBackup(
            games: List<GameState>,
            friends: List<String>,
            deviceInfo: String? = null
        ): PocketScoreShare {
            return PocketScoreShare(
                sourceDevice = deviceInfo,
                friends = friends,
                games = games
            )
        }
    }
}