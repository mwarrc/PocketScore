package com.mwarrc.pocketscore.ui.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mwarrc.pocketscore.domain.model.AppSettings
import com.mwarrc.pocketscore.ui.feature.settings.components.AppearanceSection
import com.mwarrc.pocketscore.ui.feature.settings.components.BackupSection
import com.mwarrc.pocketscore.ui.feature.settings.components.BackupVaultWarningBanner
import com.mwarrc.pocketscore.ui.feature.settings.components.GameplaySection
import com.mwarrc.pocketscore.ui.feature.settings.components.LimitsSection
import com.mwarrc.pocketscore.ui.feature.settings.components.PoolSection
import com.mwarrc.pocketscore.ui.feature.settings.components.SettingsDivider
import com.mwarrc.pocketscore.ui.feature.settings.components.SupportSection
import com.mwarrc.pocketscore.ui.feature.settings.components.UtilitiesSection

/**
 * Main Settings Screen for the application.
 *
 * This screen aggregates various configuration sections including gameplay, pool management,
 * utilities, appearance, limits, backups, and support.
 *
 * @param settings Current application settings.
 * @param hasStoragePermission Boolean indicating if storage permission is granted.
 * @param onRequestStoragePermission Callback to request storage permission.
 * @param onUpdateSettings Callback to update application settings.
 * @param onNavigateToGame Navigation callback to game screen.
 * @param onNavigateToHistory Navigation callback to history screen.
 * @param onNavigateToAbout Navigation callback to about screen.
 * @param onNavigateToBackups Navigation callback to backups screen.
 * @param onNavigateToFeedback Navigation callback to feedback screen.
 * @param onNavigateToBallValues Navigation callback to ball values screen.
 * @param onExportRecords Callback to export records.
 * @param onImportRecords Callback to import records.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settings: AppSettings,
    onUpdateSettings: ((AppSettings) -> AppSettings) -> Unit,
    onNavigateToGame: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToAbout: () -> Unit,
    onNavigateToBackups: () -> Unit,
    onNavigateToFeedback: () -> Unit,
    onNavigateToBallValues: () -> Unit,
    onLinkBackupsFolder: () -> Unit,
    onExportRecords: () -> Unit,
    onImportRecords: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Settings,
                            null,
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            "Settings",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateToGame) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                windowInsets = WindowInsets(top = 32.dp)
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 8.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            BackupVaultWarningBanner(
                backupsFolderUri = settings.backupsFolderUri,
                onLinkBackupsFolder = onLinkBackupsFolder
            )

            GameplaySection(
                settings = settings,
                onUpdateSettings = onUpdateSettings
            )

            SettingsDivider(alpha = 0.5f)

            PoolSection(
                settings = settings,
                onUpdateSettings = onUpdateSettings,
                onNavigateToBallValues = onNavigateToBallValues
            )

            SettingsDivider(alpha = 0.5f)

            UtilitiesSection(
                settings = settings,
                onUpdateSettings = onUpdateSettings
            )

            SettingsDivider(alpha = 0.5f)

            AppearanceSection(
                settings = settings,
                onUpdateSettings = onUpdateSettings
            )

            SettingsDivider(alpha = 0.5f)

            LimitsSection(
                settings = settings,
                onUpdateSettings = onUpdateSettings
            )

            SettingsDivider(alpha = 0.5f)

            BackupSection(
                settings = settings,
                onNavigateToBackups = onNavigateToBackups,
                onLinkBackupsFolder = onLinkBackupsFolder,
                backupsFolderUri = settings.backupsFolderUri,
                onExportRecords = onExportRecords,
                onImportRecords = onImportRecords
            )

            SettingsDivider(alpha = 0.5f)

            SupportSection(
                onNavigateToFeedback = onNavigateToFeedback,
                onNavigateToAbout = onNavigateToAbout
            )
        }
    }
}
