package com.jackscanner.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.jackscanner.domain.model.AppTheme
import com.jackscanner.domain.model.ScannerSettings
import com.jackscanner.domain.model.ScanMode
import com.jackscanner.domain.model.UserProfile
import com.jackscanner.domain.model.UserRank
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "bluemeanie_prefs")

@Singleton
class PreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore

    // Preference Keys
    private object PreferenceKeys {
        // Onboarding
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val USERNAME = stringPreferencesKey("username")
        val IS_ANONYMOUS = booleanPreferencesKey("is_anonymous")
        val AUTO_ROTATE_USERNAME = booleanPreferencesKey("auto_rotate_username")
        val OPTIONAL_LOCATION = booleanPreferencesKey("optional_location")
        
        // Scanner
        val AUTO_START_ON_BOOT = booleanPreferencesKey("auto_start_on_boot")
        val ALERT_SOUND = booleanPreferencesKey("alert_sound")
        val ALERT_VIBRATION = booleanPreferencesKey("alert_vibration")
        val BACKGROUND_SCANNING = booleanPreferencesKey("background_scanning")
        val SCAN_MODE = stringPreferencesKey("scan_mode")
        val DETECTION_THRESHOLD = intPreferencesKey("detection_threshold")
        
        // Theme
        val SELECTED_THEME = stringPreferencesKey("selected_theme")
        
        // Notifications
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val COMMUNITY_NOTIFICATIONS = booleanPreferencesKey("community_notifications")
        
        // Privacy
        val PRIVATE_MODE = booleanPreferencesKey("private_mode")
        val LOCATION_SHARING = booleanPreferencesKey("location_sharing")
        
        // Heatmap
        val HEATMAP_TIME_RANGE = stringPreferencesKey("heatmap_time_range")
        val HEATMAP_FOLLOW_USER = booleanPreferencesKey("heatmap_follow_user")
        
        // Premium
        val IS_PREMIUM = booleanPreferencesKey("is_premium")
        
        // Community
        val COMMUNITY_ENABLED = booleanPreferencesKey("community_enabled")
        val SHOW_PROFILE = booleanPreferencesKey("show_profile")

        // Dev Features (only dev can control these)
        val IS_DEV_ACCOUNT = booleanPreferencesKey("is_dev_account")
        val DEV_BADGE_ENABLED = booleanPreferencesKey("dev_badge_enabled")
        val COLORED_USERNAMES_ENABLED = booleanPreferencesKey("colored_usernames_enabled")
        val CHAT_BOUNDARIES_ENABLED = booleanPreferencesKey("chat_boundaries_enabled")
        val SPECIAL_FONTS_ENABLED = booleanPreferencesKey("special_fonts_enabled")
        
        // Dev Feature Flags - Control what's available for all users
        val FLAG_HEATMAP_ENABLED = booleanPreferencesKey("flag_heatmap_enabled")
        val FLAG_COMMUNITY_ENABLED = booleanPreferencesKey("flag_community_enabled")
        val FLAG_SCOREBOARD_ENABLED = booleanPreferencesKey("flag_scoreboard_enabled")
        val FLAG_LEADERBOARD_GLOBAL = booleanPreferencesKey("flag_leaderboard_global")
        val FLAG_DETECTION_ALERTS = booleanPreferencesKey("flag_detection_alerts")
        val FLAG_LOCATION_TRACKING = booleanPreferencesKey("flag_location_tracking")
        val FLAG_PREMIUM_FEATURES = booleanPreferencesKey("flag_premium_features")
    }

    // Onboarding State
    val onboardingCompleted: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[PreferenceKeys.ONBOARDING_COMPLETED] ?: false
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        dataStore.edit { prefs ->
            prefs[PreferenceKeys.ONBOARDING_COMPLETED] = completed
        }
    }

    // User Profile
    data class UserProfileData(
        val username: String,
        val isAnonymous: Boolean,
        val autoRotateUsername: Boolean,
        val optionalLocation: Boolean
    )

    val userProfile: Flow<UserProfileData> = dataStore.data.map { prefs ->
        UserProfileData(
            username = prefs[PreferenceKeys.USERNAME] ?: "Anonymous",
            isAnonymous = prefs[PreferenceKeys.IS_ANONYMOUS] ?: true,
            autoRotateUsername = prefs[PreferenceKeys.AUTO_ROTATE_USERNAME] ?: false,
            optionalLocation = prefs[PreferenceKeys.OPTIONAL_LOCATION] ?: false
        )
    }

    suspend fun setUserProfile(username: String, isAnonymous: Boolean, autoRotate: Boolean, location: Boolean) {
        dataStore.edit { prefs ->
            prefs[PreferenceKeys.USERNAME] = username
            prefs[PreferenceKeys.IS_ANONYMOUS] = isAnonymous
            prefs[PreferenceKeys.AUTO_ROTATE_USERNAME] = autoRotate
            prefs[PreferenceKeys.OPTIONAL_LOCATION] = location
        }
    }

    // Scanner Settings
    val scannerSettings: Flow<ScannerSettings> = dataStore.data.map { prefs ->
        ScannerSettings(
            autoStartOnBoot = prefs[PreferenceKeys.AUTO_START_ON_BOOT] ?: false,
            alertSound = prefs[PreferenceKeys.ALERT_SOUND] ?: true,
            alertVibration = prefs[PreferenceKeys.ALERT_VIBRATION] ?: true,
            backgroundScanning = prefs[PreferenceKeys.BACKGROUND_SCANNING] ?: true,
            scanMode = ScanMode.valueOf(prefs[PreferenceKeys.SCAN_MODE] ?: ScanMode.LOW_LATENCY.name),
            detectionThreshold = prefs[PreferenceKeys.DETECTION_THRESHOLD] ?: -80
        )
    }

    suspend fun updateScannerSettings(settings: ScannerSettings) {
        dataStore.edit { prefs ->
            prefs[PreferenceKeys.AUTO_START_ON_BOOT] = settings.autoStartOnBoot
            prefs[PreferenceKeys.ALERT_SOUND] = settings.alertSound
            prefs[PreferenceKeys.ALERT_VIBRATION] = settings.alertVibration
            prefs[PreferenceKeys.BACKGROUND_SCANNING] = settings.backgroundScanning
            prefs[PreferenceKeys.SCAN_MODE] = settings.scanMode.name
            prefs[PreferenceKeys.DETECTION_THRESHOLD] = settings.detectionThreshold
        }
    }

    // Theme
    val selectedTheme: Flow<AppTheme> = dataStore.data.map { prefs ->
        AppTheme.valueOf(prefs[PreferenceKeys.SELECTED_THEME] ?: AppTheme.BLUE_MEANIE_CLASSIC.name)
    }

    suspend fun setTheme(theme: AppTheme) {
        dataStore.edit { prefs ->
            prefs[PreferenceKeys.SELECTED_THEME] = theme.name
        }
    }

    // Notifications
    val notificationsEnabled: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[PreferenceKeys.NOTIFICATIONS_ENABLED] ?: true
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[PreferenceKeys.NOTIFICATIONS_ENABLED] = enabled
        }
    }

    val communityNotifications: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[PreferenceKeys.COMMUNITY_NOTIFICATIONS] ?: true
    }

    suspend fun setCommunityNotifications(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[PreferenceKeys.COMMUNITY_NOTIFICATIONS] = enabled
        }
    }

    // Privacy
    val privateMode: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[PreferenceKeys.PRIVATE_MODE] ?: true
    }

    suspend fun setPrivateMode(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[PreferenceKeys.PRIVATE_MODE] = enabled
        }
    }

    val locationSharing: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[PreferenceKeys.LOCATION_SHARING] ?: false
    }

    suspend fun setLocationSharing(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[PreferenceKeys.LOCATION_SHARING] = enabled
        }
    }

    // Heatmap
    val heatmapTimeRange: Flow<String> = dataStore.data.map { prefs ->
        prefs[PreferenceKeys.HEATMAP_TIME_RANGE] ?: "TWENTY_FOUR_HOURS"
    }

    suspend fun setHeatmapTimeRange(timeRange: String) {
        dataStore.edit { prefs ->
            prefs[PreferenceKeys.HEATMAP_TIME_RANGE] = timeRange
        }
    }

    // Premium
    val isPremium: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[PreferenceKeys.IS_PREMIUM] ?: false
    }

    suspend fun setPremium(isPremium: Boolean) {
        dataStore.edit { prefs ->
            prefs[PreferenceKeys.IS_PREMIUM] = isPremium
        }
    }

    // Community
    val communityEnabled: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[PreferenceKeys.COMMUNITY_ENABLED] ?: true
    }

    suspend fun setCommunityEnabled(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[PreferenceKeys.COMMUNITY_ENABLED] = enabled
        }
    }

    // ========== DEV FEATURES (Only dev can control these) ==========
    
    // Dev Account Status
    val isDevAccount: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[PreferenceKeys.IS_DEV_ACCOUNT] ?: false
    }

    suspend fun setDevAccount(isDev: Boolean) {
        dataStore.edit { prefs ->
            prefs[PreferenceKeys.IS_DEV_ACCOUNT] = isDev
        }
    }

    // Dev Badge Feature
    val devBadgeEnabled: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[PreferenceKeys.DEV_BADGE_ENABLED] ?: true
    }

    suspend fun setDevBadgeEnabled(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[PreferenceKeys.DEV_BADGE_ENABLED] = enabled
        }
    }

    // Colored Usernames Feature
    val coloredUsernamesEnabled: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[PreferenceKeys.COLORED_USERNAMES_ENABLED] ?: true
    }

    suspend fun setColoredUsernamesEnabled(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[PreferenceKeys.COLORED_USERNAMES_ENABLED] = enabled
        }
    }

    // Chat Boundaries Feature
    val chatBoundariesEnabled: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[PreferenceKeys.CHAT_BOUNDARIES_ENABLED] ?: true
    }

    suspend fun setChatBoundariesEnabled(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[PreferenceKeys.CHAT_BOUNDARIES_ENABLED] = enabled
        }
    }

    // Special Fonts Feature
    val specialFontsEnabled: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[PreferenceKeys.SPECIAL_FONTS_ENABLED] ?: true
    }

    suspend fun setSpecialFontsEnabled(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[PreferenceKeys.SPECIAL_FONTS_ENABLED] = enabled
        }
    }

    // ========== DEV FEATURE FLAGS (Control what's available for all users) ==========
    
    // Heatmap Feature Flag
    val flagHeatmapEnabled: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[PreferenceKeys.FLAG_HEATMAP_ENABLED] ?: true
    }

    suspend fun setFlagHeatmapEnabled(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[PreferenceKeys.FLAG_HEATMAP_ENABLED] = enabled
        }
    }

    // Community Feature Flag
    val flagCommunityEnabled: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[PreferenceKeys.FLAG_COMMUNITY_ENABLED] ?: true
    }

    suspend fun setFlagCommunityEnabled(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[PreferenceKeys.FLAG_COMMUNITY_ENABLED] = enabled
        }
    }

    // Scoreboard Feature Flag
    val flagScoreboardEnabled: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[PreferenceKeys.FLAG_SCOREBOARD_ENABLED] ?: true
    }

    suspend fun setFlagScoreboardEnabled(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[PreferenceKeys.FLAG_SCOREBOARD_ENABLED] = enabled
        }
    }

    // Global Leaderboard Feature Flag
    val flagLeaderboardGlobal: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[PreferenceKeys.FLAG_LEADERBOARD_GLOBAL] ?: false
    }

    suspend fun setFlagLeaderboardGlobal(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[PreferenceKeys.FLAG_LEADERBOARD_GLOBAL] = enabled
        }
    }

    // Detection Alerts Feature Flag
    val flagDetectionAlerts: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[PreferenceKeys.FLAG_DETECTION_ALERTS] ?: true
    }

    suspend fun setFlagDetectionAlerts(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[PreferenceKeys.FLAG_DETECTION_ALERTS] = enabled
        }
    }

    // Location Tracking Feature Flag
    val flagLocationTracking: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[PreferenceKeys.FLAG_LOCATION_TRACKING] ?: false
    }

    suspend fun setFlagLocationTracking(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[PreferenceKeys.FLAG_LOCATION_TRACKING] = enabled
        }
    }

    // Premium Features Feature Flag
    val flagPremiumFeatures: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[PreferenceKeys.FLAG_PREMIUM_FEATURES] ?: false
    }

    suspend fun setFlagPremiumFeatures(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[PreferenceKeys.FLAG_PREMIUM_FEATURES] = enabled
        }
    }
}