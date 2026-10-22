package com.mwarrc.pocketscore.data.local.room.entity

import androidx.room.Embedded
import androidx.room.Relation

data class GameWithPlayersAndEvents(
    @Embedded val game: GameEntity,
    
    @Relation(
        parentColumn = "id",
        entityColumn = "gameId"
    )
    val players: List<PlayerEntity>,
    
    @Relation(
        parentColumn = "id",
        entityColumn = "gameId"
    )
    val events: List<GameEventEntity>
)

/**
 * Lightweight relation that loads game + players WITHOUT events.
 * Used for history listing where event details aren't needed,
 * avoiding loading thousands of event rows into memory.
 */
data class GameWithPlayers(
    @Embedded val game: GameEntity,
    
    @Relation(
        parentColumn = "id",
        entityColumn = "gameId"
    )
    val players: List<PlayerEntity>
)
