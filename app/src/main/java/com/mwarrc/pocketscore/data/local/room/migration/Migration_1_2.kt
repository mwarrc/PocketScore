package com.mwarrc.pocketscore.data.local.room.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migration from version 1 to 2.
 * 
 * Changes:
 * - Adds missing event data columns to game_events table (points, scores, etc.)
 * - Adds composite index on games table for active game lookup performance
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
