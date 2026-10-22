# PocketScore v0.0.1 — Initial Official Release

Welcome to the official launch of **PocketScore**! 

PocketScore is a professional-grade match management and score-keeping suite engineered for high-stakes competition and casual game nights. Designed with Google's modern Material Design 3 system and built 100% with Kotlin and Jetpack Compose, it brings tactile precision, deep tactical analytics, and a premium visual experience to Android.

---

## What's Inside v0.0.1

### Match Command Center & Scoring HUD
- **Custom Ergonomic Numpad**: Tactile in-app numeric keyboard with haptic feedback, adjustable height, and lockable padlock mode for rapid scoring rounds.
- **Table Ball Intelligence**: Sunk balls are automatically tracked and removed from the active table inventory when scores match ball values.
- **Live Role Spotlights**: Real-time identification of **LEADER**, **TIED LEADER**, **LOSER**, and **TIED LOSER**.
- **Contention & Elimination Graphics**: Clear contrast-checked **ELIMINATED** status with dedicated corner tabs and watermarks for mathematically eliminated players.
- **Dual Scoreboard Views**: Instantly switch between **High-Density Grid** (multiplayer) and **Focused List** (1v1 duels).
- **In-Game Expression Calculator**: Built-in math formula ribbon with one-tap insertion of any active player's score.
- **Chronological Undo**: Mistakes are instantly corrected with single-tap undo and turn restoration.

### Celebration & Winner Presentations
- **Wavy Hero (Default)**: Modern count-up celebration over animated drifting sine waves with complete final standings, score gaps, and soft last-place markers.
- **Classic Presentation**: Clean, centered geometric trophy presentation over animated backdrops.
- **Canvas Vector Trophies**: Asset-free, crisp resolution-independent trophies with physics-based spring animations.

### Tactical Insights & Behavioral Analytics
- **18 Dynamic Player Archetypes**: Automated post-match analysis classifying players into unique roles (*The Sniper*, *The Assassin*, *The Ninja*, *The Strategist*, etc.).
- **Pool Probability Engine**: Real-time mathematical calculation determining comeback feasibility based on remaining table points.
- **Match Momentum**: Detailed analytics including standard deviation (volatility), clutch ratio in final rounds, and lead-time analysis.

### Match History, Records & Financial Settlement
- **Settle (Match Cost Splitter)**: Built-in payment distribution utility calculating fee breakdowns using multiple settlement rules (*Losers Pay* or *Equal Split*).
- **Global Leaderboards**: Automated Gold/Silver/Bronze badges with filters across Today, Weekly, Monthly, and All-Time.
- **Batch Export**: Long-press and export multiple matches into a single `.pscore` file.

### Backup Vault & Data Portability
- **Linked Backup Folder**: Connect directly to a device or cloud storage folder (Google Drive, Nextcloud) to preserve all data across app reinstalls.
- **Rolling Daily Snapshots**: Automatic daily backups plus manual snapshot checkpoints with smart duplicate-free merge.
- **Semantic Import**: Tap `.pscore` files shared via WhatsApp, Telegram, or email to preview and import records with duplicate-free player roster mapping.

### Personalization & Appearance
- **Material You Dynamic Theming**: System-wide colors adapting to wallpaper and accent palettes with refined dark/light modes.
- **App Font Selection**: Dedicated typography screen with a live scoring preview mock, featuring the system default plus five bundled numeral-first fonts: **Inter**, **JetBrains Mono**, **Montserrat**, **Poppins**, and **Rubik**.
- **Rule Presets**: Localized scoring support for 6-17, 3-17, and standard Face Value, plus custom point value editors.

---

## 🔒 Privacy & Architecture
- **Offline-First**: Zero external trackers, zero advertising, and no cloud dependency for core features. All player profiles and match logs stay strictly on your local device.
- **Open Source**: Transparent codebase licensed under the [GNU Affero General Public License v3.0 (AGPL-3.0)](LICENSE).
