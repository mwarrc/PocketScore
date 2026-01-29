package com.mwarrc.pocketscore.data.repository

import com.mwarrc.pocketscore.domain.model.AppSettings
import com.mwarrc.pocketscore.domain.model.GameHistory
import com.mwarrc.pocketscore.domain.model.GameState
import kotlinx.coroutines.flow.Flow

interface GameRepository {
    val gameState: Flow<GameState>
    val gameHistory: Flow<GameHistory>
    val appSettings: Flow<AppSettings>
    
    suspend fun saveGameState(gameState: GameState)
    suspend fun clearGameState()
    
    suspend fun archiveCurrentGame(gameState: GameState)
    suspend fun deleteGameFromHistory(gameId: String)
    suspend fun updateSettings(settings: AppSettings)
}
