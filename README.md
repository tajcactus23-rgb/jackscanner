# BlueMeanie 🎯

<p align="center">
  <a href="https://github.com/tajcactus23-rgb/jackscanner/stargazers">
    <img src="https://img.shields.io/github/stars/tajcactus23-rgb/jackscanner?style=flat&label=★&color=yellow" alt="Stars">
  </a>
  <a href="https://github.com/tajcactus23-rgb/jackscanner/issues">
    <img src="https://img.shields.io/github/issues/tajcactus23-rgb/jackscanner?color=red" alt="Issues">
  </a>
  <a href="https://opensource.org/licenses/MIT">
    <img src="https://img.shields.io/badge/License-MIT-green.svg" alt="License">
  </a>
  <img src="https://img.shields.io/badge/Android-12%2B-green.svg" alt="Android">
  <img src="https://img.shields.io/badge/Kotlin-1.9.22-purple.svg" alt="Kotlin">
</p>

<p align="center">
  <b>BlueMeanie</b> is a professional BLE (Bluetooth Low Energy) scanner designed to detect nearby Axon body cameras, tasers, and other Axon devices.
</p>

---

## ⚠️ Legal Notice

> **This tool is for educational and research purposes only.**
> 
> Use only where scanning is legal. Check local laws before use.
> 
> This project is **not affiliated with** Axon Enterprise, Inc.

---

## 📱 Overview

BlueMeanie transforms your Android device into a sophisticated Axon device detection system. With a modern, professional interface and powerful scanning capabilities, it provides real-time BLE advertisement monitoring with community features.

### Detects

| Device Type | OUI/Pattern | Description |
|-------------|-------------|-------------|
| Axon Body Cameras | 00:25:DF | Officer worn cameras |
| Axon Taser Weapons | 00:25:DF | Smart weapons |
| Axon Flex | FC:A9:E8 | Vehicle mounted systems |

---

## ✨ Features

### Core Features
- **Real-time BLE Scanning** - Continuous monitoring for Axon device signatures
- **Smart Detection** - Multi-factor detection using MAC OUI and device names
- **Persistent Background Service** - Continue scanning even when app is closed
- **Persistent Notifications** - Live status updates while scanning
- **Detection History** - Local database of all detected devices

### User Interface
- **Dark Metallic Theme** - Professional, modern design
- **10 Themes Available** - BlueMeanie Classic, Carbon, Titanium, Aurora, Monolith, Arctic, Midnight, Quantum, Nova, Glass
- **Animated Radar Display** - Visual feedback during scanning
- **Glass Card Design** - Modern UI components with subtle borders
- **Bottom Navigation** - Easy access to all features

### Screens
- **Home** - Radar visualization, stats, quick controls
- **Feed** - Detection history with filtering options
- **Heatmap** - Community detection activity visualization
- **Community** - Anonymous chat with other users
- **Scoreboard** - Leaderboard and rank system
- **Settings** - Full customization options

### Onboarding
- **Animated Welcome** - Professional introduction
- **Permission Explanations** - Clear explanations for each permission
- **Privacy-First Design** - Anonymous mode by default
- **Step-by-Step Flow** - Guided setup process

### Notifications
- **Foreground Service Notification** - Persistent status display
- **Detection Alerts** - Immediate alerts when devices detected
- **Community Activity Alerts** - Nearby detection notifications
- **Action Buttons** - Quick access to Heatmap and Feed

---

## 🔐 Permissions

### Required Permissions

| Permission | Purpose |
|------------|---------|
| `BLUETOOTH_SCAN` | Scan for nearby BLE devices |
| `BLUETOOTH_CONNECT` | Connect to Bluetooth devices |
| `ACCESS_FINE_LOCATION` | Required for BLE scanning on older Android |
| `POST_NOTIFICATIONS` | Alert you when devices are detected |
| `FOREGROUND_SERVICE` | Run scanning in background |
| `VIBRATE` | Haptic feedback on detection |
| `WAKE_LOCK` | Keep device awake during scanning |

### Optional Permissions

| Permission | Purpose |
|------------|---------|
| `RECEIVE_BOOT_COMPLETED` | Auto-start scanning after reboot |
| `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` | Uninterrupted background scanning |

---

## 🔍 How Detection Works

BlueMeanie uses a multi-factor detection approach:

```
1. BLE Scan
   └─> Receive advertisement packets
   
2. MAC Address Analysis
   └─> Extract OUI (first 6 chars)
   └─> Match against known Axon OUIs:
       - 00:25:DF (Primary)
       - FC:A9:E8 (Secondary)
       
3. Device Name Analysis
   └─> Check for patterns:
       - AXON*
       - TASER*
       - AXON_BODY*
       - AXON_CAMERA*
       - etc.

4. Detection Alert
   └─> If match found → Notify user
```

### Signal Range Reference

| RSSI (dBm) | Approximate Distance |
|------------|---------------------|
| -50 to -60 | Very close (< 3m) |
| -60 to -70 | Close (3-10m) |
| -70 to -80 | Medium (10-20m) |
| -80 to -90 | Far (> 20m) |

