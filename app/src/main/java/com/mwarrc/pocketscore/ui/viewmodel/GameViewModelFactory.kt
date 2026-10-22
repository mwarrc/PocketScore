package com.mwarrc.pocketscore.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mwarrc.pocketscore.data.repository.GameRepository
import com.mwarrc.pocketscore.util.AnalyticsManager
import com.mwarrc.pocketscore.util.DeviceInfoProvider

/**
 * Factory for creating GameViewModel instances with repository dependency injection.
 * 
 * This factory is used when dependency injection frameworks like Hilt are not available.
 * 
 * Usage:
 * ```
 * val viewModel: GameViewModel by viewModels {
 *     GameViewModelFactory(repository)
 * }
 * ```
 * 
 * For Hilt-based projects, consider using @HiltViewModel annotation instead:
 * ```
 * @HiltViewModel
 * class GameViewModel @Inject constructor(
 *     private val repository: GameRepository,
 *     private val deviceInfoProvider: DeviceInfoProvider,
 *     private val analyticsManager: AnalyticsManager
 * ) : ViewModel()
 * ```
 * 
 * @param repository The game data repository
 * @param deviceInfoProvider Optional provider for device information (uses default if null)
 * @param analyticsManager Optional analytics manager (uses singleton if null)
 */
class GameViewModelFactory(
    private val repository: GameRepository,
    private val deviceInfoProvider: DeviceInfoProvider? = null,
    private val analyticsManager: AnalyticsManager? = null
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(GameViewModel::class.java) -> {
                GameViewModel(
                    repository = repository,
                    deviceInfoProvider = deviceInfoProvider ?: DeviceInfoProvider.Default,
                    analyticsManager = analyticsManager ?: AnalyticsManager
                ) as T
            }
            else -> throw IllegalArgumentException(
                "Unknown ViewModel class: ${modelClass.name}. " +
                "GameViewModelFactory can only create GameViewModel instances."
            )
        }
    }
}