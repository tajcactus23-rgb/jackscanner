package com.jackscanner.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "detections")
data class DetectionEntity(
    @PrimaryKey
    val id: String,
    val macAddress: String,
    val deviceName: String?,
    val rssi: Int,
    val timestamp: Long,
    val firstSeen: Long,
    val lastSeen: Long,
    val observedSignals: Int,
    val manufacturerData: String?,
    val advertisementData: String?,
    val serviceUuids: String, // JSON serialized list
    val latitude: Double?,
    val longitude: Double?,
    val isAnonymous: Boolean
)

@Entity(tableName = "community_detections")
data class CommunityDetectionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val latitude: Double,
    val longitude: Double,
    val intensity: Float,
    val detectionCount: Int,
    val timeRange: String,
    val firstRecorded: Long,
    val mostRecent: Long
)

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey
    val id: Int = 1, // Single user profile
    val username: String,
    val detectionCount: Int,
    val messages: Int,
    val rank: String,
    val reputationScore: Int,
    val isAnonymous: Boolean,
    val autoRotateUsername: Boolean,
    val avatarUrl: String?
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey
    val id: String,
    val username: String,
    val message: String,
    val timestamp: Long,
    val isAnonymous: Boolean,
    val reactions: String // JSON serialized map
)