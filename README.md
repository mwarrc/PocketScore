# PocketScore

PocketScore is a modern, expressive, and user-friendly score-keeping application for Android. Built with **Kotlin** and **Jetpack Compose**, it offers a seamless experience for managing game nights, tracking scores, and celebrating victories.

---

### 📥 [**https://github.com/mwarrc/PocketScore/releases/download/v0.1.2/PocketScore.v0.1.2-expressive.apk)
*Quick and direct installation for Android.*

---


![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-4285F4?style=for-the-badge&logo=android&logoColor=white)
![Material 3](https://img.shields.io/badge/Material%203-757575?style=for-the-badge&logo=materialdesign&logoColor=white)

## Screenshots

📸 **View our full high-resolution gallery and feature showcase at [pscore.netlify.app/screenshots](https://pscore.netlify.app/screenshots)**

---




## Features

*   **Home Screen & Player Pool**: Quick access to saved players and streamlined game setup
*   **Custom Score Numpad**: High-performance, in-app keyboard with pinning and gesture-based dismissal
*   **Rich Match Insights**: Interactive momentum charts, 12 unique player archetypes, and real-time leader spotlighting
*   **Mathematical Utilities**: Built-in expression calculator for quick board sums and session cost split-tracking
*   **Backup & Data Sharing**: Local snapshots, import/export games, and share with friends via `.pscore` files
*   **History Tracking**: Complete game archive with detailed statistics and settle-up fee calculations
*   **Expressive UI**: Material You dynamic coloring, fluid animations, and confetti celebrations
*   **Strict Mode**: Enforced turn-based rules with in-game help system
*   **Privacy First**: Zero tracking, zero ads, 100% offline-first architecture
*   **Open Source**: Fully transparent codebase built with modern Android best practices

> 📋 See [CHANGELOG.md](CHANGELOG.md) for detailed version history and recent updates.

## Tech Stack

*   **Language**: [Kotlin](https://kotlinlang.org/)
*   **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose) (Material 3 Design System)
*   **Navigation**: [Compose Navigation](https://developer.android.com/guide/navigation/navigation-compose)
*   **Architecture**: MVVM (Model-View-ViewModel) with Clean Architecture principles (Core, Data, Domain, UI layers).
*   **State Management**: ViewModel & StateFlow.
*   **Persistence**: [DataStore](https://developer.android.com/topic/libraries/architecture/datastore) & [Kotlin Serialization](https://github.com/Kotlin/kotlinx.serialization).

## Project Structure

The project follows a feature-first modular structure:

```
com.mwarrc.pocketscore
├── core/           # Core utilities and extensions
├── data/           # Repositories and data sources
├── domain/         # Domain models (Game, Player, History)
└── ui/             # UI Layer
    ├── components/ # Reusable Compose components (Confetti, Cards)
    ├── feature/    # Feature-specific screens (Game, History, Settings)
    ├── theme/      # App theme and styling
    └── viewmodel/  # ViewModels
```

## Getting Started

### Prerequisites

*   Android Studio (Latest 2026 Stable Release).
*   JDK 17 or newer.

### Installation

1.  **Clone the repository**:
    ```bash
    git clone https://github.com/mwarrc/PocketScore.git
    cd PocketScore
    ```

2.  **Open in Android Studio**:
    *   Open Android Studio.
    *   Select "Open an existing Android Studio project" and navigate to the cloned directory.

3.  **Build and Run**:
    *   Wait for Gradle sync to complete.
    *   Select a connected device or emulator.
    *   Click the **Run** button (green arrow) or press `Shift + F10`.

## Contributing

Contributions are welcome! If you have suggestions or want to report a bug, please open an issue or submit a pull request.

1.  Fork the Project
2.  Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3.  Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4.  Push to the Branch (`git push origin feature/AmazingFeature`)
5.  Open a Pull Request

---

## Connect

[![Twitter](https://img.shields.io/badge/Twitter-1DA1F2?style=for-the-badge&logo=twitter&logoColor=white)](https://twitter.com/mwarrc)
[![Instagram](https://img.shields.io/badge/Instagram-E4405F?style=for-the-badge&logo=instagram&logoColor=white)](https://instagram.com/mwarrc)
[![GitHub](https://img.shields.io/badge/GitHub-100000?style=for-the-badge&logo=github&logoColor=white)](https://github.com/mwarrc)

---

Built with ❤️ by [mwarrc](https://github.com/mwarrc)
