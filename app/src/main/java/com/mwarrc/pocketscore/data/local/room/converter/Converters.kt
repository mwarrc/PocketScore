package com.mwarrc.pocketscore.data.local.room.converter

import androidx.room.TypeConverter
import com.mwarrc.pocketscore.domain.model.GameEventType
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Room type converters for values SQLite has no native column type for.
 *
 * Each converter is deliberately lossy-safe: decoding malformed or unknown data
 * falls back to a sane default rather than throwing, because one bad row should
 * not make an entire table unreadable.
 */
class Converters {

    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Serialises the set of balls still on the table as a JSON array.
     *
     * @param value Ball numbers currently on the table (normally 1..15).
     * @return JSON array string, e.g. `[1,2,3]`.
     */
    @TypeConverter
    fun fromIntSet(value: Set<Int>): String = json.encodeToString(value)

    /**
     * Parses a stored ball set.
     *
     * Older or partially written rows may not contain valid JSON. In that case
     * the converter assumes a full rack rather than an empty table, because an
     * empty set would make every ball look pocketed.
     *
     * @param value JSON array previously written by [fromIntSet].
     * @return The decoded set, or all balls 1..15 if decoding fails.
     */
    @TypeConverter
    fun toIntSet(value: String): Set<Int> {
        return try {
            json.decodeFromString(value)
        } catch (e: Exception) {
            (1..15).toSet()
        }
    }

    /**
     * Stores a [GameEventType] by name so the schema does not depend on enum
     * ordinal values (reordering the enum would silently corrupt old rows).
     *
     * @param value Event type to persist.
     * @return The enum's `name`.
     */
    @TypeConverter
    fun fromGameEventType(value: GameEventType): String = value.name

    /**
     * Rebuilds a [GameEventType] from its stored name.
     *
     * Unknown names can appear after an app downgrade or a renamed enum entry.
     * They fall back to [GameEventType.SCORE], the most common and least
     * destructive event type.
     *
     * @param value Stored enum name.
     * @return The matching event type, or [GameEventType.SCORE] if unknown.
     */
    @TypeConverter
    fun toGameEventType(value: String): GameEventType {
        return try {
            GameEventType.valueOf(value)
        } catch (e: Exception) {
            GameEventType.SCORE
        }
    }
}
