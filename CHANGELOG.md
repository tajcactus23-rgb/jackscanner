# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [2.0.0] - 2026-05-24

### 🎉 Major Release

This is a complete rewrite of the application with a new modern UI and architecture.

### Added

#### Architecture
- **MVVM Architecture** - Clean separation of concerns with ViewModels and repositories
- **Hilt Dependency Injection** - Full DI integration for better testability
- **Room Database** - Local persistence for detection history
- **DataStore Preferences** - Modern preferences storage
- **Navigation Compose** - Type-safe navigation
- **Jetpack Compose UI** - Modern declarative UI toolkit

#### Screens
- **Home Screen** - Animated radar display, stats, quick controls
- **Feed Screen** - Detection history with time filtering
- **Heatmap Screen** - Community detection visualization
- **Community Screen** - Anonymous chat with other users
- **Scoreboard Screen** - Leaderboard with 7 ranks
- **Settings Screen** - Full customization options
- **Detection Detail Screen** - Expanded technical details
- **Onboarding Screen** - Step-by-step setup flow

#### Features
- **10 Themes** - BlueMeanie Classic, Carbon, Titanium, Aurora, Monolith, Arctic, Midnight, Quantum, Nova, Glass
- **Persistent Foreground Service** - Continuous background scanning
- **Action Notifications** - Heatmap and Feed quick actions
- **Anonymous Mode** - Privacy-first design
- **Auto-rotate Username** - Rotate identity every 24 hours
- **Scan Mode Selection** - Low Power, Balanced, Low Latency

#### UI Components
- **GlassCard** - Modern card component with subtle borders
- **RadarAnimation** - Animated radar visualization
- **StatCard** - Statistics display cards
- **DetectionNotice** - Legal disclaimer for detections
- **StatusBadge** - Status indicator component

### Changed

#### UI/UX
- **Complete UI Overhaul** - From XML layouts to Jetpack Compose
- **Dark Metallic Theme** - Professional appearance with glass effects
- **Bottom Navigation** - 6 tabs for easy navigation
- **Rounded Corners** - Modern 20dp corner radius

#### Service
- **Enhanced BleScanService** - Hilt integration
- **Multiple Notification Channels** - Scanning, Alerts, Community
- **Pause/Resume Support** - Better service control
- **Detection Tracking** - Track signals per device

### Fixed
- Permission handling improvements
- Better error handling in BLE scanning
- Notification stability improvements

### Security
- Anonymous mode by default
- No data sharing unless explicitly enabled
- Clear permission explanations in onboarding

---

## [1.0.0] - Previous Release

### Initial Release
- Basic BLE scanning functionality
- OUI-based detection (00:25:DF)
- Simple notification alerts
- Single-screen interface

---

## [Unreleased]

### Planned
- Google Maps SDK integration for heatmap
- Cloud backup for premium users
- Advanced detection filters
- Trend analytics
- Historical heatmap playback