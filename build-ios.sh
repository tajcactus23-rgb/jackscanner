#!/bin/bash
# Build script for iOS Sideloadly signing
# Usage: ./build-ios.sh

set -e

echo "=== JackScanner iOS Build ==="
echo "Requires: Sideloadly, Xcode"

# Check for Xcode
if ! command -v xcodebuild &> /dev/null; then
    echo "Error: Xcode not found. Install from Mac App Store."
    exit 1
fi

PROJECT_DIR="jackscanner"
OUTPUT_DIR="build-output"

mkdir -p "$OUTPUT_DIR"

echo "[1/2] Building JackScanner..."
cd "$PROJECT_DIR"

# Create generic archive (unsigned - for Sideloadly signing)
xcodebuild -scheme JackScanner \
    -configuration Debug \
    -destination 'generic/platform=iOS' \
    -archivePath "../$OUTPUT_DIR/JackScanner.xcarchive" \
    CODE_SIGN_IDENTITY="" \
    CODE_SIGNING_REQUIRED=NO \
    archive

cd ..

echo "[2/2] Building BlueMeanie..."
cd axonbleeder

xcodebuild -scheme AxonBleeder \
    -configuration Debug \
    -destination 'generic/platform=iOS' \
    -archivePath "../$OUTPUT_DIR/BlueMeanie.xcarchive" \
    CODE_SIGN_IDENTITY="" \
    CODE_SIGNING_REQUIRED=NO \
    archive

echo ""
echo "=== Build Complete ==="
echo "Archives created in: $OUTPUT_DIR/"
echo ""
echo "To install on iOS:"
echo "1. Download Sideloadly: https://sideloadly.io"
echo "2. Connect iPhone to computer"
echo "3. Open Sideloadly, drag .xcarchive file"
echo "4. Enter your Apple ID when prompted"
echo "5. App installs to iPhone!"