package com.jackscanner.data.repository

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.jackscanner.data.local.dao.CommunityDetectionDao
import com.jackscanner.data.local.dao.DetectionDao
import com.jackscanner.data.local.dao.UserProfileDao
import com.jackscanner.data.local.entity.CommunityDetectionEntity
import com.jackscanner.data.local.entity.DetectionEntity
import com.jackscanner.data.local.entity.UserProfileEntity
import com.jackscanner.domain.model.CommunityDetection
import com.jackscanner.domain.model.Detection
import com.jackscanner.domain.model.UserProfile
import com.jackscanner.domain.model.UserRank
import com.jackscanner.domain.repository.CommunityRepository
import com.jackscanner.domain.repository.DetectionRepository
import com.jackscanner.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DetectionRepositoryImpl @Inject constructor(
    private val detectionDao: DetectionDao,
    private val gson: Gson
) : DetectionRepository {

    override fun getAllDetections(): Flow<List<Detection>> {
        return detectionDao.getAllDetections().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getRecentDetections(limit: Int): Flow<List<Detection>> {
        return detectionDao.getRecentDetections(limit).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getDetectionsSince(startTime: Long): Flow<List<Detection>> {
        return detectionDao.getDetectionsSince(startTime).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getDetectionById(id: String): Detection? {
        return detectionDao.getDetectionById(id)?.toDomain()
    }

    override suspend fun saveDetection(detection: Detection) {
        detectionDao.insertDetection(detection.toEntity())
    }

    override suspend fun updateDetection(detection: Detection) {
        detectionDao.updateDetection(detection.toEntity())
    }

    override suspend fun deleteDetection(detection: Detection) {
        detectionDao.deleteDetection(detection.toEntity())
    }

    override suspend fun getDetectionCountToday(): Int {
        val startOfDay = getStartOfDay()
        return detectionDao.getDetectionCountSince(startOfDay)
    }

    override suspend fun clearOldDetections(daysOld: Int) {
        val cutoffTime = System.currentTimeMillis() - (daysOld * 24 * 60 * 60 * 1000L)
        detectionDao.deleteOldDetections(cutoffTime)
    }

    private fun getStartOfDay(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    private fun DetectionEntity.toDomain(): Detection {
        return Detection(
            id = id,
            macAddress = macAddress,
            deviceName = deviceName,
            rssi = rssi,
            timestamp = timestamp,
            firstSeen = firstSeen,
            lastSeen = lastSeen,
            observedSignals = observedSignals,
            manufacturerData = manufacturerData,
            advertisementData = advertisementData,
            serviceUuids = gson.fromJson(serviceUuids, object : TypeToken<List<String>>() {}.type) ?: emptyList(),
            latitude = latitude,
            longitude = longitude,
            isAnonymous = isAnonymous
        )
    }

    private fun Detection.toEntity(): DetectionEntity {
        return DetectionEntity(
            id = id,
            macAddress = macAddress,
            deviceName = deviceName,
            rssi = rssi,
            timestamp = timestamp,
            firstSeen = firstSeen,
            lastSeen = lastSeen,
            observedSignals = observedSignals,
            manufacturerData = manufacturerData,
            advertisementData = advertisementData,
            serviceUuids = gson.toJson(serviceUuids),
            latitude = latitude,
            longitude = longitude,
            isAnonymous = isAnonymous
        )
    }
}

@Singleton
class CommunityRepositoryImpl @Inject constructor(
    private val communityDetectionDao: CommunityDetectionDao
) : CommunityRepository {

    override fun getCommunityDetections(): Flow<List<CommunityDetection>> {
        return communityDetectionDao.getAllCommunityDetections().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getCommunityDetectionsByTimeRange(timeRange: String): Flow<List<CommunityDetection>> {
        return communityDetectionDao.getCommunityDetectionsByTimeRange(timeRange).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun saveCommunityDetection(detection: CommunityDetection) {
        communityDetectionDao.insertCommunityDetection(detection.toEntity())
    }

    override suspend fun uploadDetection(detection: Detection) {
        // In a real app, this would upload to a backend server
        // For now, we just save locally
        detection.latitude?.let { lat ->
            detection.longitude?.let { lng ->
                val communityDetection = CommunityDetection(
                    latitude = lat,
                    longitude = lng,
                    intensity = 1f,
                    detectionCount = 1,
                    timeRange = com.jackscanner.domain.model.TimeRange.ONE_HOUR,
                    firstRecorded = System.currentTimeMillis(),
                    mostRecent = System.currentTimeMillis()
                )
                saveCommunityDetection(communityDetection)
            }
        }
    }

    private fun CommunityDetectionEntity.toDomain(): CommunityDetection {
        return CommunityDetection(
            latitude = latitude,
            longitude = longitude,
            intensity = intensity,
            detectionCount = detectionCount,
            timeRange = com.jackscanner.domain.model.TimeRange.valueOf(timeRange),
            firstRecorded = firstRecorded,
            mostRecent = mostRecent
        )
    }

    private fun CommunityDetection.toEntity(): CommunityDetectionEntity {
        return CommunityDetectionEntity(
            latitude = latitude,
            longitude = longitude,
            intensity = intensity,
            detectionCount = detectionCount,
            timeRange = timeRange.name,
            firstRecorded = firstRecorded,
            mostRecent = mostRecent
        )
    }
}

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val userProfileDao: UserProfileDao
) : UserRepository {

    override fun getUserProfile(): Flow<UserProfile?> {
        return userProfileDao.getUserProfile().map { entity ->
            entity?.toDomain()
        }
    }

    override suspend fun saveUserProfile(profile: UserProfile) {
        userProfileDao.insertUserProfile(profile.toEntity())
    }

    override suspend fun updateUserProfile(profile: UserProfile) {
        userProfileDao.updateUserProfile(profile.toEntity())
    }

    override suspend fun updateUsername(username: String) {
        val currentProfile = userProfileDao.getUserProfile()
        // This would need to be done differently with Flow
    }

    override suspend fun updateDetectionCount(count: Int) {
        userProfileDao.incrementDetectionCount()
    }

    private fun UserProfileEntity.toDomain(): UserProfile {
        return UserProfile(
            username = username,
            detectionCount = detectionCount,
            messages = messages,
            rank = UserRank.valueOf(rank),
            reputationScore = reputationScore,
            isAnonymous = isAnonymous,
            autoRotateUsername = autoRotateUsername,
            avatarUrl = avatarUrl
        )
    }

    private fun UserProfile.toEntity(): UserProfileEntity {
        return UserProfileEntity(
            username = username,
            detectionCount = detectionCount,
            messages = messages,
            rank = rank.name,
            reputationScore = reputationScore,
            isAnonymous = isAnonymous,
            autoRotateUsername = autoRotateUsername,
            avatarUrl = avatarUrl
        )
    }
}