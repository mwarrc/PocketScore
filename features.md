# PocketScore Feature Specification

PocketScore is a professional-grade match management suite designed for high-stakes competition. While primarily optimized for Pool and Billiards, its core logic is versatile enough for any turn-based game, transforming simple score-keeping into a deep, immersive analytical experience.

##  The Definitive Game Screen (Match Command Center)

The core of the PocketScore experience is the Game Screen—a high-fidelity, real-time interface designed for maximum accuracy and visual impact.

*   **Adaptive Scoring HUD (Top Bar)**
    *   **Live Table Metrics:** A floating, pill-shaped status indicator that tracks total points remaining in the pool session.
    *   **Ball Removal Intelligence:** When a score is entered that matches a ball value (e.g., +8), the HUD displays a **"Ball Removed" notification** accompanied by a realistic miniature ball icon.
    *   **Collision Feedback:** Visual alerts if an entered score does not mathematically match any remaining balls on the table ("No matching ball").
    *   **Tactile Session Controls:** High-visibility "End Game" and "Undo" actions with clear state feedback.

*   **Intelligent Player Cards (Active & Passive)**
    *   **Dynamic Identity Tiers:** Players are assigned interactive badges based on live standings: **LEADER**, **TIED LEADER**, **LOSER**, and **TIED LOSER**.
    *   **Active-Turn Spotlighting:** A high-contrast "PLAYING NOW" badge and primary-color container that pulses to indicate the current turn holder.
    *   **The Leader's "Star Shower":** A background animation of rotating stars activates for the current leader, providing a premium reward for dominant play.
    *   **Elimination Graphics:** Players mathematically out of contention receive an **"ELIMINATED" watermark** and a large 3D "Block" icon overlay to prevent scoring errors.
    *   **Persistence Delta Badges:** Small historical badges that display the exact amount of the **last points change** (e.g., "+14", "-2", or "Undo +8").
    *   **Layout Fluidity:** Seamlessly switch between a **High-Density Grid** for large rosters and a **Focused List View** for 1v1 sessions.

*   **Advanced Calculation Utilities**
    *   **The Full-Range Numpad:** A custom-built numeric interface with haptic feedback, ergonomically adjustable height, and multi-stage input validation.
    *   **The Quick Expression Calculator:** A dedicated formula overlay for calculating complex score additions (e.g., `(20 + 15) * 2`) without leaving the match.
    *   **Formula Insertion Ribbon:** A "Quick Ribbon" allows you to insert any active player's current score into a math formula with one tap.
    *   **Hybrid Keyboard Logic:** Dynamically switch between the custom numpad and the system keyboard inside the calculator as needed.

*   **Elite Match Conclusion (Celebration Overlay)**
    *   **Procedural Sine-Wave Evolution:** A mathematical simulation of three layered, staggered sine waves that animate across the background during victory.
    *   **Geometric Trophy Marks:** Asset-free, high-fidelity trophy marks drawn entirely in Canvas for a crisp, resolution-independent look.
    *   **Physics-Based Motion:** Entrance animations utilizing spring-based damping ratios (0.55f) to ensure a premium, tactile feel.
    *   **Contextual Tie-Handling:** Unique visual states for ties ("Honours Even") vs soul victories ("CHAMPION").

##  Adaptive Home Dashboard

*   **Live Match HUD**
    *   **One-Tap Resumption:** A prominent card appears when a session is in the background, showing **"LIVE" status** and player avatars for immediate reentry.
*   **Modern Player Recruitment**
    *   **Alphanumeric Seat Selection:** Fast-entry system for adding new players or selecting from your saved roster.
    *   **Predictive Start-Game Bar:** A floating HUD that updates as you add players, indicating when the match is ready for kickoff.
*   **Premium Navigation Cards**
    *   High-impact entry points for **Records**, **Leaderboards**, and **Help** using DM Serif Display typography.

##  Behavioral Player Analytics (Archetype Engine)

*   **Dynamic Identity Assignment**
    *   **18 Unique Archetypes:** Players receive roles like **The Sniper** (precision), **The Assassin** (explosive turns), or **The Ninja** (late-game lead changes) after every match.
*   **Deep Performance Metrics**
    *   **Volatility Index:** Real-time calculation of scoring stability (Standard Deviation).
    *   **Clutch Scoring Percentage:** Tracking contribution during the final 25% of the match.
    *   **Lead-Time Analysis:** Monitoring exactly how long a player held the top position.

##  Global Ranks & Leaderboards

*   **Automatic Tiering:** Continuous ranking with **Gold, Silver, and Bronze badges**.
*   **Time-Filtered Performance:** View dominance across **Today**, **Weekly**, **Monthly**, or **All-Time** history.
*   **Privacy Guarding:** Hide specific regulars from the public leaderboard while preserving their data.

##  Advanced Player & Roster Management

*   **The "Most Played" Algorithm:** Smart sorting that pulls your frequent competitors to the top.
*   **Global Renaming Utility:** Update a player's name once and propagate it across all historical records and snapshots instantly.
*   **Deactivation:** Hide players from the active roster without losing their career stats.

##  Premium Match Archives

*   **Sharing & Data Portability**
    *   **Proprietary `.pscore` Exports:** Full integrity sharing with other PocketScore users.
    *   **Semantic Data Import:** Intelligent **Player Mapping** that merges shared data into your local roster without duplicates.
    *   **Momentum Charts:** Interactive line graphs showing the pulse and "momentum" of previous matches.

##  Advanced Settings & Personalization

*   **Appearance & UX Tuning**
    *   **Material You Synergy:** System-wide themes that adapt to your device's wallpaper and accents.
*   **Snapshot Architecture**
    *   **Manual Restoration Points:** Create named snapshots of your entire database.
    *   **Automated Daily Preservation:** Silent background backups ensuring zero data loss.

##  Technical Foundation

*   **Modern Android Stack:** 100% Kotlin & Jetpack Compose for fluid animations.
*   **MVVM Clean Architecture:** Robust, scalable, and battle-tested code design.
*   **Zero-Tracking Privacy:** No external analytics, no cloud dependency, and local encrypted storage.
