package com.mwarrc.pocketscore.data.local.room.converter

import androidx.room.TypeConverter
import com.mwarrc.pocketscore.domain.model.GameEventType
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID

class Converters {
    private val json = Json { ignoreUnknownKeys = true }
    
    @TypeConverter
    fun fromIntSet(value: Set<Int>): String = json.encodeToString(value)
    
    @TypeConverter
    fun toIntSet(value: String): Set<Int> {
        return try {
            json.decodeFromString(value)
        } catch (e: Exception) {
            (1..15).toSet()
        }
    }
    
    @TypeConverter
    fun fromGameEventType(value: GameEventType): String = value.name
    
    @TypeConverter
    fun toGameEventType(value: String): GameEventType {
        return try {
            GameEventType.valueOf(value)
        } catch (e: Exception) {
            GameEventType.SCORE
        }
    }
}
