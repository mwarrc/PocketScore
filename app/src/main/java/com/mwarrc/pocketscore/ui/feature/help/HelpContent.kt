package com.mwarrc.pocketscore.ui.feature.help

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Represents a single help entry in the FAQ system.
 *
 * To add a new item: append a [HelpEntry] to the relevant category in [HelpContent.categories].
 *
 * @property title       The feature name or question.
 * @property description Detailed explanation or answer.
 * @property icon        Optional icon to represent the item visually.
 */
data class HelpEntry(
    val title: String,
    val description: String,
    val icon: ImageVector? = null
)

/**
 * Represents a grouping of related [HelpEntry] items.
 *
 * @property title       Category name shown on the card header.
 * @property description One-line summary shown as the card subtitle.
 * @property icon        Icon shown in the category badge.
 * @property entries     The FAQ items within this category.
 */
data class HelpCategory(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val entries: List<HelpEntry>
)

/**
 * Central repository for all help documentation.
 *
 * NON-TECHNICAL GUIDE:
 * – To add a new category: append a [HelpCategory] to the list.
 * – To add a new FAQ: add a [HelpEntry] to the correct category's `entries` list.
 */
object HelpContent {
    val categories = listOf(
        HelpCategory(
            title = "Mastering Records",
            description = "Analyze matches, settle payments, track ranks.",
            icon = Icons.Default.History,
            entries = listOf(
                HelpEntry(
                    title = "Game Insights",
                    description = "Tap any record to view a deep-dive analysis. See scoring patterns, per-turn averages, and total duration stats.",
                    icon = Icons.Default.Analytics
                ),
                HelpEntry(
                    title = "Settle (Payment Split)",
                    description = "In the 'Settle' tab, the app calculates how much each player owes based on match performance. Perfect for sharing entry fees or prizes.",
                    icon = Icons.Default.Payments
                ),
                HelpEntry(
                    title = "Batch Export",
                    description = "Long-press a record to enter selection mode. Select multiple games and export them as a single .pscore file for sharing or backup.",
                    icon = Icons.Default.Share
                ),
                HelpEntry(
                    title = "Ranks (Leaderboard)",
                    description = "The 'Ranks' tab calculates global performance. Players are ranked by winrate and average points across all saved history.",
                    icon = Icons.Default.Leaderboard
                )
            )
        ),
        HelpCategory(
            title = "Game Play",
            description = "How the scoreboard tracks your sessions.",
            icon = Icons.Default.SportsEsports,
            entries = listOf(
                HelpEntry(
                    title = "Smart Math Tool",
                    description = "Open the calculator to enter expressions like '7+1+14'. Tap a player to apply the result instantly — no more mental math.",
                    icon = Icons.Default.Calculate
                ),
                HelpEntry(
                    title = "Elimination Warning",
                    description = "For pool games, the app tracks 'Balls on Table' and warns you with a 'Match Insight' banner if a comeback is mathematically impossible.",
                    icon = Icons.Default.QueryStats
                ),
                HelpEntry(
                    title = "Strict Turn Security",
                    description = "Enable in settings to lock scoring to the current player only. Prevents tampering and ensures honest competitive play.",
                    icon = Icons.Default.Lock
                ),
                HelpEntry(
                    title = "Leader Spotlight",
                    description = "The current leader gets a gold glow and a 'LEADER' badge. Tie games highlight multiple leaders simultaneously.",
                    icon = Icons.Default.Star
                ),
                HelpEntry(
                    title = "Quick Undo",
                    description = "Mistakes happen. Undo reverts the last score and returns the turn to the correct player automatically.",
                    icon = Icons.AutoMirrored.Filled.Undo
                )
            )
        ),
        HelpCategory(
            title = "The Pool (Roster)",
            description = "Manage your players and global visibility.",
            icon = Icons.Default.Group,
            entries = listOf(
                HelpEntry(
                    title = "Manage People",
                    description = "In the 'Pool' tab, long-press players to hide them from 'Ranks' (stats preserved) or 'Home' (keeps your active roster clean).",
                    icon = Icons.Default.VisibilityOff
                ),
                HelpEntry(
                    title = "Global Renaming",
                    description = "Renaming a player in the Pool updates their name across every record in your full match history.",
                    icon = Icons.Default.Edit
                ),
                HelpEntry(
                    title = "Guest Sessions",
                    description = "Enable 'Guest Mode' on Home to play rounds that won't save to history or create permanent player entries.",
                    icon = Icons.Default.PersonSearch
                )
            )
        ),
        HelpCategory(
            title = "Backup Vault",
            description = "4-layer redundant protection for your data.",
            icon = Icons.Default.Security,
            entries = listOf(
                HelpEntry(
                    title = "Redundant Safety",
                    description = "Every snapshot is mirrored across 4 locations: Internal Storage, your Linked Folder, the Android Sync folder, and Downloads.",
                    icon = Icons.Default.Sync
                ),
                HelpEntry(
                    title = "Auto-Vault (Downloads)",
                    description = "By default, the app syncs records to a 'PocketScore' folder in your Downloads — a safety net even without a custom linked folder.",
                    icon = Icons.Default.FileDownload
                ),
                HelpEntry(
                    title = "Recovering Records",
                    description = "After reinstalling or moving devices, open Backup Center. The app scans your Downloads and Documents for records to restore automatically.",
                    icon = Icons.Default.SettingsBackupRestore
                ),
                HelpEntry(
                    title = "Manual Snapshots",
                    description = "Create manual data snapshots before major updates. Share these portable files as backups to other devices.",
                    icon = Icons.Default.Save
                )
            )
        ),
        HelpCategory(
            title = "Open Source & Freedom",
            description = "Community driven, transparent, and always free.",
            icon = Icons.Default.Code,
            entries = listOf(
                HelpEntry(
                    title = "How to Contribute",
                    description = "PocketScore is built by the community. You can contribute by reporting bugs, suggesting features, or submitting PRs: https://github.com/mwarrc/PocketScore",
                    icon = Icons.Default.Terminal
                ),
                HelpEntry(
                    title = "GNU AGPLv3 License",
                    description = "The app is licensed under the GNU Affero General Public License v3. This ensures the app stays free forever and any improvements remain open to all.",
                    icon = Icons.Default.Gavel
                ),
                HelpEntry(
                    title = "Transparency",
                    description = "Our code is fully open for inspection. This ensures trust, security, and verifies that your data never leaves your device.",
                    icon = Icons.Default.Policy
                ),
                HelpEntry(
                    title = "Anonymous Analytics",
                    description = "We use minimal telemetry to track active user counts and popular features. No personal data is ever collected or sold — just simple event signals to help us improve the experience for everyone.",
                    icon = Icons.Default.BarChart
                )
            )
        )
    )
}