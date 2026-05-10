# BlueMeanie - iOS Build Guide

## Why No GitHub Actions Build?

GitHub's macOS runners don't include **Xcode with iOS SDK**. Building iOS apps requires:
- Full Xcode installation
- iOS SDK (~15GB)
- Apple signing certificates

These are only available on your local Mac.

---

## Option 1: BUILD LOCAL (Recommended)

### Step 1: Get Xcode (Mac only)
```bash
# Via Mac App Store (free):
# Search "Xcode" in Mac App Store
```

### Step 2: Build the App
```bash
# Clone this repo
git clone https://github.com/tajcactus23-rgb/jackscanner.git
cd jackscanner

# Double-click jackscanner.xcworkspace to open in Xcode
# Or build from command line:
xcodebuild -workspace jackscanner.xcworkspace \
  -scheme JackScanner \
  -configuration Debug \
  -destination generic/platform=iOS \
  CODE_SIGN_IDENTITY="" \
  CODE_SIGNING_REQUIRED=NO \
  build
```

### Step 3: Install via Sideloadly (FREE - No membership!)

1. **Download Sideloadly**: https://sideloadly.io
2. Connect iPhone via USB
3. Open Sideloadly
4. Select the built .ipa file
5. Enter your Apple ID when prompted
6. App auto-installs to iPhone!

---

## Option 2: AltStore (FREE)

1. Download **AltServer**: https://altstore.io
2. Install AltServer on your Mac
3. Connect iPhone on same WiFi
4. App installs via network injection!