# PocketScore Analytics Redesign - Production Blueprint

> **Document type:** Implementation guide (no code edits - action plan only)
> **Scope:** Replace the current privacy-over-utility analytics with a production-grade system that answers real business questions.

---

## 1. Audit of the Current System - Why It's Useless

### What exists today

| Layer | File | What it does | Problem |
|---|---|---|---|
| **Firebase Analytics** | [`AnalyticsManager.kt`](file:///c:/Users/clint/Main-Dev/Projects/app/src/main/java/com/mwarrc/pocketscore/util/AnalyticsManager.kt) | Fires generic `APP_OPEN`, `LEVEL_START`, `LEVEL_END` events | These map to Firebase's built-in "Level" concept - your data drowns in auto-collected noise. `LEVEL_START` tells you nothing about a *pool game*. |
| **Firestore Cloud** | [`CloudAnalyticsManager.kt`](file:///c:/Users/clint/Main-Dev/Projects/app/src/main/java/com/mwarrc/pocketscore/util/CloudAnalyticsManager.kt) | Writes raw docs to 4 Firestore collections (`analytics_installs`, `analytics_daily_activity`, `analytics_matches`, `analytics_events`) | Raw un-aggregated documents. No indexes. No rollups. Firestore costs scale linearly per write. Impossible to query "games per day" without reading every document. |
| **ViewModel calls** | [`GameViewModel.kt`](file:///c:/Users/clint/Main-Dev/Projects/app/src/main/java/com/mwarrc/pocketscore/ui/viewmodel/GameViewModel.kt) | Calls `logGameStarted`, `logGameEnded`, `logAppOpen` | `analyticsId` is **nullable everywhere** - if settings haven't loaded yet, the event fires with `null` and the Cloud layer silently drops it. |

### Critical gaps

- ❌ **No screen tracking** - you can't see which screens users visit or how long they stay
- ❌ **No funnel tracking** - you have no idea where users drop off (onboarding → add players → start game → finish game)
- ❌ **No retention metrics** - Day 1 / Day 7 / Day 30 retention is invisible
- ❌ **No feature adoption** - you don't know if anyone uses pool ball management, match splitting, strict mode, custom keyboards, themes, etc.
- ❌ **No session duration** - `APP_OPEN` fires but you never log `APP_CLOSE` or `APP_BACKGROUND`
- ❌ **No game completion rate** - you log starts and ends separately with no correlation ID
- ❌ **No "people per device" concept** - `analyticsId` is per-install, and `playerCount` in match events isn't aggregated
- ❌ **No error context** - crashes happen with zero analytics breadcrumbs
- ❌ **No settings snapshot** - you don't know what configuration the average user runs

---

## 2. The Metrics You Actually Need - KPI Framework

### Tier 1: North Star Metrics (check daily)

| Metric | Definition | Why it matters |
|---|---|---|
| **Daily Active Devices (DAD)** | Unique `deviceId`s that opened the app today | Your real user count |
| **Games Played / Day** | Count of `game_completed` events per calendar day | Core engagement signal |
| **Avg Players per Game** | Mean `playerCount` across all `game_completed` events | Tells you if this is a solo scorer or a group activity |
| **Avg Players per Device / Day** | Distinct player names used on a device in a day | Reveals the "shared device in a pool hall" pattern |
| **Game Completion Rate** | `game_completed / game_started` | If people start but don't finish, something is broken |

### Tier 2: Engagement Metrics (check weekly)

| Metric | Definition |
|---|---|
| **Session Duration** | Time between `session_start` and `session_end` |
| **Sessions per DAD** | How many times a device opens the app per day |
| **Games per Session** | Games started within one session |
| **Avg Game Duration** | `endTime - startTime` for completed games |
| **Avg Turns per Game** | Mean scoring events per completed game |
| **Peak Usage Hours** | Hour-of-day distribution of `game_started` events |
| **Day-of-Week Distribution** | Which days see the most games |

### Tier 3: Retention & Growth (check monthly)

| Metric | Definition |
|---|---|
| **D1 / D7 / D30 Retention** | % of devices that return 1 / 7 / 30 days after first open |
| **Weekly Active Devices (WAD)** | Unique devices in a 7-day window |
| **Monthly Active Devices (MAD)** | Unique devices in a 30-day window |
| **Stickiness (DAD/MAD)** | How often monthly users come back daily |
| **New vs Returning** | First-open devices vs repeat devices per day |
| **Churn Rate** | Devices active in previous 30 days but not current 30 days |

### Tier 4: Feature Adoption (check monthly)

| Metric | Definition |
|---|---|
| **Pool Ball Management Usage** | % of games with `poolBallManagementEnabled = true` |
| **Match Split Usage** | % of games where settlement screen was viewed |
| **Strict Mode Usage** | % of games with `strictTurnMode = true` |
| **Custom Keyboard Usage** | % of devices with `useCustomKeyboard = true` |
| **Theme Distribution** | Breakdown of `appTheme` (Light / Dark / System) |
| **Font Distribution** | Breakdown of `appFont` across devices |
| **Onboarding Completion** | % of new installs that complete onboarding |
| **History Screen Visits** | How often users check past games |
| **Share Feature Usage** | How often users share game results |
| **Guest Mode Usage** | % of sessions in guest mode |

---

## 3. Event Taxonomy - The Complete Event Catalog

Replace the current ad-hoc events with this strict taxonomy. Every event follows the format `category_action`.

### 3.1 App Lifecycle Events

```
┌────────────┐
│ Event Name              │ Parameters                            │
├─────────┼──┤
│ app_first_open          │ device_id, device_model,              │
│                         │ android_version, app_version,         │
│                         │ install_source                        │
├─────────┼──┤
│ app_session_start       │ device_id, app_version,               │
│                         │ session_id, is_returning_user,        │
│                         │ days_since_first_open,                │
│                         │ days_since_last_session               │
├─────────┼──┤
│ app_session_end         │ device_id, session_id,                │
│                         │ session_duration_sec,                 │
│                         │ games_played_in_session,              │
│                         │ screens_visited (count),              │
│                         │ end_reason (background/closed/crash)  │
├─────────┼──┤
│ app_update              │ device_id, previous_version,          │
│                         │ new_version                           │
└─────────┴──┘
```

### 3.2 Screen / Navigation Events

```
┌────────────┐
│ Event Name              │ Parameters                            │
├─────────┼──┤
│ screen_view             │ screen_name, previous_screen,         │
│                         │ device_id, session_id                 │
│                         │                                       │
│                         │ screen_name enum:                     │
│                         │   home, game, history, settings,      │
│                         │   about, help, feedback, onboarding,  │
│                         │   splash, winner_screen, settlement   │
└─────────┴──┘
```

### 3.3 Game Lifecycle Events (THE CORE)

```
┌────────────┐
│ Event Name              │ Parameters                            │
├─────────┼──┤
│ game_started            │ game_id, device_id, session_id,       │
│                         │ player_count, is_resume,              │
│                         │ pool_ball_mgmt_enabled,               │
│                         │ strict_mode_enabled,                  │
│                         │ auto_next_turn_enabled,               │
│                         │ scoreboard_layout (list/grid),        │
│                         │ device_model, app_version             │
├─────────┼──┤
│ game_score_entered      │ game_id, device_id,                   │
│                         │ player_index (NOT name),              │
│                         │ points, running_total,                │
│                         │ turn_number, input_method             │
│                         │ (custom_keyboard/system),             │
│                         │ time_since_last_score_sec             │
├─────────┼──┤
│ game_undo               │ game_id, device_id,                   │
│                         │ turn_number, points_reversed          │
├─────────┼──┤
│ game_correction         │ game_id, device_id,                   │
│                         │ player_index, old_score, new_score    │
├─────────┼──┤
│ game_paused             │ game_id, device_id,                   │
│                         │ duration_so_far_sec, turn_number      │
├─────────┼──┤
│ game_resumed            │ game_id, device_id,                   │
│                         │ pause_duration_sec                    │
├─────────┼──┤
│ game_completed          │ game_id, device_id, session_id,       │
│                         │ player_count,                         │
│                         │ total_turns,                          │
│                         │ total_scoring_events,                 │
│                         │ game_duration_sec,                    │
│                         │ winner_score, loser_score,            │
│                         │ score_spread (max - min),             │
│                         │ avg_score,                            │
│                         │ is_finalized (true),                  │
│                         │ undo_count, correction_count,         │
│                         │ pool_balls_remaining,                 │
│                         │ device_model, app_version             │
├─────────┼──┤
│ game_abandoned          │ game_id, device_id,                   │
│                         │ player_count, turns_played,           │
│                         │ duration_sec,                         │
│                         │ abandon_reason (reset/new_game/exit)  │
├─────────┼──┤
│ game_pool_ball_removed  │ game_id, device_id,                   │
│                         │ ball_number, ball_value,              │
│                         │ is_auto_removed,                      │
│                         │ balls_remaining                       │
└─────────┴──┘
```

### 3.4 Player Management Events

```
┌────────────┐
│ Event Name              │ Parameters                            │
├─────────┼──┤
│ player_added            │ device_id, total_saved_players,       │
│                         │ is_new_name (bool)                    │
├─────────┼──┤
│ player_removed          │ device_id, total_saved_players        │
├─────────┼──┤
│ player_hidden           │ device_id                             │
├─────────┼──┤
│ player_quick_select     │ device_id, players_selected_count     │
├─────────┼──┤
│ roster_sort_changed     │ device_id, sort_option                │
│                         │ (manual/alpha/losers/winners/random/  │
│                         │  most_played)                         │
└─────────┴──┘
```

### 3.5 Feature Usage Events

```
┌────────────┐
│ Event Name              │ Parameters                            │
├─────────┼──┤
│ settlement_viewed       │ game_id, device_id,                   │
│                         │ settlement_method, match_cost,        │
│                         │ currency, player_count,               │
│                         │ excluded_player_count                 │
├─────────┼──┤
│ game_shared             │ device_id, game_id,                   │
│                         │ share_method (text/image/etc)         │
├─────────┼──┤
│ history_viewed          │ device_id, total_games_in_history     │
├─────────┼──┤
│ history_game_opened     │ device_id, game_age_days              │
├─────────┼──┤
│ history_game_archived   │ device_id                             │
├─────────┼──┤
│ feedback_submitted      │ device_id, feedback_type,             │
│                         │ rating (if applicable)                │
├─────────┼──┤
│ snapshot_created        │ device_id, trigger (auto/manual/      │
│                         │ after_game), size_bytes               │
├─────────┼──┤
│ ball_preset_changed     │ device_id, preset_name,               │
│                         │ is_custom                             │
├─────────┼──┤
│ winner_screen_shown     │ device_id, game_id,                   │
│                         │ winner_screen_style (wavy/classic)    │
└─────────┴──┘
```

### 3.6 Settings Change Events

```
┌────────────┐
│ Event Name              │ Parameters                            │
├─────────┼──┤
│ setting_changed         │ device_id, setting_name,              │
│                         │ old_value, new_value                  │
│                         │                                       │
│                         │ Track these settings specifically:    │
│                         │   app_theme, app_font,                │
│                         │   scoreboard_layout,                  │
│                         │   strict_turn_mode,                   │
│                         │   auto_next_turn,                     │
│                         │   use_custom_keyboard,                │
│                         │   keyboard_height,                    │
│                         │   pool_ball_management,               │
│                         │   auto_remove_pool_balls,             │
│                         │   match_split_enabled,                │
│                         │   settlement_method,                  │
│                         │   haptic_feedback,                    │
│                         │   global_scale,                       │
│                         │   winner_screen_style,                │
│                         │   guest_mode                          │
└─────────┴──┘
```

### 3.7 Onboarding Events

```
┌────────────┐
│ Event Name              │ Parameters                            │
├─────────┼──┤
│ onboarding_started      │ device_id                             │
├─────────┼──┤
│ onboarding_step_viewed  │ device_id, step_index, step_name      │
├─────────┼──┤
│ onboarding_completed    │ device_id, duration_sec               │
├─────────┼──┤
│ onboarding_skipped      │ device_id, last_step_index            │
└─────────┴──┘
```

### 3.8 Device Settings Snapshot (fired once per session)

```
┌────────────┐
│ Event Name              │ Parameters                            │
├─────────┼──┤
│ device_config_snapshot  │ device_id, app_version,               │
│                         │ device_model, android_version,        │
│                         │ screen_density, screen_size,          │
│                         │ locale, timezone,                     │
│                         │                                       │
│                         │ // App config:                        │
│                         │ theme, font, keyboard_type,           │
│                         │ keyboard_height, layout,              │
│                         │ strict_mode, auto_next_turn,          │
│                         │ pool_ball_mgmt, auto_remove_balls,    │
│                         │ match_split, settlement_method,       │
│                         │ haptic, global_scale,                 │
│                         │ saved_player_count,                   │
│                         │ total_games_played,                   │
│                         │ guest_mode,                           │
│                         │ winner_screen_style,                  │
│                         │ roster_layout, roster_sort            │
└─────────┴──┘
```

---

## 4. Identity & Device Strategy

### The Problem Today
Your current `analyticsId` is a UUID stored in `AppSettings`. It's **nullable**, generated lazily, and if a user clears app data it's gone. You can't tell the difference between "new user" and "reinstall."

### The New Approach

```
┌─────────┐
│                        IDENTITY STACK                                │
├──────────┬──────┤
│ device_id        │ Stable per-install UUID. Generated at first       │
│                  │ launch. Stored in SharedPreferences (NOT          │
│                  │ DataStore/AppSettings - survives settings reset). │
│                  │ This is your PRIMARY key for everything.          │
├──────────┼──────┤
│ install_id       │ Firebase Installation ID (FID). Use               │
│                  │ FirebaseInstallations.getId(). Survives app       │
│                  │ updates but not reinstalls. Cross-reference       │
│                  │ with device_id to detect reinstalls.              │
├──────────┼──────┤
│ session_id       │ UUID generated each time the app comes to         │
│                  │ foreground from a cold/warm start. Ties all       │
│                  │ events in one session together.                   │
├──────────┼──────┤
│ game_id          │ Already exists in GameState.id - USE IT.          │
│                  │ Correlates game_started → scores → game_completed │
└──────────┴──────┘
```

### Key Rules
1. **`device_id` is NEVER nullable.** Generate it synchronously in `Application.onCreate()` before any analytics call.
2. **Every single event** gets `device_id` and `session_id` attached automatically - not optionally.
3. **`game_id`** is attached to every in-game event - this is how you correlate starts with completions.
4. **No player names in analytics.** Use `player_index` (0, 1, 2...) - privacy-safe but still useful for per-player behavior.

---

## 5. Architecture - What Replaces What

### Drop the Dual-System Complexity

```
CURRENT (broken):                    NEW (clean):
                                     
┌──────┐                     ┌──────┐
│ Firebase     │ ← generic events    │  PocketScoreAnalytics│
│ Analytics    │   nobody reads      │  (single orchestrator│
└──────┘                     │   singleton)         │
                                     └──┬───┘
┌──────┐                                │
│ Firestore    │ ← raw docs that    ┌───┼───┐
│ Cloud        │   cost money       │           │           │
│ Analytics    │   and can't be     ▼           ▼           ▼
└──────┘   queried       Firebase    Firestore    BigQuery
                                 Analytics  Aggregates   (optional)
                                 (events)   (rollups)    (warehouse)
```

### New Architecture Components

#### A. `PocketScoreAnalytics` - The Single Entry Point
- Replaces both `AnalyticsManager` and `CloudAnalyticsManager`
- **Initialized in `PocketScoreApp.onCreate()`** (not `MainActivity`)
- Holds `device_id` (from SharedPrefs) and `session_id` (generated on init)
- Every public method auto-attaches `device_id`, `session_id`, `app_version`, `timestamp`
- Logs to **Firebase Analytics** (primary) with properly named custom events
- Optionally writes aggregated rollups to Firestore (not raw events)

#### B. Firebase Analytics - The Event Store
- **Stop using generic events** (`LEVEL_START`, `LEVEL_END`, `APP_OPEN`)
- Use **custom event names** from the taxonomy above (`game_started`, `game_completed`, etc.)
- Set **User Properties** for segmentation:

```
User Properties to register in Firebase Console:
─────
• app_theme          (light / dark / system)
• keyboard_type      (custom / system)
• pool_ball_mgmt     (enabled / disabled)
• match_split        (enabled / disabled)
• total_games_played (bucket: 0, 1-5, 6-20, 21-50, 51-100, 100+)
• days_since_install  (bucket: 0, 1-7, 8-30, 31-90, 90+)
• primary_player_count (most common player count: 2, 3, 4, 5+)
• device_tier         (low / mid / high based on RAM/SDK)
```

#### C. Firestore - Aggregated Rollups Only (NOT Raw Events)

> [!CAUTION]
> **Stop writing raw per-event documents to Firestore.** At scale this will cost you real money and you can't query it efficiently. Firebase Analytics already stores raw events for free (up to limits).

Use Firestore only for **pre-aggregated daily rollup documents** that you can read cheaply from a dashboard or Cloud Function:

```
Firestore Collections (NEW):
────────────

📁 daily_stats/{YYYY-MM-DD}
   {
     date: "2026-10-22",
     total_devices: 147,
     new_devices: 12,
     returning_devices: 135,
     total_games_started: 423,
     total_games_completed: 389,
     total_games_abandoned: 34,
     completion_rate: 0.92,
     avg_players_per_game: 3.2,
     avg_game_duration_min: 18.5,
     avg_turns_per_game: 24.7,
     total_unique_players: 891,
     avg_players_per_device: 6.1,
     peak_hour: 19,
     player_count_distribution: {
       "2": 120, "3": 145, "4": 89,
       "5": 35, "6+": 34
     },
     game_duration_distribution: {
       "0-5min": 45, "5-15min": 156,
       "15-30min": 134, "30-60min": 42, "60+min": 12
     },
     app_version_distribution: {
       "2.1.0": 89, "2.0.9": 58
     },
     feature_usage: {
       pool_ball_mgmt: 0.67,
       match_split: 0.34,
       strict_mode: 0.12,
       custom_keyboard: 0.89,
       guest_mode: 0.05
     },
     updatedAt: <server_timestamp>
   }

📁 device_profiles/{device_id}
   {
     device_id: "uuid",
     first_seen: <timestamp>,
     last_seen: <timestamp>,
     total_sessions: 234,
     total_games: 156,
     total_unique_player_names_count: 8,
     avg_players_per_game: 3.4,
     device_model: "Pixel 7",
     android_version: "14",
     current_app_version: "2.1.0",
     favorite_player_count: 4,
     days_active: 45,
     last_30d_sessions: 22,
     last_30d_games: 34,
     config: {
       theme: "dark",
       font: "inter",
       keyboard: "custom",
       pool_mgmt: true,
       match_split: true,
       settlement: "sole_loser_pays"
     },
     updatedAt: <server_timestamp>
   }
```

#### D. Cloud Functions - The Aggregation Engine

Write **one Cloud Function** triggered daily (scheduled via Cloud Scheduler or Pub/Sub) that:

1. Reads Firebase Analytics exports from BigQuery (if enabled) **OR** reads the `device_profiles` collection
2. Computes the `daily_stats` rollup document for the previous day
3. Updates `device_profiles` with latest activity

Alternatively, if you don't want BigQuery, use **on-write triggers**:
- When a `device_profiles/{device_id}` doc is updated (from the app), a Cloud Function increments the appropriate counters in `daily_stats/{date}` using `FieldValue.increment()`.

---

## 6. Implementation Plan - Step by Step

### Phase 1: Foundation (Do First)

| # | Task | Details |
|---|---|---|
| 1.1 | **Create `DeviceIdManager`** | New singleton. On first call, checks `SharedPreferences` for `device_id`. If missing, generates UUID and writes it. Returns the ID **synchronously**. This is called in `PocketScoreApp.onCreate()`. |
| 1.2 | **Create `SessionManager`** | Generates a `session_id` UUID on each app foreground transition. Uses `ProcessLifecycleOwner` to detect foreground/background. Tracks session start time. Fires `app_session_start` on foreground, `app_session_end` on background. |
| 1.3 | **Create `PocketScoreAnalytics`** | New singleton replacing both `AnalyticsManager` and `CloudAnalyticsManager`. Holds references to `DeviceIdManager` and `SessionManager`. All public methods auto-attach `device_id`, `session_id`, `app_version`, `timestamp`. Internally calls `FirebaseAnalytics.logEvent()` with custom event names. |
| 1.4 | **Register User Properties** | In Firebase Console, register all User Properties listed in Section 5B. In code, call `setUserProperty()` on each session start with current values from `AppSettings`. |
| 1.5 | **Register Custom Definitions** | In Firebase Console → Custom Definitions, register all custom event parameters (player_count, game_duration_sec, etc.) so they appear in reports and can be used for audiences. |

### Phase 2: Core Game Events (Do Second)

| # | Task | Details |
|---|---|---|
| 2.1 | **Wire `game_started`** | In `GameViewModel.startNewGame()` - pass `game_id`, player count, and current game settings (pool mgmt, strict mode, etc.) |
| 2.2 | **Wire `game_completed`** | In `logGameEndedAnalytics()` - compute ALL the parameters: undo count (count UNDO events in globalEvents), correction count, score spread, avg score, balls remaining. Use `game.id` as `game_id`. |
| 2.3 | **Wire `game_abandoned`** | In `endGame()` when `isFinalized = false` - include abandon reason (reset vs new game vs exit). |
| 2.4 | **Wire `game_score_entered`** | In `processScore()` or equivalent - log every score. Include `turn_number` (globalEvents count), `time_since_last_score_sec`. **Use `player_index` not player name.** |
| 2.5 | **Wire `game_undo`** | In the undo handler - log with turn number and points reversed. |
| 2.6 | **Wire `game_paused` / `game_resumed`** | In the pause/resume handlers - log duration of pause. |
| 2.7 | **Wire `game_pool_ball_removed`** | When a ball is removed - log ball number, value, whether auto-removed. |

### Phase 3: Navigation & Feature Events (Do Third)

| # | Task | Details |
|---|---|---|
| 3.1 | **Add `screen_view` tracking** | Add a navigation listener in `MainActivity` or `NavHost`. Log `screen_view` with `screen_name` on every navigation. Firebase has `FirebaseAnalytics.Event.SCREEN_VIEW` - use it with `FirebaseAnalytics.Param.SCREEN_NAME`. |
| 3.2 | **Wire onboarding events** | In onboarding screens - `onboarding_started`, `onboarding_step_viewed`, `onboarding_completed`, `onboarding_skipped`. |
| 3.3 | **Wire feature events** | `settlement_viewed`, `game_shared`, `history_viewed`, `history_game_opened`, `feedback_submitted`, `snapshot_created`, `winner_screen_shown`. |
| 3.4 | **Wire `setting_changed`** | In `GameViewModel.updateSettings()` - compare old and new settings, log each changed field as a separate `setting_changed` event. |
| 3.5 | **Wire `device_config_snapshot`** | Fire once per session during `app_session_start`. Captures the full settings state so you can see distributions. |

### Phase 4: Aggregation Backend (Do Fourth)

| # | Task | Details |
|---|---|---|
| 4.1 | **Write `device_profiles` updater** | On each `app_session_end`, upsert the device's Firestore profile doc with latest stats (increment session count, update last_seen, update config snapshot). Use `FieldValue.increment()` and `set(data, SetOptions.merge())`. |
| 4.2 | **Create daily rollup Cloud Function** | Scheduled function that reads `device_profiles` where `last_seen` is today, aggregates into `daily_stats/{date}`. |
| 4.3 | **Build retention Cloud Function** | Weekly function that computes D1/D7/D30 retention by comparing `first_seen` cohorts against `last_seen` dates in `device_profiles`. |

### Phase 5: Dashboard & Cleanup (Do Last)

| # | Task | Details |
|---|---|---|
| 5.1 | **Build Firebase Dashboard** | Create custom dashboards in Firebase Console using the new events. Set up Audiences (power users, churned, new). |
| 5.2 | **Set up BigQuery export** | Enable BigQuery linking in Firebase. This gives you raw event-level SQL access for complex queries. Free tier handles most indie apps. |
| 5.3 | **Delete old Firestore collections** | Remove `analytics_installs`, `analytics_daily_activity`, `analytics_matches`, `analytics_events`. They're costing reads/writes for data you can't use. |
| 5.4 | **Remove old code** | Delete `CloudAnalyticsManager.kt`. Delete `AnalyticsManager.kt`. Remove all references. |

---

## 7. Firestore Security Rules

```javascript
// Firestore Security Rules for Analytics
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    
    // Device profiles - devices can only write their own profile
    match /device_profiles/{deviceId} {
      allow read: if false;  // Only Cloud Functions read these
      allow write: if true;  // App writes its own profile
      // In production, validate the document structure:
      // allow write: if request.resource.data.keys().hasAll(['device_id', 'last_seen'])
      //              && request.resource.data.device_id == deviceId;
    }
    
    // Daily stats - only Cloud Functions write these
    match /daily_stats/{date} {
      allow read: if false;   // Read from admin SDK only
      allow write: if false;  // Written by Cloud Functions only
    }
  }
}
```

---

## 8. What You'll Be Able to Answer

After implementation, you'll be able to answer these questions **directly from Firebase Console or a simple Firestore read**:

### Daily Glance (from `daily_stats`)
- ✅ "How many devices used the app today?" → `total_devices`
- ✅ "How many games were played today?" → `total_games_completed`
- ✅ "What's the average number of players per game?" → `avg_players_per_game`
- ✅ "How many people use the app per device on average?" → `avg_players_per_device`
- ✅ "What's the completion rate?" → `completion_rate`
- ✅ "What's the most popular player count?" → `player_count_distribution`
- ✅ "When do people play the most?" → `peak_hour`

### Weekly/Monthly (from Firebase Analytics)
- ✅ "What % of users who installed last week came back this week?" → D7 retention audience
- ✅ "Which features are people actually using?" → `feature_usage` in daily_stats
- ✅ "How long do games typically last?" → `game_duration_distribution`
- ✅ "Where do new users drop off?" → Onboarding funnel (started → step1 → step2 → completed)
- ✅ "Are people finishing games or abandoning them?" → `game_completed` vs `game_abandoned` funnel
- ✅ "What's the most popular theme/font/keyboard?" → `device_config_snapshot` User Properties

### Strategic (from BigQuery or Firestore)
- ✅ "Is the app growing or shrinking?" → `new_devices` trend over daily_stats
- ✅ "What device models should I prioritize testing?" → `device_model` distribution from `device_profiles`
- ✅ "Is match splitting driving engagement?" → Compare session length / games per session for match_split=true vs false
- ✅ "What's the DAD/WAD/MAD trend?" → Computed from `device_profiles.last_seen` ranges

---

## 9. Firebase Analytics Console Setup Checklist

Do these in the Firebase Console **before** deploying code:

- [ ] **Custom Definitions → Custom Dimensions:**
  - `game_id` (Event scope)
  - `session_id` (Event scope)
  - `device_id` (User scope - also set as User Property)
  - `player_count` (Event scope)
  - `game_duration_sec` (Event scope)
  - `total_turns` (Event scope)
  - `screen_name` (Event scope)
  - `is_finalized` (Event scope)
  - `is_resume` (Event scope)
  - `setting_name` (Event scope)

- [ ] **Custom Definitions → Custom Metrics:**
  - `player_count` (Event scope, Sum)
  - `game_duration_sec` (Event scope, Average)
  - `total_turns` (Event scope, Average)
  - `winner_score` (Event scope, Average)
  - `score_spread` (Event scope, Average)
  - `session_duration_sec` (Event scope, Average)

- [ ] **User Properties (register all):**
  - `app_theme`
  - `keyboard_type`
  - `pool_ball_mgmt`
  - `match_split`
  - `total_games_bucket`
  - `days_since_install_bucket`
  - `primary_player_count`
  - `device_tier`

- [ ] **Audiences (create these):**
  - "Power Users" → `total_games_bucket` in [21-50, 51-100, 100+]
  - "New Users (7 days)" → `days_since_install_bucket` = "0" or "1-7"
  - "Churning" → last `app_session_start` > 14 days ago
  - "Match Splitters" → `match_split` = "enabled"
  - "Pool Mode Users" → `pool_ball_mgmt` = "enabled"

- [ ] **Enable BigQuery Export:**
  - Project Settings → Integrations → BigQuery → Link
  - This gives you `analytics_XXXXXX.events_*` tables for SQL queries

---

## 10. Cost Estimation & Guardrails

### Firebase Analytics
- **Free.** Unlimited events. 500 distinct event types. 25 User Properties. No cost concern.

### Firestore (New Aggregated Approach)
| Operation | Volume (1,000 DAD) | Monthly Cost |
|---|---|---|
| `device_profiles` writes | ~1,000/day × 30 = 30,000 writes | ~$0.054 |
| `daily_stats` writes | 1/day = 30 writes | ~$0.00 |
| Cloud Function reads | ~1,000/day × 30 = 30,000 reads | ~$0.012 |
| **Total** | | **~$0.07/month** |

### Firestore (Current Raw Approach for Comparison)
| Operation | Volume (1,000 DAD) | Monthly Cost |
|---|---|---|
| `analytics_events` writes | ~10 events/session × 1,000 × 30 = 300,000 | ~$0.54 |
| `analytics_matches` writes | ~3 games/session × 1,000 × 30 = 90,000 | ~$0.16 |
| `analytics_daily_activity` | 1,000 × 30 = 30,000 | ~$0.054 |
| `analytics_installs` | negligible | ~$0.00 |
| **Total** | | **~$0.75/month** |
| **And you STILL can't query it.** | | 💸 |

### Guardrails to Add in Code
1. **Rate limit `game_score_entered`** - If a game has > 200 turns, stop logging individual scores (aggregate at game end instead). Prevents abuse/bugs from flooding events.
2. **Batch Firestore writes** - Don't write `device_profiles` on every event. Buffer updates and write once on `app_session_end`.
3. **Max event params** - Firebase allows 25 custom parameters per event. The taxonomy above stays within limits, but validate before adding more.

---

## 11. Privacy Considerations

> [!IMPORTANT]
> This design is still privacy-respectful - it just stops being *uselessly* private.

| What we track | What we DON'T track |
|---|---|
| Device model, OS version | Player names (use `player_index`) |
| Anonymous device UUID | Location / GPS |
| Game stats (scores, duration, player count) | Personal identifiers |
| Feature usage patterns | Contacts or social data |
| App configuration | Photos / media |
| Session timing | Cross-app behavior |

**For your Play Store listing / Privacy Policy, disclose:**
- Analytics data collected: device info, app usage, game statistics
- Purpose: app improvement and bug fixing
- Data is anonymous and not linked to personal identity
- No data is sold to third parties

---

## 12. Migration Checklist

```
Phase 1 - Foundation:
  □ Create DeviceIdManager (SharedPreferences-based)
  □ Create SessionManager (ProcessLifecycleOwner-based)  
  □ Create PocketScoreAnalytics singleton
  □ Initialize in PocketScoreApp.onCreate()
  □ Verify device_id is never null in any event
  □ Register User Properties in Firebase Console
  □ Register Custom Definitions in Firebase Console

Phase 2 - Core Events:
  □ Wire game_started with full parameters
  □ Wire game_completed with computed stats
  □ Wire game_abandoned with reason
  □ Wire game_score_entered (with rate limiting)
  □ Wire game_undo, game_correction
  □ Wire game_paused / game_resumed
  □ Wire game_pool_ball_removed
  □ Test: Start game → play → end → verify events in DebugView

Phase 3 - Navigation & Features:
  □ Add screen_view tracking to NavHost
  □ Wire onboarding funnel events
  □ Wire settlement_viewed, game_shared, history events
  □ Wire setting_changed with diff detection
  □ Wire device_config_snapshot on session start
  □ Test: Walk through every screen → verify in DebugView

Phase 4 - Backend:
  □ Deploy device_profiles upsert logic in app
  □ Deploy daily_stats Cloud Function
  □ Deploy retention Cloud Function
  □ Enable BigQuery export
  □ Test: Verify daily_stats populates correctly

Phase 5 - Cleanup:
  □ Build Firebase Console dashboards
  □ Create Audiences for segmentation
  □ Delete old Firestore collections (analytics_*)
  □ Delete AnalyticsManager.kt
  □ Delete CloudAnalyticsManager.kt
  □ Update GameViewModel to use PocketScoreAnalytics
  □ Update Privacy Policy
```

---

> [!TIP]
> **Quick win before any code changes:** Enable **Firebase DebugView** on a test device (`adb shell setprop debug.firebase.analytics.app com.mwarrc.pocketscore`). This lets you see events in real-time as you implement them, instead of waiting 24 hours for them to appear in reports.
