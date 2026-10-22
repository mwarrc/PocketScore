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
    
    abstract fun gameDao(): GameDao
    abstract fun savedPlayerDao(): SavedPlayerDao
    
    companion object {
        private const val DATABASE_NAME = "pocketscore_database"
        const val TAG = "AppDatabase"
        
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                .fallbackToDestructiveMigrationOnDowngrade(true)
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onOpen(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                        super.onOpen(db)
                        db.execSQL("PRAGMA foreign_keys = ON;")
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

