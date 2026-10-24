package com.mwarrc.pocketscore.data.local.room.entity

import androidx.room.Embedded
import androidx.room.Relation

/**
 * Full game aggregate loaded via Room's `@Relation`.
 *
 * This is the shape consumed by [com.mwarrc.pocketscore.data.repository.GameRepositoryImplRoom]
 * when building a [com.mwarrc.pocketscore.domain.model.GameState]. Loading players
 * and events in one query avoids N+1 calls.
 *
 * @property game Core match header.
 * @property players All players who took part in the match.
 * @property events Complete audit log in table order; mappers sort/shape it
 *   further for the UI.
 */
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
 * Lightweight aggregate for list views.
 *
 * Used by the history/records queries that only need a player's name and score
 * to render cards. Skipping events avoids pulling potentially thousands of rows
 * into memory and makes the history flow cheaper to diff.
 *
 * @property game Core match header.
 * @property players All players who took part in the match.
 */
data class GameWithPlayers(
    @Embedded val game: GameEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "gameId"
    )
    val players: List<PlayerEntity>
)
