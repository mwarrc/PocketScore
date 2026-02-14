package com.mwarrc.pocketscore.domain.model

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class GameState(
    val id: String = UUID.randomUUID().toString(),
    val players: List<Player> = emptyList(),
    val isGameActive: Boolean = false,
    val startTime: Long = System.currentTimeMillis(),
    val endTime: Long? = null,
    val lastUpdate: Long = System.currentTimeMillis(),
    val currentPlayerId: String? = null,
    val globalEvents: List<GameEvent> = emptyList(),
    val canUndo: Boolean = false,
    val isFinalized: Boolean = false,
    val ballsOnTable: Set<Int> = (1..15).toSet(),
    val deviceInfo: String? = null,
    val isArchived: Boolean = false
)
