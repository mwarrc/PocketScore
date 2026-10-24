package com.mwarrc.pocketscore.data.local.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.mwarrc.pocketscore.data.local.room.converter.Converters
import com.mwarrc.pocketscore.data.local.room.dao.GameDao
import com.mwarrc.pocketscore.data.local.room.dao.SavedPlayerDao
import com.mwarrc.pocketscore.data.local.room.entity.GameEntity
import com.mwarrc.pocketscore.data.local.room.entity.GameEventEntity
import com.mwarrc.pocketscore.data.local.room.entity.PlayerEntity
import com.mwarrc.pocketscore.data.local.room.entity.SavedPlayerEntity
import com.mwarrc.pocketscore.data.local.room.migration.MIGRATION_1_2
import com.mwarrc.pocketscore.data.local.room.migration.MIGRATION_2_3

/**
 * Room database for games, players, event logs and the saved-player roster.
 *
 * Data is normalised into four tables instead of the previous JSON blobs:
 *  - `games`       - one row per match (active, paused, finished or archived).
 *  - `players`     - score lines, cascaded from their game.
 *  - `game_events` - append-only audit log, cascaded from their game.
 *  - `saved_players` - reusable roster entries independent of any game.
 *
 * Child rows use `ON DELETE CASCADE`, so deleting a game removes its players and
 * events without callers having to clean up manually. Foreign keys are enabled
 * per connection in [onOpen] because SQLite disables them by default.
 *
 * Bump [version] and add a [androidx.room.migration.Migration] whenever the
 * schema changes. `exportSchema` is disabled because no schema history is kept
 * in version control; migrations are covered by the migration classes instead.
 */
@Database(
    entities = [
        GameEntity::class,
        PlayerEntity::class,
        GameEventEntity::class,
        SavedPlayerEntity::class
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    /** Queries and transactions for games, their players and their events. */
    abstract fun gameDao(): GameDao

    /** Queries for the reusable saved-player roster. */
    abstract fun savedPlayerDao(): SavedPlayerDao

    companion object {
        private const val DATABASE_NAME = "pocketscore_database"

        /** Logcat tag used by database-related diagnostics. */
        const val TAG = "AppDatabase"

        /**
         * Cached singleton. Marked `@Volatile` so the double-checked locking
         * below is safe across threads.
         */
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Returns the process-wide database, building it on first access.
         *
         * Using the application context avoids leaking an Activity. The build is
         * synchronised because two threads could otherwise create two databases
         * pointed at the same file.
         *
         * @param context Any context; the application context is used internally.
         * @return The shared [AppDatabase] instance.
         */
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                // Downgrades only happen in development when installing an older
                // build. Destroying the local cache is preferable to crashing.
                .fallbackToDestructiveMigrationOnDowngrade(true)
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onOpen(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                        super.onOpen(db)
                        // SQLite requires this pragma per connection; without it
                        // the CASCADE rules on players/events are not enforced.
                        db.execSQL("PRAGMA foreign_keys = ON;")
                        // Lets SQLite refresh its query-planner statistics.
                        db.execSQL("PRAGMA optimize;")
                    }
                })
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