---

## 📂 Project Structure

```
jackscanner/
├── jackscanner/                    # Android app
│   ├── app/
│   │   ├── src/main/
│   │   │   ├── java/com/jackscanner/
│   │   │   │   ├── ui/             # Jetpack Compose UI
│   │   │   │   │   ├── screens/    # Screen composables
│   │   │   │   │   ├── components/  # Reusable UI components
│   │   │   │   │   ├── theme/       # Theme and colors
│   │   │   │   │   └── navigation/  # Navigation graph
│   │   │   │   ├── domain/          # Business logic
│   │   │   │   │   ├── model/       # Domain models
│   │   │   │   │   └── repository/  # Repository interfaces
│   │   │   │   ├── data/            # Data layer
│   │   │   │   │   ├── local/       # Room database
│   │   │   │   │   ├── repository/  # Repository implementations
│   │   │   │   │   └── preferences/ # DataStore preferences
│   │   │   │   ├── service/         # BLE scanning service
│   │   │   │   ├── di/              # Hilt dependency injection
│   │   │   │   └── utils/           # Utilities (OuiMapper)
│   │   │   └── res/                 # Resources
│   │   └── build.gradle
│   └── build.gradle
├── .github/workflows/              # CI/CD workflows
├── docs/                          # Documentation
└── README.md                      # This file
```

---

## 🛠️ Build Instructions

### Prerequisites
- Android Studio Hedgehog (2024.1.1) or later
- JDK 17+
- Android SDK 34
- Gradle 8.2+

### Build Commands

```bash
# Clone repository
git clone https://github.com/tajcactus23-rgb/jackscanner.git
cd jackscanner

# Build debug APK
cd jackscanner
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Clean and rebuild
./gradlew clean assembleDebug

# Run unit tests
./gradlew test

# Run instrumented tests
./gradlew connectedAndroidTest
```

### APK Location
- Debug: `jackscanner/app/build/outputs/apk/debug/app-debug.apk`
- Release: `jackscanner/app/build/outputs/apk/release/app-release.apk`

---

## 📝 Release Instructions

### Version Numbering
- **Version Name**: Semantic versioning (e.g., 2.0.0)
- **Version Code**: Integer incrementing (e.g., 1, 2, 3...)

### Release Checklist

1. **Update Version**
   - Bump `versionName` in `app/build.gradle`
   - Bump `versionCode` in `app/build.gradle`

2. **Update Changelog**
   - Add new release section with date
   - Document all changes
   - Update compatibility notes

3. **GitHub Release**
   - Create git tag: `git tag v2.0.0`
   - Push tag: `git push origin v2.0.0`
   - GitHub Actions will build and create release

4. **Verify Artifacts**
   - Check debug APK builds successfully
   - Verify release APK (if applicable)
   - Confirm all workflows pass

---

## 🔧 Troubleshooting

### Scanner Not Detecting Devices

1. **Check Bluetooth**
   - Ensure Bluetooth is enabled
   - Verify location permission granted
   - Restart Bluetooth if needed

2. **Android Version**
   - Android 12+ requires BLUETOOTH_SCAN and BLUETOOTH_CONNECT
   - Android 11 and below require ACCESS_FINE_LOCATION

3. **Battery Optimization**
   - Disable battery optimization for reliable background scanning
   - Grant "Ignore battery optimization" permission in Settings

4. **Random MAC Addresses**
   - Some devices use random MAC addresses (Android 10+)
   - Detection may be affected by this Android privacy feature

### App Crashing

1. **Clear Cache**
   - Settings → Apps → BlueMeanie → Clear Cache

2. **Reinstall**
   - Uninstall and reinstall the app

3. **Check Logs**
   - Connect device to computer
   - Run `adb logcat` for debugging

---

## ❓ FAQ

**Q: Is this app legal to use?**
A: BLE scanning itself is generally legal in public spaces. However, laws vary by jurisdiction. Always check local regulations before use.

**Q: How accurate is the detection?**
A: Detection accuracy depends on device hardware, environmental factors, and whether target devices use random MAC addresses.

**Q: Does this drain battery?**
A: Continuous BLE scanning does use battery. The app uses SCAN_MODE_LOW_LATENCY for best detection but allows switching to LOW_POWER mode for battery savings.

**Q: Is my data shared?**
A: By default, the app operates in anonymous mode. Location sharing is disabled by default and can be enabled in Settings.

**Q: Can I run this in the background?**
A: Yes, the app uses a foreground service to continue scanning even when closed. A persistent notification is shown.

---

## 🙏 Acknowledgments

- Inspired by [lookout.py](https://github.com/judcrandall/lookout.py)
- Inspired by [PoliceDetector](https://github.com/omtoi101/PoliceDetector)
- DEF CON 31 talks by Null Agent and Sally
- Jetpack Compose and Material Design 3

---

## 📜 License

MIT License - See [LICENSE](LICENSE)

---

<p align="center">
  <sub>BlueMeanie - Axon Device Scanner</sub>
  <br>
  <sub>Version 2.0.0</sub>
</p>