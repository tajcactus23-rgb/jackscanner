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
}