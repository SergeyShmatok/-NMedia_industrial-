package ru.netology.nmedia.dao

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.netology.nmedia.db.AppDb
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object DaoModule {

    @Singleton
    @Provides
    fun providePostRemoteKeyDao(
        db: AppDb): PostRemoteKeyDao = db.postRemoteKeyDao()

}