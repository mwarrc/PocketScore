# PocketScore Feature Specification

PocketScore is a professional-grade match management suite designed for high-stakes competition and casual game nights alike. While primarily optimized for Pool and Billiards, its modular scoring engine adapts effortlessly to any turn-based game, transforming scorekeeping into an immersive, analytical experience.

---

## 1. The Match Command Center (Game Screen)

The heart of PocketScore is the Game Screen—a high-fidelity, real-time interface engineered for tactile precision, rapid input, and complete match awareness.

*   **Adaptive Scoring HUD (Top Bar)**
    *   **Live Table Metrics:** Floating indicator tracking total remaining table points and remaining balls in real time.
    *   **Ball Removal Intelligence:** When an entered score matches a ball value (e.g., `+8`), the HUD triggers a **Ball Removed notification** with a miniature ball icon and updates table inventory.
    *   **Collision Detection & Alerts:** Visual warning if an entered score does not mathematically match any remaining balls on the table ("No matching ball").
    *   **Table Inventory Correction:** Tap the ball indicator anytime to manually adjust balls on or off the table.
    *   **Instant History & Undo:** Every score adjustment is logged chronologically; single-tap Undo reverts scores and restores turn order immediately.

*   **Intelligent Player Cards**
    *   **Dynamic Identity Badges:** Live standings tiers indicating **LEADER**, **TIED LEADER**, **LOSER**, and **TIED LOSER**.
    *   **Active-Turn Spotlight:** High-contrast pulsing indicator clearly marking the current player at the table.
    *   **Leader's Star Shower:** Ambient particle animation celebrating the active leader.
    *   **Mathematical Elimination Treatment:** Players out of contention receive a contrast-checked **ELIMINATED** watermark, muted background, and corner tab so trailing status is unmistakable without visual clutter.
    *   **Persistence Delta Badges:** Small badges displaying the exact amount of the last point change (e.g., `+14`, `-2`, or `Undo +8`).
    *   **Dual Viewports:** Instantly toggle between a **High-Density Grid** (ideal for multi-player games) and a **Focused List View** (optimized for head-to-head duels).

*   **Advanced Calculation & Input Utilities**
    *   **Full-Range Numpad:** Ergonomically adjustable in-app keypad with tactile haptic feedback and multi-stage input validation.
    *   **Numpad Padlock:** Lock the numpad open for ultra-fast score logging during rapid rounds.
    *   **Quick Expression Calculator:** Built-in formula tool for evaluating complex round additions (e.g., `(20 + 15) * 2`) without leaving the match.
    *   **Formula Insertion Ribbon:** One-tap insertion of any active player's score directly into a calculation.
    *   **Strict Turn Security:** Optional toggle in Settings locking input exclusively to the current turn holder to prevent accidental taps.

*   **Victory Celebrations**
    *   **Two Presentation Styles:**
        *   **Wavy Hero (Default):** Count-up winning score over drifting two-layer animated waves, presenting full final standings with rank, avatar, score gap, and soft last-place markers.
        *   **Classic:** Centred geometric trophy over an animated sine-wave backdrop.
    *   **Canvas-Drawn Trophies:** Procedural, resolution-independent vector trophies rendered natively with spring-damping physics animations.
    *   **Contextual Tie Handling:** Dedicated visual states and celebratory badges for drawn games ("Honours Even") vs solo triumphs ("CHAMPION").

---

## 2. Adaptive Home Dashboard

*   **Live Match HUD:** Prominent resume card appearing when a match is active in the background, showing live status and avatars for single-tap reentry.
*   **Rapid Seat Recruitment:** Fast-entry system for adding new players or selecting from saved roster favorites.
*   **Predictive Start-Game Bar:** Dynamic floating bar reflecting readiness as players are added.
*   **Direct Hub Navigation:** Streamlined entry points for **Records**, **Leaderboards**, **Settings**, and **Help**.

---

## 3. Behavioral Player Analytics (Archetype Engine)

PocketScore studies match patterns across rounds to uncover unique playing behaviors.

