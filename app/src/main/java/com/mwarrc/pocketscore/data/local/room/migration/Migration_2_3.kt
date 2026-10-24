package com.mwarrc.pocketscore.data.local.room.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migration from version 2 to 3.
 *
 * Schema changes:
 * - Adds the `isPaused` flag to the `games` table so a suspended match can be
 *   retained and resumed without archiving it into history. Default `0`
 *   keeps existing rows as non-paused.
 */
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE games ADD COLUMN isPaused INTEGER NOT NULL DEFAULT 0")
    }
}
