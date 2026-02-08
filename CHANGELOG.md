# Changelog

All notable changes to PocketScore will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.1.2-expressive] - 2026-02-06

### Added

#### Advanced Scoring System
- **Custom Score Numpad**: Integrated a minimal, high-performance in-app keyboard tailored for scoring.
  - Supports pinning for rapid data entry across players.
  - Gesture-based "swipe to dismiss" mechanics.
  - **Tactile Feedback**: Integrated **Haptic Feedback** (subtle ticks) on key presses, synced with app-wide settings.


#### Match Insights & Analytics
- **Interactive Momentum Chart**:
  - Toggle player visibility using interactive legend chips.
  - Dynamic vertical scaling (Auto-Zoom) based on the score range of visible players.
  - Added Y-axis labels and horizontal grid lines for professional data visualization.
  - Added a "Reset Zoom" shortcut button.
  
#### Feedback & Support (v1.1)
- **Cloud-Powered Feedback**: Complete system overhaul using **Firebase Firestore** for real-time submission tracking.
- **Extended Capacity**: Message limits increased to **10,000 characters** with a live counter and email validation.
- **Keyboard-Aware Design**: Optimized scrolling and padding for a seamless support experience.
- **Expanded Player Archetypes**:
  - Archetype library expanded from 6 to 12 unique categories (The Snake, The Closer, The Fireball, etc.).
  - Smarter, prioritized detection logic for more authentic player "personalities".
- **Leader Spotlight**: Real-time visual highlighting of the player currently in first place.

#### Utilities & Tools

- **Session Cost Calculator ("Settle")**: New utility to calculate match fees and player debts.
  - Supports "Winner Pays" and custom match cost splitting.
  - **Live Cost Badge**: Displays the active "Cost per Match" in the header even when settings are collapsed for instant clarity.
  - Integrated into the Records screen for post-game settlement.
- **Device Identity**: Set a custom device name (e.g., "Jacob's Phone") to verify the origin of shared `.pscore` files.

#### UI/UX Polishing
- **Finish & Resume flow**: New dialog options when ending a match to either "Archive" (Finalize) or "Resume Later" (Save progress).
- **Expressive Iconography**: Integrated `Smartphone`, `Snake`, `Timer`, and `Payments` icons.
- **Consistency**: Switched all legacy icons to **AutoMirrored** versions for better RTL support.
- **Better Micro-copy**: Updated "Kick off Match" to **"Start Game"** and refined player record labels for better clarity.

### Fixed

- **Cost per Match Input**: Fixed a bug where entering decimal values was difficult due to state syncing conflicts.
- **Feedback Accessibility**: Resolved an issue where the submit button was obscured by the system keyboard.
- **Code Health**: Cleaned up redundant/duplicate imports and optimized data merging logic.

---

## [0.1.1-expressive] - 2026-01-31

### Added

#### Home Screen & Player Management
- **New Home Screen**: Complete redesign replacing the old Setup screen
  - Active Roster system for quick player selection
  - Saved player pool with rename and delete capabilities
  - Visual player count indicator
  - Streamlined "Start Game" flow
- **Player Pool Management**: Persistent player storage with full CRUD operations

#### Backup & Data Sharing System
- **Local Snapshot System**: 
  - Automated daily backups with toggle control
  - Manual snapshot creation with custom naming
  - Snapshot restore with smart merge (avoids duplicates)
  - Storage usage tracking and metadata display
- **Backup Management Screen**: Minimal, elegant interface for managing local backups
  - Share snapshots as `.pscore` files
  - Delete old snapshots
  - Visual snapshot history with timestamps
- **Import/Export System**:
  - Share individual games or full history
  - Import `.pscore` files from other devices
  - Import preview screen with conflict resolution
  - Smart merge prevents duplicate games and players
- **File Provider Integration**: Secure file sharing via Android FileProvider

#### Roadmap & Developer Features
- **Roadmap Screen**: Interactive feature timeline with status indicators
  - Categorized by implementation status (Completed, In Progress, Planned)
  - Visual progress tracking
  - Clean, minimal card-based design
- **Hidden Developer Mode**: 
  - Unlock by tapping app logo 3 times in About screen
  - Toggle to show/hide upcoming features
  - Access to experimental functionality

#### About Screen Enhancements
- **Refined Information Architecture**:
  - Material You dynamic coloring highlight
  - Open Source & Tech section
  - Privacy-focused messaging
  - Developer social links (GitHub, Twitter, Instagram, Email)
- **Minimal text-based design** with reduced visual weight
- **Clickable roadmap navigation** with visual indicator

#### Game Screen Improvements
- **Game Help Sheet**: Comprehensive in-game help system
  - Strict Mode explanation
  - Scoring mechanics guide
  - Feature overview
- **Enhanced Quick Settings**:
  - Visual mode indicators
  - Improved layout and spacing
  - Better haptic feedback integration
- **Refined Player Cards**:
  - Cleaner active player card with better visual hierarchy
  - Improved passive player cards with subtle interactions
  - Enhanced score display and animations

#### History Screen Refinements
- **Streamlined UI**: Reduced visual clutter
- **Better game cards**: Improved readability and information density
- **Enhanced interactions**: Minimal ripple effects, cleaner touch feedback

### Changed

#### UI/UX Refinements
- **Settings Screen**:
  - Reorganized settings into logical groups (Gameplay, Appearance, Limits)
  - Moved "Upcoming Features" toggle to About screen (hidden)
  - Updated Strict Mode documentation and info dialog
  - Removed outdated "3-tap" disable instruction
  - Added clickable info icon for Strict Mode details
- **Material 3 Consistency**: 
  - Unified color schemes across all screens
  - Consistent border styles and elevations
  - Standardized spacing and padding
- **Typography Scale**: Reduced text sizes in About screen for cleaner look
- **Navigation**: Improved flow between screens with better back button handling

#### Technical Improvements
- **ViewModel Enhancements**:
  - Added snapshot management methods
  - Implemented data sharing logic
  - Enhanced state management for backups
- **Repository Layer**:
  - New `getSnapshotContent()` method
  - Improved `mergeShareData()` with smart deduplication
  - Enhanced backup file management (internal + public storage)
- **Domain Models**:
  - New `PocketScoreShare` data class for import/export
  - Extended `AppSettings` with backup-related fields
  - Added `showComingSoonFeatures` flag
- **AndroidManifest**: 
  - FileProvider configuration for secure file sharing
  - Updated backup rules for better data preservation

### Fixed
- **Strict Mode Logic**: Corrected enforcement behavior and in-game toggles
- **Import Conflicts**: Smart merge prevents duplicate data on import
- **UI Consistency**: Resolved various spacing and alignment issues
- **Navigation Flow**: Fixed back button behavior across screens

### Removed
- **Old Setup Screen**: Completely replaced with new Home screen
- **Setup Components**: Removed `PlayerNameRow`, `SetupGhostHeader`, `SetupValidationBanner`
- **HelpDialog**: Replaced with new `GameHelpSheet`
- **Outdated References**: Removed OLED dark mode mentions (not implemented)

---

## [0.1.0] - 2026-01-31

### Added
- Initial release
- Core game management functionality
- Score tracking and history
- Material 3 design system
- Dark/Light theme support
- Strict turn mode
- Local data persistence with DataStore

---

## Release Statistics

**Version 0.1.1-expressive**:

