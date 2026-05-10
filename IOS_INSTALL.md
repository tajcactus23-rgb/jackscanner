# BlueMeanie - iOS Build Guide

## Option 1: Sideloadly (FREE - Recommended)

### Step 1: Get Xcode (Mac only)
- Download from Mac App Store (free)

### Step 2: Setup iOS Simulator  
- Open Xcode > Preferences > Accounts
- Click "+" to add your Apple ID
- Download simulators: Window > Device Manager > iOS 17.0

### Step 3: Build & Install
```bash
# Clone this repo
git clone https://github.com/tajcactus23-rgb/jackscanner.git
cd jackscanner

# Open in Xcode (double-click jackscanner.xcworkspace)
# Or run build script:
chmod +x ../build-ios.sh
../build-ios.sh
```

### Step 4: Install via Sideloadly
1. Download Sideloadly: https://sideloadly.io
2. Connect iPhone via USB
3. Open Sideloadly
4. Select JackScanner.ipa or BlueMeanie.ipa
5. Enter Apple ID when prompted
6. App installs!

---

## Option 2: AltStore (FREE)

1. Download AltStore: https://altstore.io
2. Install AltServer on your Mac
3. Put iPhone on same network
4. AltServer will inject app

---

## Option 3: Web-based Install

If you can't use a Mac, try these services:

### iOSAppSign (paid, $5-10)
- Upload .ipa, get download link
- Uses their Apple Developer cert

### AppBox (freemium)
- https://installiosapps.com
- Free tier available

---

## Build Outputs

| App | File | Description |
|-----|------|-------------|
| BlueMeanie | app-release.ipa | Blue themed scanner |
| JackScanner | app-release.ipa | Original scanner |

---

## Troubleshooting

**"Unable to install"**
- iOS 17+ requires Apple ID with no 2FA or use 2-step verification
- Some accounts blocked - try different Apple ID

**"Expired" after 7 days**
- Sideloadly certs expire after 7 days
- Re-install via Sideloadly to refresh

**Can't build locally?**
- I can provide a pre-built unsigned .IPA
- You'll need to sign via Sideloadly