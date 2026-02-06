package com.mwarrc.pocketscore.data.repository

import com.mwarrc.pocketscore.domain.model.AppSettings
import com.mwarrc.pocketscore.domain.model.GameHistory
import com.mwarrc.pocketscore.domain.model.GameState
import com.mwarrc.pocketscore.domain.model.PocketScoreShare
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
    
    suspend fun getShareableData(gameId: String? = null): PocketScoreShare
    suspend fun mergeShareData(share: PocketScoreShare, playerNameMappings: Map<String, String> = emptyMap())
    suspend fun renamePlayer(oldName: String, newName: String)
    
    // Snapshot Management
    suspend fun createLocalSnapshot(name: String)
    suspend fun getLocalSnapshots(): List<Pair<String, Long>> // Name to Timestamp
    suspend fun restoreFromSnapshot(name: String)
    suspend fun getSnapshotContent(name: String): PocketScoreShare?
    suspend fun deleteSnapshot(name: String)
    suspend fun triggerDailyAutoSnapshot()

    suspend fun triggerCloudBackup()
}
