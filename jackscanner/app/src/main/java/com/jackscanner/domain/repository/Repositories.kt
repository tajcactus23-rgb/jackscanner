package com.jackscanner.domain.repository

import com.jackscanner.domain.model.Detection
import com.jackscanner.domain.model.CommunityDetection
import com.jackscanner.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface DetectionRepository {
    fun getAllDetections(): Flow<List<Detection>>
    fun getRecentDetections(limit: Int): Flow<List<Detection>>
    fun getDetectionsSince(startTime: Long): Flow<List<Detection>>
    suspend fun getDetectionById(id: String): Detection?
    suspend fun saveDetection(detection: Detection)
    suspend fun updateDetection(detection: Detection)
    suspend fun deleteDetection(detection: Detection)
    suspend fun getDetectionCountToday(): Int
    suspend fun clearOldDetections(daysOld: Int = 30)
}

interface CommunityRepository {
    fun getCommunityDetections(): Flow<List<CommunityDetection>>
    fun getCommunityDetectionsByTimeRange(timeRange: String): Flow<List<CommunityDetection>>
    suspend fun saveCommunityDetection(detection: CommunityDetection)
    suspend fun uploadDetection(detection: Detection)
}

interface UserRepository {
    fun getUserProfile(): Flow<UserProfile?>
    suspend fun saveUserProfile(profile: UserProfile)
    suspend fun updateUserProfile(profile: UserProfile)
    suspend fun updateUsername(username: String)
    suspend fun updateDetectionCount(count: Int)
}