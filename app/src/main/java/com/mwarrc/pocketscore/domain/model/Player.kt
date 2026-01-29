package com.mwarrc.pocketscore.domain.model

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class ScoreEvent(
    val points: Int,
    val timestamp: Long = System.currentTimeMillis()
)

@Serializable
data class Player(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val score: Int = 0,
    val history: List<Int> = emptyList(), // Retaining for backwards compatibility or simple undo
    val eventHistory: List<ScoreEvent> = emptyList(), // Structured history
    val isActive: Boolean = true
)
