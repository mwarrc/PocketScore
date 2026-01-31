package com.mwarrc.pocketscore.domain.model

import kotlinx.serialization.Serializable

/**
 * A portable data package for sharing PocketScore records and friends.
 * Used for both individual match sharing and full backups.
 */
@Serializable
data class PocketScoreShare(
    val version: Int = 1,
    val sourceDevice: String? = null,
    val shareDate: Long = System.currentTimeMillis(),
    val friends: List<String> = emptyList(),
    val games: List<GameState> = emptyList()
)