*   **18 Dynamic Player Archetypes:** Players earn distinctive personality roles based on their stats (e.g., *The Sniper* for pinpoint consistency, *The Assassin* for explosive scoring runs, *The Ninja* for dramatic late-game lead changes).
*   **Volatility Index:** Real-time standard deviation tracking measuring player consistency versus unpredictable swings.
*   **Clutch Performance:** Measures scoring effectiveness during the final 25% of the match.
*   **Lead-Time Analysis:** Tracks exactly how long and often each competitor holds the top position.

---

## 4. Global Ranks & Leaderboards

*   **Automated Tiering:** Continuous player ranking with **Gold, Silver, and Bronze badges**.
*   **Time-Filtered Dominance:** Analyze win rates and average points across **Today**, **Weekly**, **Monthly**, or **All-Time** periods.
*   **Privacy Controls:** Hide specific players from public leaderboard standings while preserving their career records.

---

## 5. Player & Roster Management

*   **Most-Played Sorting:** Intelligent sorting placing frequent competitors at the top of the selection pool.
*   **Global Renaming:** Update a player's name once to immediately propagate the change across all past match records, leaderboards, and backup snapshots.
*   **Visibility Toggles:** Hide inactive players from the Home screen or Leaderboards without deleting their history.
*   **Guest Mode:** Run non-recorded sessions that avoid populating permanent player profiles.
*   **Guest Session Override:** Toggle in the match completion dialog allowing players to retroactively save a great guest match to permanent history.

---

## 6. Financial & Settlement Utilities (Settle)

*   **Automated Match Splitting:** Dedicated settlement tab in Records to calculate financial dues based on match results.
*   **Multiple Distribution Methods:**
    *   **Losers Pay:** Fair calculation splitting table/session costs among trailing players.
    *   **Equal Split:** Divides the session total evenly among all participating competitors.
*   **Live Fee Calculation:** Configure total match fees and immediately see the per-player breakdown.

---

## 7. Match Archives & Data Portability

*   **Deep-Dive Game Insights:** Revisit completed matches to inspect scoring breakdowns, per-turn averages, and total durations.
*   **Interactive Momentum Charts:** Dynamic visual graphs detailing the momentum and lead swings across the entire match timeline.
*   **Batch Export:** Multi-select matches from the record list to export them into a unified `.pscore` file.
*   **Semantic Data Import:** Tap any `.pscore` file to open an interactive preview and import matches with intelligent, duplicate-free player roster mapping.

---

## 8. Backup Vault & Snapshot Architecture

*   **Linked Storage Folder:** Connect PocketScore to a permanent folder on the device or cloud-synced storage (e.g., Google Drive, Nextcloud) to guarantee data survivability even across app uninstalls.
*   **Automated Daily Preservation:** Silent daily snapshots saved automatically to the linked folder.
*   **Manual Snapshots:** Create named point-in-time snapshots before major tournament sessions or device transfers.
*   **Smart Restore & Merge:** Restore historical snapshots seamlessly with duplicate detection.

---

## 9. Personalization & Settings

*   **Material Design 3 & Material You:** Cohesive dynamic theming that harmonizes with Android wallpaper and system palette accents across both light and dark modes.
*   **App Font Selection:** Dedicated typography screen with a live scoring and numpad preview mock. Choose between the System font and five bundled, numeral-tailored typefaces:
    *   **Inter** (Modern neutral grotesque)
    *   **JetBrains Mono** (Monospaced, perfectly aligned scoring digits)
    *   **Montserrat** (Geometric, bold styling)
    *   **Poppins** (Clean, contemporary rounded geometry)
    *   **Rubik** (Soft, friendly numerals)
*   **Pool Scoring Presets:** Switch between standard localized formats (**6-17**, **3-17**, **Face Value**) or create custom ball value configurations.

---

## 10. Technical Foundation & Privacy

*   **Modern Android Architecture:** 100% Kotlin with Jetpack Compose, built using MVVM and Clean Architecture principles.
*   **Zero-Tracking Privacy:** Offline-first architecture where player data, game history, and financial splits remain strictly on your local device.
*   **Crash Reporting & Feedback:** Integrated Firebase Crashlytics for crash resilience and direct support ticketing powered by Firebase Firestore.
*   **Open Source:** Transparent codebase licensed under the GNU Affero General Public License v3.0 (AGPL-3.0).
