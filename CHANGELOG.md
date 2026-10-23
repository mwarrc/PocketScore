# Changelog

All notable changes to PocketScore are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/), and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.0.1] - 2026-10-22

### Initial Release

Official first release of PocketScore - a polished, professional-grade match management and scoring suite built with Kotlin, Jetpack Compose, and Material Design 3.

### Added

#### Core Scoring Experience & Game Command Center
- **Adaptive Scoreboard HUD**: Real-time scoring interface with dynamic table metrics, remaining point tracking, and instant Undo.
- **Ball Auto-Removal Intelligence**: Automatically detects sunk balls and removes them from the table inventory when points match active balls, complete with visual notifications and ball icon alerts.
- **Intelligent Player Cards**:
  - Dynamic standings badges: **LEADER**, **TIED LEADER**, **LOSER**, and **TIED LOSER**.
  - Animated leader reward with star shower particles.
  - Active-turn spotlight with pulsing contrast indicators.
  - High-visibility **ELIMINATED** status with contrast-checked watermark and dedicated corner badges when players are mathematically eliminated from contention.
  - Persistence delta badges showing the exact points change from the previous action.
- **Dual Display Modes**: Seamless toggling between **High-Density Grid** (ideal for large groups) and **Focused List View** (optimized for 1v1 play).
- **Custom Numeric Keyboard**: Ergonomically tuned in-app numpad with tactile haptic feedback, lockable open state, and input validation.
- **Quick Expression Calculator**: In-game math formula ribbon supporting complex expressions (e.g., `(14 + 7) * 2`) and single-tap score insertion from any active player.
- **Strict Turn Enforcement**: Optional strict mode locking score inputs exclusively to the active turn holder to ensure honest play.

#### Match Conclusion & Celebrations
- **Two Presentation Styles**:
  - **Wavy Hero (Default)**: Dynamic count-up hero over drifting two-layer animated waves with full ranked standings, gap-to-winner indicators, and low-emphasis last place formatting.
  - **Classic**: Centred trophy presentation over animated sine waves.
- **Canvas-Rendered Vector Trophies**: High-fidelity, resolution-independent procedural trophies with physics-based spring entrance motion.
- **Contextual Tie Handling**: Distinct victory states for solo champions versus drawn matches ("Honours Even").

#### Tactical Insights & Behavioral Analytics
- **Mathematical Win Probabilities**: Real-time calculations determining comeback feasibility and last-man-standing eliminations based on remaining ball values.
- **Behavioral Archetype Engine**: Automatically analyzes player performance data across matches to assign one of **18 unique player archetypes** (e.g., *The Sniper*, *The Assassin*, *The Ninja*, *The Strategist*).
- **In-Depth Match Analytics**: Live and historical momentum curves, scoring volatility index (standard deviation), clutch scoring ratios, and lead-time distribution.

#### Records, Leaderboards & Settlement
- **Match Archives**: Chronological history tracking every turn, duration, and score shift, with interactive momentum charts.
- **Global Leaderboards**: Automated Gold/Silver/Bronze tiering and winrate rankings across **Today**, **Weekly**, **Monthly**, and **All-Time** windows.
- **Settle (Match Cost Splitter)**: Built-in payment distribution utility calculating fee breakdowns using multiple settlement rules (Losers Pay or Equal Split).
- **Batch Export**: Selection mode in match history allowing bulk export of records into a single `.pscore` file.

#### Roster & Player Management
- **Most Played Sorting**: Smart algorithm surfacing frequent competitors to the top of the roster.
- **Global Renaming**: Rename players once and automatically propagate the change across all past match records and saved snapshots.
- **Privacy & Visibility**: Hide specific players from public leaderboards or the active Home screen while preserving career statistics.
- **Guest Sessions**: Dedicated guest mode for quick unranked games, featuring an end-game override toggle to permanently save memorable matches.

#### Backup Vault & Data Portability
- **Linked Backup Folder**: Direct integration with device or cloud-synced folders to retain backups across uninstalls and device migrations.
- **Automated Daily Snapshots**: Scheduled background backups preserving rolling snapshots of games and players.
- **Manual Restoration Points**: On-demand point-in-time snapshots with smart duplicate-free merge capabilities.
- **Proprietary `.pscore` Exchange**: Full match export and import preview with intelligent player-mapping.

#### Personalization & Design System
- **Material Design 3 & Material You**: Dynamic color system adapting to user wallpapers and system palettes with refined light and dark themes.
- **App Font Selection**: Dedicated typography picker with live gameplay score and numpad preview, supporting the system font plus five bundled numeral-focused typefaces (**Inter**, **JetBrains Mono**, **Montserrat**, **Poppins**, **Rubik**).
- **Customizable Pool Rules**: Support for 6-17, 3-17, and standard Face Value presets, plus custom point value editors for house rules.
