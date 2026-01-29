package com.mwarrc.pocketscore.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class GameHistory(
    val pastGames: List<GameState> = emptyList()
)
