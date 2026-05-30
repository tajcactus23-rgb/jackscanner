package com.jackscanner.di

import com.google.gson.Gson
import com.jackscanner.data.repository.CommunityRepositoryImpl
import com.jackscanner.data.repository.DetectionRepositoryImpl
import com.jackscanner.data.network.BitcoinApiService
import com.jackscanner.data.repository.UserRepositoryImpl
import com.jackscanner.domain.repository.CommunityRepository
import com.jackscanner.domain.repository.DetectionRepository
import com.jackscanner.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindDetectionRepository(
        impl: DetectionRepositoryImpl
    ): DetectionRepository

    @Binds
    @Singleton
    abstract fun bindCommunityRepository(
        impl: CommunityRepositoryImpl
    ): CommunityRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(
        impl: UserRepositoryImpl
    ): UserRepository
}

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideGson(): Gson = Gson()
    
    @Provides
    @Singleton
    fun provideBitcoinApiService(): BitcoinApiService = BitcoinApiService()
}
