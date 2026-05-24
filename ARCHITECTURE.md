# Architecture Documentation

This document describes the architecture and design decisions of the BlueMeanie Android application.

---

## 📐 Architecture Overview

BlueMeanie follows **Clean Architecture** principles with **MVVM** pattern, implemented using modern Android development practices.

```
┌─────────────────────────────────────────────────────────────┐
│                        UI Layer                              │
│  ┌─────────────────────────────────────────────────────┐    │
│  │  Compose Screens, ViewModels, Navigation            │    │
│  └─────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                     Domain Layer                             │
│  ┌─────────────────────────────────────────────────────┐    │
│  │  Use Cases, Repository Interfaces, Domain Models    │    │
│  └─────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                       Data Layer                            │
│  ┌─────────────────────────────────────────────────────┐    │
│  │  Repository Impl, Room DB, DataStore, BLE Service   │    │
│  └─────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────┘
```

---

## 🏗️ Project Structure

```
com.jackscanner/
├── ui/                      # Presentation Layer
│   ├── screens/             # Screen Composables
│   │   ├── home/            # Home screen + ViewModel
│   │   ├── feed/            # Feed screen + ViewModel
│   │   ├── heatmap/         # Heatmap screen + ViewModel
│   │   ├── community/       # Community screen + ViewModel
│   │   ├── scoreboard/      # Scoreboard screen + ViewModel
│   │   ├── settings/         # Settings + Detection Detail
│   │   └── onboarding/       # Onboarding flow
│   ├── components/          # Reusable UI Components
│   ├── theme/              # Theme definitions
│   └── navigation/         # Navigation graph
│
├── domain/                  # Business Logic Layer
│   ├── model/              # Domain models
│   └── repository/         # Repository interfaces
│
├── data/                    # Data Layer
│   ├── local/              # Room database
│   │   ├── dao/           # Data Access Objects
│   │   ├── entity/        # Database entities
│   │   └── database/      # Database class
│   ├── repository/         # Repository implementations
│   └── preferences/        # DataStore preferences
│
├── service/                # BLE Scanning Service
├── di/                     # Hilt dependency injection
└── util/                   # Utilities
```

---

## 📱 UI Layer

### Jetpack Compose
- **Modern declarative UI** using Jetpack Compose
- **Material Design 3** components and theming
- **Type-safe navigation** with Navigation Compose

### Screen Structure
Each screen follows the pattern:
```
ScreenName/
├── ScreenNameScreen.kt     # Composable UI
└── ScreenNameViewModel.kt  # ViewModel
```

### ViewModels
- Extend **HiltViewModel** for DI
- Expose **StateFlow** for UI state
- Handle **business logic** and state management

### State Management
- **Unidirectional data flow** (UDF)
- UI State in **immutable data classes**
- Events flow **up**, state flows **down**

---

## 🎯 Domain Layer

### Domain Models
Pure Kotlin data classes representing business entities:
- `Detection` - Axon device detection
- `UserProfile` - User information
- `CommunityDetection` - Aggregated detection data
- `ScannerSettings` - Configuration

### Repository Interfaces
Define contracts for data access:
```kotlin
interface DetectionRepository {
    fun getAllDetections(): Flow<List<Detection>>
    suspend fun saveDetection(detection: Detection)
    suspend fun getDetectionCountToday(): Int
}
```

---

## 💾 Data Layer

### Room Database
Local persistence using Room:
- **Entities** - Database table representations
- **DAOs** - Data access methods
- **Type converters** - For complex types

### DataStore Preferences
Modern preferences storage:
- **Scanner settings**
- **User preferences**
- **Theme selection**

### Repository Implementations
Concrete implementations of domain interfaces:
- Transform entities to domain models
- Handle data transformations
- Provide Flow-based data access

---

## 🔌 Service Layer

### BleScanService
Foreground service for BLE scanning:
- **@AndroidEntryPoint** for Hilt DI
- **Persistent notification** with actions
- **WakeLock** for background operation
- **Multiple notification channels**

### Bluetooth LE Scanning
- **ScanCallback** for device detection
- **OUI matching** for Axon devices
- **Signal tracking** for RSSI values

---

## 💉 Dependency Injection

### Hilt Setup
- **@HiltAndroidApp** on Application
- **@AndroidEntryPoint** on Activities/Services
- **@HiltViewModel** on ViewModels
- **@Inject** on constructors

### Modules
```kotlin
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(...): BlueMeanieDatabase
}
```

---

## 🎨 Theming

### Custom Theme System
- **10 predefined themes** (BlueMeanie Classic, Carbon, etc.)
- **BlueMeanieColors** data class for colors
- **CompositionLocal** for theme access

### Color Palette
Each theme provides:
- Primary/Secondary colors
- Background/Surface colors
- Text colors (Primary/Secondary/Tertiary)
- Status colors (Active/Warning/Danger)
- Accent/Glow colors

---

## 📊 Navigation

### Screen Routes
```kotlin
object Screen {
    object Home : Screen("home", "Home", Icons.Default.Home)
    object Feed : Screen("feed", "Feed", Icons.Default.List)
    object Heatmap : Screen("heatmap", "Heatmap", Icons.Default.Map)
    object Community : Screen("community", "Community", Icons.Default.People)
    object Scoreboard : Screen("scoreboard", "Scoreboard", Icons.Default.Star)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
}
```

### Navigation Graph
- Bottom navigation for main screens
- Full-screen for onboarding
- Deep linking support for notifications

---

## 🔄 Data Flow

### Detection Flow
```
1. BleScanService detects Axon device
         ↓
2. Create Detection domain model
         ↓
3. Save to DetectionRepository
         ↓
4. ViewModel observes Flow
         ↓
5. UI updates reactively
```

### User Actions Flow
```
1. User taps button
         ↓
2. Event sent to ViewModel
         ↓
3. ViewModel updates state
         ↓
4. UI recomposes with new state
```

---

## 🧪 Testing Strategy

### Unit Tests
- ViewModel logic tests
- Repository tests
- Use case tests

### Integration Tests
- Database tests
- Service tests

### UI Tests
- Compose UI tests
- Navigation tests

---

## 📦 Build Configuration

### Gradle Setup
- **Kotlin DSL** for build scripts
- **Version catalog** for dependencies
- **Build flavors** for different variants

### Dependencies
- Compose BOM for UI
- Hilt for DI
- Room for database
- DataStore for preferences
- Navigation Compose

---

## 🔐 Security Considerations

### Data Protection
- **No sensitive data storage** in plain text
- **Local-only** data by default
- **Anonymous mode** available

### Privacy
- **Minimal permissions** required
- **Clear explanations** in onboarding
- **User control** over all features

---

## 🚀 Performance

### Optimizations
- **Lazy loading** for lists
- **Efficient recomposition** with stable keys
- **Background processing** with Coroutines
- **Database indexing** for fast queries

### Battery Efficiency
- **Configurable scan modes** (Low Power/Balanced/Low Latency)
- **WakeLock management** for scanning
- **Foreground service** for reliability

---

## 🔧 Future Improvements

### Planned Enhancements
- Google Maps SDK integration
- Cloud backup (premium)
- Advanced analytics
- Widget support

### Technical Debt
- Additional unit tests
- Performance profiling
- Accessibility audit

---

*Documentation generated: May 24, 2026*