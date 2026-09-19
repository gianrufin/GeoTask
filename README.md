# GeoReminder 📍⚡

An offline-first, battery-efficient Android geofencing reminder and one-tap activity logger built with **Jetpack Compose**, **Material 3**, and **Room Database**.

---

## 📥 Download APK

You can download the pre-compiled APK directly from this repository:

👉 **[Download GeoReminder.apk](./release/GeoReminder.apk)** (located in `/release/GeoReminder.apk`)

### Installation Instructions
1. Download `GeoReminder.apk` to your Android device.
2. Open the file in your device's File Manager or Downloads app.
3. If prompted, allow "Install Unknown Apps" for your browser or file manager in Android Settings.
4. Tap **Install** and launch **GeoReminder**.

---

## 🌟 Key Features

### 1. 📍 Offline Geofence Reminders
- Monitor arrival, departure, and proximity zones without requiring an internet connection.
- Dynamic distance calculations comparing your live GPS coordinate against customized location pins.
- Configurable radius thresholds (50m to 1,000m) with entry, exit, or nearby trigger conditions.
- Quick location presets (Office HQ, Local Supermarket, Central Library, Gym, Home Residence, etc.) plus custom GPS coordinates.

### 2. 📅 Smart Weekday Scheduling
- Automatically mute alerts on weekends (Saturday & Sunday) to prevent work or school reminders during downtime.
- Granular 7-day bitmask toggle to select any combination of days (e.g., Mon/Wed/Fri or Tuesday only).

### 3. ⚡ One-Tap Fast Logging for Repetitive Tasks
- Pin frequent tasks to the main screen's One-Tap Quick Action bar.
- Instantly record arrivals, shift check-ins, or parcel deliveries with a single tap.
- Offline activity history captures exact timestamp, location label, and GPS coordinates without opening multi-step dialogs.

### 4. 🔔 Subtle, Low-Intrusiveness Audio Synthesis
- Built-in lightweight PCM tone generator synthesized on device using Android `AudioTrack`.
- 4 subtle sound styles:
  - **Subtle Chime** (Gentle dual-tone 587Hz & 880Hz harmonic)
  - **Gentle Bell** (Soft harmonic fade)
  - **Soft Pop** (Warm low-frequency blip)
  - **Discreet Vibration** (Silent tactile haptic)
- Built-in sound volume slider and preview button.

### 5. 🔋 Battery-Efficient Background Monitoring
- Uses Android `FusedLocationProviderClient` with balanced power priority.
- Displacement gating: Only evaluates geofences when the device moves more than 50 meters, preventing CPU wakeups while stationary.
- User-selectable polling intervals (1, 5, 10, or 15 minutes).

### 6. 🎨 Accessibility & High-Contrast Theming
- Strict Material Design 3 guidelines with generous touch targets (≥ 48dp).
- Full support for **Dark Mode** (System Default, Forced Dark, Forced Light).
- 4 high-contrast accessibility color profiles:
  - **Slate & Cyan**: Modern cool palette with vibrant accents.
  - **High-Contrast Amber**: Maximum contrast for outdoor sunlight visibility.
  - **Emerald Focus**: Calming green tones for focused routines.
  - **High-Contrast Monochrome**: Minimalist black/white scheme.

### 7. 🧪 Built-in Geofence Simulator
- Test location triggers directly in the app without having to physically walk or travel outside.

---

## 🛠️ Architecture & Tech Stack

GeoReminder is built following modern Android best practices and Clean Architecture:

| Layer | Technology |
|---|---|
| **Language** | Kotlin 2.0+ |
| **UI Toolkit** | Jetpack Compose + Material Design 3 (M3) |
| **State Management** | Android `ViewModel`, `StateFlow`, `combine` flows |
| **Local Database** | Jetpack Room 2.6+ with SQLite & Kotlin Symbol Processing (KSP) |
| **Location Engine** | Google Play Services Location (`FusedLocationProviderClient`) + `LocationManager` fallback |
| **Audio Engine** | Custom low-latency PCM waveform synthesizer via `android.media.AudioTrack` |
| **Background Work** | Foreground Service with low-priority notification channel |
| **Testing** | Robolectric for local JVM unit testing and Roborazzi for visual verification |

---

## 📂 Project Structure

```
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/
│   │   │   │   ├── MainActivity.kt               # Entry point, navigation tabs, permissions
│   │   │   │   ├── GeoReminderApplication.kt     # App container initializing repositories
│   │   │   │   ├── audio/
│   │   │   │   │   └── AudioToneSynthesizer.kt   # Runtime PCM audio tone synthesis
│   │   │   │   ├── data/
│   │   │   │   │   ├── local/                    # Room Database, ReminderDao, QuickLogDao
│   │   │   │   │   ├── model/                    # ReminderEntity, QuickLogEntity
│   │   │   │   │   └── repository/               # ReminderRepository, SettingsRepository
│   │   │   │   ├── location/
│   │   │   │   │   └── LocationHelper.kt         # Haversine distance, geofence evaluator, presets
│   │   │   │   ├── notification/
│   │   │   │   │   └── NotificationHelper.kt     # Notification channels, heads-up push alerts
│   │   │   │   ├── service/
│   │   │   │   │   └── LocationTrackingService.kt# Battery-efficient background tracking
│   │   │   │   └── ui/
│   │   │   │       ├── MainViewModel.kt          # UI state, coroutine event handlers
│   │   │   │       ├── components/               # ReminderCard, AddEditDialog, QuickActionBar
│   │   │   │       ├── screens/                  # RemindersScreen, QuickLogScreen, SettingsScreen
│   │   │   │       └── theme/                    # Theme.kt, AppColorPalette, DarkModePreference
│   │   │   └── res/                              # Adaptive icons, vector drawables, strings
│   │   └── test/                                 # Unit & Robolectric tests
├── release/
│   └── GeoReminder.apk                           # Ready-to-install Android APK
├── gradle/
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

---

## 🚀 How to Build from Source

### Prerequisites
- Android Studio Ladybug (2024.2+) or Meerkat
- JDK 17 or higher
- Android SDK Platform 35
- Android Build Tools 35.0.0

### Command-Line Build
Clone the repository and run Gradle commands from the root directory:

```bash
# Clean and assemble debug APK
gradle assembleDebug

# Output APK will be located at:
# app/build/outputs/apk/debug/app-debug.apk

# Run local unit tests (Robolectric)
gradle :app:testDebugUnitTest
```

---

## 🔒 Permissions & Privacy
GeoReminder works completely offline and respects user privacy:
- `ACCESS_FINE_LOCATION` & `ACCESS_COARSE_LOCATION`: Required strictly to calculate proximity to user-defined geofences on device.
- `POST_NOTIFICATIONS`: Delivers local push notifications when a reminder zone is entered or exited.
- `FOREGROUND_SERVICE` & `FOREGROUND_SERVICE_LOCATION`: Maintains passive, battery-efficient geofence monitoring while the screen is off.
- `VIBRATE`: Provides discreet haptic feedback on reminder triggers and one-tap logging.

**No tracking, no analytics, no external servers.** All reminders, notes, and activity history remain on your device.
