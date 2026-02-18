package com.mwarrc.pocketscore.util

/**
 * Provider interface for device-specific information.
 * 
 * This abstraction allows the ViewModel to access device info without
 * directly coupling to Android framework classes, improving testability.
 */
interface DeviceInfoProvider {
    /**
     * Returns the device model name.
     * 
     * @return Device model identifier (e.g., "Pixel 6", "SM-G991B")
     */
    fun getDeviceModel(): String

    /**
     * Default implementation that uses Android Build class.
     */
    object Default : DeviceInfoProvider {
        override fun getDeviceModel(): String {
            return android.os.Build.MODEL
        }
    }
}

/**
 * Test implementation for unit testing.
 * 
 * Usage in tests:
 * ```
 * val testProvider = TestDeviceInfoProvider("Test Device")
 * val viewModel = GameViewModel(repository, testProvider)
 * ```
 */
class TestDeviceInfoProvider(private val model: String = "Test Device") : DeviceInfoProvider {
    override fun getDeviceModel(): String = model
}