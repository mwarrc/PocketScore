# PocketScore Feature Specification

PocketScore is a professional-grade score-keeping application designed for high-performance match management, primarily optimized for pool but versatile enough for various competitive games.

## Core Scoring System

*   **Custom Score Numpad**
    *   Optimized in-app keyboard tailored for rapid numeric entry.
    *   Tactile haptic feedback integration for precise interaction.
    *   Gesture-based dismissal and pinning functionality for streamlined multi-player scoring.
    *   Configurable keyboard height (Compact, Medium, Large) and text size.

*   **Dynamic Scoreboard**
    *   Adaptive layout supporting both Grid and List views.
    *   Active/Passive player differentiation for clear focus on current turns.
    *   Real-time leader and loser spotlighting for competitive clarity.
    *   Automatic scrolling to the current active player.

*   **Turn Management**
    *   Turn-based progression with auto-next functionality.
    *   Strict Mode enforcement for official match rules.
    *   In-game help system for rule verification during active matches.

## Advanced Match Insights

*   **Pool Probability Calculator**
    *   Real-time calculation of ball-count probabilities.
    *   Dynamic odds visualization for strategic decision-making.
    *   Support for multiple game formats through customizable ball values.

*   **Interactive Analytics**
    *   Visual representation of game progress and scoring trends.
    *   Complete game history dialog accessible during active matches.
    *   Real-time scoreboard updates with expressive animations.

*   **Player Archetypes**
    *   Intelligent player categorization based on performance data.
    *   12 unique archetypes including "The Snake", "The Closer", and "The Fireball".
    *   Prioritized detection logic for authentic personality assignment.

## Data Management & Privacy

*   **Local Snapshot System**
    *   Automated daily background backups for data preservation.
    *   Manual on-demand snapshots with custom labeling.
    *   Smart merge technology to prevent duplicate records during restoration.
    *   Visual storage tracking and metadata display.

*   **Secure Data Portability**
    *   Import and export functionality via proprietary `.pscore` files.
    *   Conflict resolution during import to ensure data integrity.
    *   Ability to share individual games or complete historical archives.
    *   Custom device identity tagging to verify the origin of shared files.

*   **Privacy-First Architecture**
    *   Complete offline-first functionality ensuring zero data collection.
    *   Ad-free experience with no tracking or external dependencies.
    *   Secure file sharing using Android FileProvider protocols.

## Game Rules & Customization

*   **Flexible Setup**
    *   Active Roster management for quick selection from a saved player pool.
    *   Support for up to 32 players per session.
    *   Customizable player order with options for manual, alphabetical, random, or performance-based sorting.

*   **Rule Configuration**
    *   Adjustable scoring limits and bounds.
    *   Customizable ball values for various pool variants (Standard, Face Value, Classic).
    *   Preset management for rapid rule switching.

*   **Visual Personalization**
    *   Material You dynamic coloring for system-wide aesthetic harmony.
    *   Dedicated Light and Dark mode support.
    *   Customizable scoreboard layouts tailored to device screen size.

## Mathematical Utilities

*   **Session Cost Calculator ("Settle")**
    *   Integrated match fee management for sessions involving currency.
    *   Support for various settlement methods:
        *   Losers Pay (individual or multiple).
        *   All Split (equal distribution).
        *   Custom match cost splitting.
    *   Live cost indicators and post-game debt tracking.

*   **Quick Calculator**
    *   In-app expression evaluator for complex board sums.
    *   Integrated into the game interface to avoid task-switching.

## History & Analytics

*   **Comprehensive Records**
    *   Detailed archive of all past matches with full scoring breakdowns.
    *   Advanced leaderboard tracking player performance over time.
    *   Friends management system for recurring competitors.

*   **Session Management**
    *   Ability to pause and resume games across app sessions.
    *   Archive versus Resume flow for managing multiple active sessions.

## Support & Feedback

*   **Cloud-Integrated Support**
    *   Real-time feedback submission powered by Firebase.
    *   Support for detailed reporting with message limits up to 10,000 characters.
    *   Live validation and status tracking.

*   **Onboarding & Help**
    *   Comprehensive Game Help Sheet for new users.
    *   Contextual banners for critical features like Strict Mode.
    *   Interactive roadmap for platform development transparency.

## Technical Excellence

*   **Modern Android Stack**
    *   Built entirely with Kotlin and Jetpack Compose.
    *   MVVM Clean Architecture for robustness and scalability.
    *   Performance-optimized local storage using DataStore and Kotlin Serialization.
    *   Haptic Feedback integration for premium user experience.
