package com.jackscanner.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.jackscanner.data.local.dao.ChatMessageDao
import com.jackscanner.data.local.dao.CommunityDetectionDao
import com.jackscanner.data.local.dao.DetectionDao
import com.jackscanner.data.local.dao.UserProfileDao
import com.jackscanner.data.local.entity.ChatMessageEntity
import com.jackscanner.data.local.entity.CommunityDetectionEntity
import com.jackscanner.data.local.entity.DetectionEntity
import com.jackscanner.data.local.entity.UserProfileEntity

@Database(
    entities = [
        DetectionEntity::class,
        CommunityDetectionEntity::class,
        UserProfileEntity::class,
        ChatMessageEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class BlueMeanieDatabase : RoomDatabase() {
    abstract fun detectionDao(): DetectionDao
    abstract fun communityDetectionDao(): CommunityDetectionDao
    abstract fun userProfileDao(): UserProfileDao
    abstract fun chatMessageDao(): ChatMessageDao

    companion object {
        const val DATABASE_NAME = "bluemeanie_database"
    }
}