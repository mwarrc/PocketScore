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
                    description = "Long-press a record to enter selection mode. Select multiple games and export them as a single .pscore file for sharing or backup. You can also export all records at once in settings in the Backup & Sharing section.",
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
            title = "In-Game Scoreboard",
            description = "Track scores, manage turns, and utilize smart tools.",
            icon = Icons.Default.SportsEsports,
            entries = listOf(
                HelpEntry(
                    title = "Rapid Score Entry",
                    description = "Use the built-in custom numpad to easily add points. You can pin the numpad to the screen to quickly log scores during fast-paced rounds.",
                    icon = Icons.Default.Keyboard
                ),
                HelpEntry(
                    title = "Winner & Celebration",
                    description = "The app automatically computes and triggers a winner celebration when the pool table is cleared or if a comeback is mathematically impossible.",
                    icon = Icons.Default.EmojiEvents
                ),
                HelpEntry(
                    title = "Strict Turn Security",
                    description = "Enable this in settings to lock inputs to only the current active player. It prevents accidental taps on other players and ensures honest play.",
                    icon = Icons.Default.Lock
                ),
                HelpEntry(
                    title = "Leader Spotlight",
                    description = "The scoreboard visually highlights the current leader with a gold glow and the 'LEADER' badge. It updates dynamically and tracks ties.",
                    icon = Icons.Default.Star
                ),
                HelpEntry(
                    title = "Smart Calculator",
                    description = "Open the Quick Math tool from the bottom bar to safely calculate complex additions (e.g., '14+2+7') and directly apply the total.",
                    icon = Icons.Default.Calculate
                ),
                HelpEntry(
                    title = "History & Quick Undo",
                    description = "All score changes are logged chronologically. If a mistake happens, tapping Undo instantly reverts the points and corrects the turn order.",
                    icon = Icons.AutoMirrored.Filled.Undo
                )
            )
        ),
        HelpCategory(
            title = "Pool & Billiard Mechanics",
            description = "Master table features, auto-removing balls, and points systems.",
            icon = Icons.Default.Adjust,
            entries = listOf(
                HelpEntry(
                    title = "Points Systems (6-17, Face Value)",
                    description = "The app supports various localized scoring systems like 6-17, 3-17, and standard Face Value. Head over to Settings > Pool Values to swap presets or create custom point values to match your local house rules.",
                    icon = Icons.Default.SettingsApplications
                ),
                HelpEntry(
                    title = "Auto-Remove Balls",
                    description = "When playing a pool match, if you score points that perfectly match the value of a ball resting on the table, the app will automatically remove it. This completely streamlines your focus allowing you to just play.",
                    icon = Icons.Default.AutoAwesome
                ),
                HelpEntry(
                    title = "Mathematical Win Probabilities",
                    description = "Using the remaining balls on the table, the app mathematically calculates winning probabilities. Open the Pool Probability tab to see if a comeback is still physically possible.",
                    icon = Icons.Default.QueryStats
                ),
                HelpEntry(
                    title = "Last Man Standing / Eliminations",
                    description = "Because the app knows the remaining total value on the table, it can automatically declare a winner early if the trailing players simply don't have enough balls left to catch up.",
                    icon = Icons.Default.DoneAll
                ),
                HelpEntry(
                    title = "Quick Ball Correction",
                    description = "Need to make a manual adjustment? Just tap the 'Balls Remaining' indicator at the top of your screen to instantly add or remove specific balls from the table inventory.",
                    icon = Icons.Default.Toll
                )
            )
        ),
        HelpCategory(
            title = "The players (Roster)",
            description = "Manage your players and global visibility.",
            icon = Icons.Default.Group,
            entries = listOf(
                HelpEntry(
                    title = "Manage People",
                    description = "In the 'Players' tab, long-press players to hide them from 'Ranks' (stats preserved) or 'Home' (keeps your active roster clean).",
                    icon = Icons.Default.VisibilityOff
                ),
                HelpEntry(
                    title = "Global Renaming",
                    description = "Renaming a player in the 'Players' tab updates their name across every record in your full match history.",
                    icon = Icons.Default.Edit
                ),
                HelpEntry(
                    title = "Guest Sessions",
                    description = "Enable 'Guest Mode' on Home to play rounds that won't save to history or create permanent player entries.",
                    icon = Icons.Default.PersonSearch
                ),
                HelpEntry(
                    title = "Override Guest Session",
                    description = "Played a great game in Guest Mode and want to keep it? In the end-game dialog, simply toggle the override switch to force-save the match and persist the players, guaranteeing your epic win is recorded even if you hit 'Play Again'!",
                    icon = Icons.Default.Save
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
                    title = "Importing Players & Games (.pscore)",
                    description = "Received a .pscore file from a friend (via WhatsApp, Telegram, or email)? Simply tap the file! The app will open an Import Preview showing you exactly what matches and players are inside. It intelligently merges the data, ensuring duplicate player names link up perfectly without overwriting or duplicating your existing history or roster.",
                    icon = Icons.Default.Download
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