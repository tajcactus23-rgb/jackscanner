package com.jackscanner.domain.model

import java.util.UUID

/**
 * Represents a detected Axon device
 */
data class Detection(
    val id: String = UUID.randomUUID().toString(),
    val macAddress: String,
    val deviceName: String?,
    val rssi: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val firstSeen: Long = System.currentTimeMillis(),
    val lastSeen: Long = System.currentTimeMillis(),
    val observedSignals: Int = 1,
    val manufacturerData: String? = null,
    val advertisementData: String? = null,
    val serviceUuids: List<String> = emptyList(),
    val latitude: Double? = null,
    val longitude: Double? = null,
    val isAnonymous: Boolean = true
) {
    val signalStrength: SignalStrength
        get() = when {
            rssi >= -50 -> SignalStrength.EXCELLENT
            rssi >= -60 -> SignalStrength.GOOD
            rssi >= -70 -> SignalStrength.FAIR
            rssi >= -80 -> SignalStrength.WEAK
            else -> SignalStrength.POOR
        }
}

enum class SignalStrength {
    EXCELLENT,
    GOOD,
    FAIR,
    WEAK,
    POOR
}

/**
 * Community detection data for heatmap
 */
data class CommunityDetection(
    val latitude: Double,
    val longitude: Double,
    val intensity: Float,
    val detectionCount: Int,
    val timeRange: TimeRange,
    val firstRecorded: Long,
    val mostRecent: Long
)

enum class TimeRange {
    ONE_HOUR,
    THREE_HOURS,
    TWELVE_HOURS,
    TWENTY_FOUR_HOURS,
    THREE_DAYS,
    SEVEN_DAYS,
    FOURTEEN_DAYS,
    THIRTY_DAYS,
    ONE_YEAR,
    ALL
}

/**
 * Scanner status
 */
enum class ScannerStatus {
    IDLE,
    SCANNING,
    PAUSED,
    ERROR
}

/**
 * User profile for community
 */
data class UserProfile(
    val username: String,
    val detectionCount: Int = 0,
    val messages: Int = 0,
    val rank: UserRank = UserRank.OBSERVER,
    val reputationScore: Int = 0,
    val isAnonymous: Boolean = true,
    val autoRotateUsername: Boolean = false,
    val avatarUrl: String? = null
)

enum class UserRank(val displayName: String, val minScore: Int) {
    OBSERVER("Observer", 0),
    SCOUT("Scout", 10),
    TRACKER("Tracker", 50),
    ANALYST("Analyst", 100),
    VETERAN("Veteran", 500),
    ELITE("Elite", 1000),
    LEGEND("Legend", 5000)
}

/**
 * Theme options
 */
enum class AppTheme(val displayName: String) {
    BLUE_MEANIE_CLASSIC("BlueMeanie Classic"),
    CARBON("Carbon"),
    TITANIUM("Titanium"),
    AURORA("Aurora"),
    MONOLITH("Monolith"),
    ARCTIC("Arctic"),
    MIDNIGHT("Midnight"),
    QUANTUM("Quantum"),
    NOVA("Nova"),
    GLASS("Glass"),
    SIREN("Siren")
}

/**
 * Scanner settings
 */
data class ScannerSettings(
    val autoStartOnBoot: Boolean = false,
    val alertSound: Boolean = true,
    val alertVibration: Boolean = true,
    val backgroundScanning: Boolean = true,
    val scanMode: ScanMode = ScanMode.LOW_LATENCY,
    val detectionThreshold: Int = -80
)

enum class ScanMode {
    LOW_POWER,
    BALANCED,
    LOW_LATENCY
}

/**
 * Detection notice for UI display
 */
object DetectionNotice {
    const val NOTICE_TEXT = """Detection Notice

This detection represents a Bluetooth Low Energy advertisement received by your device and matched against BlueMeanie detection criteria.

Detection locations represent the position of the detecting device when the advertisement was received and should not be interpreted as the exact position of the detected device.

Bluetooth reception range varies significantly depending on hardware, obstacles, interference and environmental conditions."""
}