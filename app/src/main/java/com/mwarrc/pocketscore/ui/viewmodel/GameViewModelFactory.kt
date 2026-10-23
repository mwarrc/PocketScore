package com.mwarrc.pocketscore.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mwarrc.pocketscore.data.repository.GameRepository
import com.mwarrc.pocketscore.util.DeviceInfoProvider
import com.mwarrc.pocketscore.util.analytics.AnalyticsTracker
import com.mwarrc.pocketscore.util.analytics.NoOpAnalytics

/**
 * Factory for creating GameViewModel instances with repository dependency injection.
 */
class GameViewModelFactory(
    private val repository: GameRepository,
    private val deviceInfoProvider: DeviceInfoProvider? = null,
    private val analyticsManager: AnalyticsTracker? = null
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(GameViewModel::class.java) -> {
                GameViewModel(
                    repository = repository,
                    deviceInfoProvider = deviceInfoProvider ?: DeviceInfoProvider.Default,
                    analyticsManager = analyticsManager ?: NoOpAnalytics
                ) as T
            }
            else -> throw IllegalArgumentException(
                "Unknown ViewModel class: ${modelClass.name}. " +
                "GameViewModelFactory can only create GameViewModel instances."
            )
        }
    }
}