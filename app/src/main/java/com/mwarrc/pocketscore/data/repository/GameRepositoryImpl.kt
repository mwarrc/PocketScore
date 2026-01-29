package com.mwarrc.pocketscore.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.mwarrc.pocketscore.domain.model.AppSettings
import com.mwarrc.pocketscore.domain.model.GameHistory
import com.mwarrc.pocketscore.domain.model.GameState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "game_prefs")

class GameRepositoryImpl(private val context: Context) : GameRepository {

    private val gameStateKey = stringPreferencesKey("game_state")
    private val gameHistoryKey = stringPreferencesKey("game_history")
    private val appSettingsKey = stringPreferencesKey("app_settings")

    override val gameState: Flow<GameState> = context.dataStore.data.map { preferences ->
        preferences[gameStateKey]?.let {
            try { Json.decodeFromString<GameState>(it) } catch (e: Exception) { GameState() }
        } ?: GameState()
    }

    override val gameHistory: Flow<GameHistory> = context.dataStore.data.map { preferences ->
        preferences[gameHistoryKey]?.let {
            try { Json.decodeFromString<GameHistory>(it) } catch (e: Exception) { GameHistory() }
        } ?: GameHistory()
    }

    override val appSettings: Flow<AppSettings> = context.dataStore.data.map { preferences ->
        preferences[appSettingsKey]?.let {
            try { Json.decodeFromString<AppSettings>(it) } catch (e: Exception) { AppSettings() }
        } ?: AppSettings()
    }

    override suspend fun saveGameState(gameState: GameState) {
        context.dataStore.edit { preferences ->
            preferences[gameStateKey] = Json.encodeToString(gameState)
        }
    }

    override suspend fun clearGameState() {
        context.dataStore.edit { preferences ->
            preferences.remove(gameStateKey)
        }
    }

    override suspend fun archiveCurrentGame(gameState: GameState) {
        val currentHistory = gameHistory.first()
        val updatedHistory = currentHistory.copy(
            pastGames = (listOf(gameState.copy(endTime = System.currentTimeMillis())) + currentHistory.pastGames).take(50)
        )
        context.dataStore.edit { preferences ->
            preferences[gameHistoryKey] = Json.encodeToString(updatedHistory)
        }
    }

    override suspend fun deleteGameFromHistory(gameId: String) {
        val currentHistory = gameHistory.first()
        val updatedHistory = currentHistory.copy(
            pastGames = currentHistory.pastGames.filter { it.id != gameId }
        )
        context.dataStore.edit { preferences ->
            preferences[gameHistoryKey] = Json.encodeToString(updatedHistory)
        }
    }

    override suspend fun updateSettings(settings: AppSettings) {
        context.dataStore.edit { preferences ->
            preferences[appSettingsKey] = Json.encodeToString(settings)
        }
    }
}
