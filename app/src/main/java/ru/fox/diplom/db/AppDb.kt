package ru.fox.diplom.db

import androidx.room.Database
import androidx.room.RoomDatabase
import ru.fox.diplom.dao.PostDao
import ru.fox.diplom.dao.PostRemoteKeyDao
import ru.fox.diplom.entity.PostEntity
import ru.fox.diplom.entity.PostRemoteKeyEntity

@Database(entities = [PostEntity::class, PostRemoteKeyEntity::class], version = 1, exportSchema = false)
abstract class AppDb : RoomDatabase() {
    abstract fun postDao(): PostDao
    abstract fun postRemoteKeyDao(): PostRemoteKeyDao
}