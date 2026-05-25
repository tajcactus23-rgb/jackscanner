package com.jackscanner.data.local.dao

import androidx.room.*
import com.jackscanner.data.local.entity.CommunityDetectionEntity
import com.jackscanner.data.local.entity.DetectionEntity
import com.jackscanner.data.local.entity.ChatMessageEntity
import com.jackscanner.data.local.entity.UserProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DetectionDao {
    @Query("SELECT * FROM detections ORDER BY lastSeen DESC")
    fun getAllDetections(): Flow<List<DetectionEntity>>

    @Query("SELECT * FROM detections WHERE id = :id")
    suspend fun getDetectionById(id: String): DetectionEntity?

    @Query("SELECT * FROM detections WHERE macAddress = :macAddress ORDER BY timestamp DESC LIMIT 1")
    suspend fun getDetectionByMac(macAddress: String): DetectionEntity?

    @Query("SELECT * FROM detections WHERE timestamp >= :startTime ORDER BY timestamp DESC")
    fun getDetectionsSince(startTime: Long): Flow<List<DetectionEntity>>

    @Query("SELECT COUNT(*) FROM detections WHERE timestamp >= :startTime")
    suspend fun getDetectionCountSince(startTime: Long): Int

    @Query("SELECT * FROM detections ORDER BY lastSeen DESC LIMIT :limit")
    fun getRecentDetections(limit: Int): Flow<List<DetectionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDetection(detection: DetectionEntity)

    @Update
    suspend fun updateDetection(detection: DetectionEntity)

    @Delete
    suspend fun deleteDetection(detection: DetectionEntity)

    @Query("DELETE FROM detections WHERE timestamp < :olderThan")
    suspend fun deleteOldDetections(olderThan: Long)

    @Query("DELETE FROM detections")
    suspend fun deleteAllDetections()
}

@Dao
interface CommunityDetectionDao {
    @Query("SELECT * FROM community_detections ORDER BY intensity DESC")
    fun getAllCommunityDetections(): Flow<List<CommunityDetectionEntity>>

    @Query("SELECT * FROM community_detections WHERE timeRange = :timeRange ORDER BY intensity DESC")
    fun getCommunityDetectionsByTimeRange(timeRange: String): Flow<List<CommunityDetectionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCommunityDetection(detection: CommunityDetectionEntity)

    @Query("DELETE FROM community_detections WHERE mostRecent < :olderThan")
    suspend fun deleteOldCommunityDetections(olderThan: Long)

    @Query("DELETE FROM community_detections")
    suspend fun deleteAllCommunityDetections()
}

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profile WHERE id = 1")
    fun getUserProfile(): Flow<UserProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserProfile(profile: UserProfileEntity)

    @Update
    suspend fun updateUserProfile(profile: UserProfileEntity)

    @Query("UPDATE user_profile SET detectionCount = detectionCount + 1 WHERE id = 1")
    suspend fun incrementDetectionCount()

    @Query("UPDATE user_profile SET messages = messages + 1 WHERE id = 1")
    suspend fun incrementMessageCount()
}

@Dao
interface ChatMessageDao {
    @Query("SELECT * FROM chat_messages ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentMessages(limit: Int): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity)

    @Query("DELETE FROM chat_messages WHERE timestamp < :olderThan")
    suspend fun deleteOldMessages(olderThan: Long)
}