package com.mwarrc.pocketscore.domain.model

import kotlinx.serialization.Serializable

/**
 * Represents the complete application state combining all data sources.
 * 
 * This is the root state object used throughout the application, representing
 * the combined data of the combined data of the current game, history, and user settings.
 * 
 * @property gameState Current active game state
 * @property gameHistory Historical games and past sessions
 * @property settings User preferences and app configuration
 * @property isLoading Indicates if the application is currently loading data
 * @property loadingMessage An optional message to display during loading
 */
@Serializable
data class AppState(
    val gameState: GameState = GameState(),
    val gameHistory: GameHistory = GameHistory(),
    val settings: AppSettings = AppSettings(),
    val isLoading: Boolean = false,
    val loadingMessage: String? = null
)
