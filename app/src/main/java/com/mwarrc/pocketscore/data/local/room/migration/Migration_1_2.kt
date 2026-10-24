package com.mwarrc.pocketscore.data.local.room.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migration from version 1 to 2.
 *
 * Schema changes:
 * - Adds the richer event fields introduced when the history dialog needed
 *   previous/new scores, auto-removed balls and whether input was `0`.
 * - Adds a composite index `index_games_active_lookup` covering
 *   `(isArchived, isGameActive, lastUpdate)` so `GameDao.getActiveGame()` can
 *   seek directly without a full scan of the games table.
 *
 * All new columns are nullable or have `DEFAULT 0` so existing rows populate
 * cleanly without touching historical data.
 */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Add missing columns to game_events table
        db.execSQL("ALTER TABLE game_events ADD COLUMN points INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE game_events ADD COLUMN previousPlayerId TEXT DEFAULT NULL")
        db.execSQL("ALTER TABLE game_events ADD COLUMN isZeroInput INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE game_events ADD COLUMN previousScore INTEGER DEFAULT NULL")
        db.execSQL("ALTER TABLE game_events ADD COLUMN newScore INTEGER DEFAULT NULL")
        db.execSQL("ALTER TABLE game_events ADD COLUMN autoRemovedBall INTEGER DEFAULT NULL")

        // Add composite index for the hot getActiveGame() query
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS index_games_active_lookup " +
                    "ON games (isArchived, isGameActive, lastUpdate)"
        )
    }
}
