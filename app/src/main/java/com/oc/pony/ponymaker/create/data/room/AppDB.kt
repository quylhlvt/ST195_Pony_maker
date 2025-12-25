package com.oc.pony.ponymaker.create.data.room

import androidx.room.Database
import androidx.room.RoomDatabase
import com.oc.pony.ponymaker.create.data.model.AvatarModel
import javax.inject.Singleton

@Singleton
@Database(entities = [AvatarModel::class], version = 1, exportSchema = false)
abstract class AppDB: RoomDatabase() {
    abstract fun dbDao(): Dao
}