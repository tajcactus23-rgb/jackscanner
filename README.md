# JackScanner 🛡️

<p align="center">
  <a href="https://github.com/follow">
    <img src="https://img.shields.io/github/stars/jackscanner?style=flat&label=★&color=yellow" alt="Stars">
  </a>
  <a href="https://github.com/jackscanner/jackscanner/issues">
    <img src="https://img.shields.io/github/issues/jackscanner/jackscanner?color=red" alt="Issues">
  </a>
  <a href="https://opensource.org/licenses/MIT">
    <img src="https://img.shields.io/badge/License-MIT-green.svg" alt="License">
  </a>
  <img src="https://img.shields.io/badge/Python-3.x-blue.svg" alt="Python">
  <img src="https://img.shields.io/badge/Android-12%2B-green.svg" alt="Android">
</p>

<p align="center">
  <b>JackScanner</b> is a BLE (Bluetooth Low Energy) scanner designed to detect nearby Axon body cameras, tasers, and other police equipment. Originally inspired by research from DEF CON 31 talks.
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

JackScanner Detects:

| Device Type | OUI | Notes |
|-------------|-----|-------|
| Axon Body Cameras | 00:25:DF | Officer worn |
| Axon Taser | 00:25:DF | Weapons |
| Axon Flex | 00:25:DF | Vehicle mounted |

```
┌─────────────────────────────────────┐
│  JackScanner                        │
├─────────────────────────────────────┤
│  ┌─────────────────┐  ┌──────────┐  │
│  │ BLE Scanner    │──│ Detects │  │
│  │ (hcitool/bleak)│  │ 00:25:DF│  │
│  └─────────────────┘  └──────────┘  │
│                                    │
│  ┌─────────────────┐  ┌──────────┐  │
│  │ Alert System   │──│ Notify  │  │
│  │ (vibrate/sound)│  │ +Log    │  │
│  └─────────────────┘  └──────────┘  │
└─────────────────────────────────────┘
```

---

## 🚀 Features

### Core
- **Continuous Scanning** - Real-time BLE device detection
- **OUI Filtering** - Targets only Axon devices (00:25:DF)
- **Dual Mode** - Works with or without root
- **Logging** - Records all detections with timestamps

### Alert Options
- **Sound** - Audio notification on detection
- **Vibrate** - Haptic feedback (Termux)
- **Push Notification** - System notification (Termux)

### Platforms
- **Python** - Termux/Android (root or non-root)
- **Kotlin** - Native Android app source

---

## 💻 Installation

### Termux (Android - Recommended)

```bash
# Install dependencies
pkg update && pkg upgrade -y
pkg install -y python git bluez-utils

# Clone repo
git clone https://github.com/jackscanner/jackscanner.git
cd jackscanner

# Install Python dependencies
pip install bleak

# Run (non-root mode)
python jackscanner.py
```

### Root Mode (hcitool)

```bash
# If you have root, use hcitool for better scanning
# Enable in Magisk - Grant root to Termux

# Run scanner
python jackscanner.py
```

---

## 📱 Android App (APK)

Pre-built APK coming soon. Build from source:

```bash
cd android
./gradlew assembleDebug
```

APK Location: `app/build/outputs/apk/debug/app-debug.apk`

---

## 🔧 Usage

```
$ python jackscanner.py

══════════════════════════════════════════
 JackScanner - BLE Device Scanner
 Target OUI: 00:25:DF (Axon)
══════════════════════════════════════════

[*] Using hcitool (root mode)
[14:32:05] Scanning... (3 devices)
[14:32:07] Scanning... (5 devices)
[!] TARGET DETECTED!
    Address: 00:25:DF:A1:B2:C3
    Name: AXON Flex 2
    RSSI: -72 dBm
    Time: 2024-05-10 14:32:10
```

### Output Files

- `detections.log` - All detection events
- Console output - Real-time status

---

## 🔍 How It Works

### Detection Flow

```
1. Scan for BLE devices
   ↓
2. Extract MAC address
   ↓
3. Get first 8 chars (OUI)
   ↓
4. Match against target:
   - 00:25:DF (Axon)
   - 00:25:df (lowercase)
   ↓
5. If match → ALERT!
```

### Signal Range

| RSSI (dBm) | Approx Distance |
|------------|----------------|
| -50 to -60 | Very close (< 3m) |
| -60 to -70 | Close (3-10m) |
| -70 to -80 | Medium (10-20m) |
| -80 to -90 | Far (> 20m) |

---

## 📂 Project Structure

```
jackscanner/
├── jackscanner.py      # Main scanner (Python)
├── axonbleeder.py     # Direct mode variant
├── termux_setup.sh   # Install script
├── README.md          # This file
├── LICENSE            # MIT License
├── android/           # Android app source
│   ├── app/
│   │   ├── build.gradle
│   │   └── src/main/
│   │       ├── AndroidManifest.xml
│   │       ├── java/com/jackscanner/
│   │       └── res/
│   └── build.gradle
└── docs/
    └── SCANNING.md   # Detailed docs
```

---

## 🔐 Permissions

### Android (Required)
- `BLUETOOTH_SCAN`
- `BLUETOOTH_CONNECT`  
- `ACCESS_FINE_LOCATION`
- `POST_NOTIFICATIONS`

### Termux (Optional)
- Bluetooth access
- Vibration

---

## ⚠️ Limitations

- **Android Stock** - BLE scanning may not work on stock Android without root
- **Range** - Limited to Bluetooth range (~10-30m typical)
- **Detection** - Only detects advertising devices
- **Random MAC** - Some devices use random MAC addresses (Android 10+)

---

## 📜 License

MIT License - See [LICENSE](LICENSE)

---

## 🙏 Acknowledgments

- Inspired by [lookout.py](https://github.com/judcrandall/lookout.py)
- Inspired by [PoliceDetector](https://github.com/omtoi101/PoliceDetector)
- DEF CON 31 talks by Null Agent and Sally

---

## ⚡ Quick Start

```bash
# One-liner for Termux
curl -sL https://git.io/jackscan | bash

# Or clone directly
git clone https://github.com/jackscanner/jackscanner.git
cd jackscanner
python jackscanner.py
```

---

<p align="center">
  <sub>For educational and research purposes only.</sub>
</p>