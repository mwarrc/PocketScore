package com.mwarrc.pocketscore.domain.model

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
enum class GameEventType {
    SCORE,
    CORRECTION,
    UNDO,
    STATUS_CHANGE
}

@Serializable
data class GameEvent(
    val id: String = UUID.randomUUID().toString(),
    val playerId: String,
    val playerName: String,
    val type: GameEventType,
    val points: Int,
    val previousPlayerId: String? = null,
    val isZeroInput: Boolean = false,
    val message: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
