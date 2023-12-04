package ru.fox.diplom.dao

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.fox.diplom.db.AppDb

@InstallIn(SingletonComponent::class)
@Module
class DaoModule {
    @Provides
    fun providePostDao (db:AppDb):PostDao = db.postDao()
    @Provides
    fun providePostRemoteKeyDao (db:AppDb):PostRemoteKeyDao = db.postRemoteKeyDao()
}