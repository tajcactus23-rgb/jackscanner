# JackScanner APK Release Candidate

## Build Status
- **Build**: SUCCESS ✓
- **GitHub Actions Run**: 26778182537
- **Artifact**: jackscanner-apk (36.7 MB)
- **Branch**: fix/build-improvements
- **Build Time**: ~4 minutes 28 seconds

## APK Download
Download from GitHub Actions artifacts:
```
https://github.com/tajcactus23-rgb/jackscanner/actions/runs/26778182537
```

## What's Included

### BLE Axon Scanner (Main Feature)
- Radar sweep animation UI
- BLE device detection with RSSI
- Permission handling (step-by-step)
- Device list with signal strength
- Connect/Disconnect functionality

### BTC Puzzle Tracker
- 71 puzzles with real key ranges (2^20 to 2^91)
- Wallet address input
- Balance & transaction display
- Raw logs with timestamp, balance, tx count
- Search methods: Sequential, Random, Fibonacci, Binary Search
- Audio/vibration alerts on balance found
- Range sliders for custom start/end keys

### Theme
- BlueMeanie dark theme (neon blue/green)
- User's custom icon applied to all densities

## Test Checklist

### Scanning
- [ ] Scan starts successfully
- [ ] Scan stops successfully
- [ ] Devices appear in list
- [ ] RSSI updates in real-time

### Permissions
- [ ] First launch permission flow
- [ ] Denied permissions handling
- [ ] Re-granted permissions work

### BTC Puzzle
- [ ] Puzzle selector scrolls (P1-P71)
- [ ] Balance displays for addresses
- [ ] Raw logs update with checks
- [ ] Start/Stop buttons work
- [ ] Audio plays on positive balance

### UI/UX
- [ ] All screens load
- [ ] Navigation works
- [ ] Settings persist

### Stability
- [ ] No crashes on launch
- [ ] No ANRs
- [ ] No frozen screens

## Known Limitations

1. **BTC Puzzle is SIMULATION** - The app does not actually connect to Bitcoin blockchain. Balance checking is simulated.

2. **Real BTC Puzzle Integration Would Require**:
   - Blockstream API or similar BTC blockchain API
   - Real puzzle addresses from btcpuzzle.info
   - Actual private key checking (computationally infeasible on mobile)

3. **BLE Hardware Validation Required** - Physical Axon device testing needed to verify:
   - Real device detection range
   - GATT characteristic reading
   - Actual connection stability

## Recent Commits
```
268c42e Add AI-generated UI mockups
b8e1bde fix: Complete puzzle list 1-71 with correct key ranges
7ee6ce4 fix: Update puzzle list with real BTC puzzle numbers
7ed5eda fix: Use user's Imgur icon and add icon for all densities
9102ab5 fix: Puzzle BTC - wallet input, balance display, raw logs, audio cues
```

## Next Steps
1. Test APK on physical device
2. Verify BLE scanning detects Axon devices
3. Validate puzzle display works
4. Report any issues for fixes

---
*Generated: 2026-06-01*