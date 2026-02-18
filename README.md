# PocketScore

PocketScore is a modern, expressive, and user-friendly score-keeping application for Android. Built with Kotlin and Jetpack Compose, it offers a seamless experience for managing game nights, tracking scores, and celebrating victories.

<div align="center">
  <br />
  <a href="https://github.com/mwarrc/PocketScore/releases/download/v1.0.0-expressive/PocketScore_v1_expressive.apk">
    <img src="https://img.shields.io/badge/Download-Latest%20APK-brightgreen?style=for-the-badge&logo=android" alt="Download Latest APK" />
  </a>
  <br />
  <br />
</div>

## Performance and Design

PocketScore leverage Material 3 design principles to provide a premium, cohesive user interface. The application is built with an offline-first architecture, ensuring data privacy and consistent performance without the need for an internet connection.

## Showcase

View screenshots at [screenshots](https://mwarrc.github.io/pscore/screenshots).

## Key Capabilities

*   **Professional Scoring**: High-performance in-app keyboard with tactile haptic feedback and gesture-based interactions.
*   **Adaptive Scoreboard**: Support for Grid and List layouts with real-time leader and loser spotlighting.
*   **Strategic Insights**: Pool probability calculations and performance-based player archetypes.
*   **Data Integrity**: Automated local snapshots, manual backup management, and secure .pscore file portability.
*   **Financial Utilities**: Integrated match fee calculator with multiple settlement methods and live cost tracking.
*   **Deep Customization**: Material You dynamic coloring, adjustable scoring rules, and customizable ball value presets.

For a detailed breakdown of all application capabilities, please refer to [features.md](features.md).

## Technical Specifications

*   **Language**: Kotlin
*   **UI Framework**: Jetpack Compose (Material 3)
*   **Navigation**: Compose Navigation
*   **Architecture**: MVVM with Clean Architecture principles
*   **State Management**: ViewModel and StateFlow
*   **Persistence**: DataStore and Kotlin Serialization
*   **Integrations**: Firebase Firestore (Support), Firebase Analytics

## Project Structure

The project follows a feature-first modular structure:

```
com.mwarrc.pocketscore
├── core/           # Core utilities and extensions
├── data/           # Repositories and data sources
├── domain/         # Domain models (Game, Player, History)
└── ui/             # UI Layer
    ├── components/ # Reusable Compose components
    ├── feature/    # Feature-specific screens
    ├── theme/      # App theme and styling
    └── viewmodel/  # ViewModels
```

## Getting Started

### Prerequisites

*   Android Studio (Latest Stable Release)
*   JDK 17 or newer

### Installation

1.  **Clone the repository**
    ```bash
    git clone https://github.com/mwarrc/PocketScore.git
    cd PocketScore
    ```

2.  **Open in Android Studio**
    *   Initialize Android Studio.
    *   Select "Open" and navigate to the cloned directory.

3.  **Build and Run**
    *   Synchronize Gradle configuration.
    *   Execute the Run command (Shift + F10) on a connected device or emulator.

## Contribution Guidelines

Contributions are welcome. Please adhere to the following workflow:

1.  Fork the repository.
2.  Create a feature branch.
3.  Commit changes with descriptive messages.
4.  Push to your branch.
5.  Submit a Pull Request for review.

## Licensing

PocketScore is open-source software licensed under the [GNU Affero General Public License v3.0 (AGPL-3.0)](LICENSE). 

---

Built by [mwarrc](https://github.com/mwarrc)

