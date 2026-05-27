package com.jackscanner.di

import android.content.Context
import androidx.room.Room
import com.jackscanner.data.local.dao.ChatMessageDao
import com.jackscanner.data.local.dao.CommunityDetectionDao
import com.jackscanner.data.local.dao.DetectionDao
import com.jackscanner.data.local.dao.UserProfileDao
import com.jackscanner.data.local.database.BlueMeanieDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): BlueMeanieDatabase {
        return Room.databaseBuilder(
            context,
            BlueMeanieDatabase::class.java,
            BlueMeanieDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideDetectionDao(database: BlueMeanieDatabase): DetectionDao {
        return database.detectionDao()
    }

    @Provides
    @Singleton
    fun provideCommunityDetectionDao(database: BlueMeanieDatabase): CommunityDetectionDao {
        return database.communityDetectionDao()
    }

    @Provides
    @Singleton
    fun provideUserProfileDao(database: BlueMeanieDatabase): UserProfileDao {
        return database.userProfileDao()
    }

    @Provides
    @Singleton
    fun provideChatMessageDao(database: BlueMeanieDatabase): ChatMessageDao {
        return database.chatMessageDao()
    }
}