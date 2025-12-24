package com.example.basefragment.data.room

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.basefragment.data.model.AvatarModel
import javax.inject.Singleton

@Singleton
@Database(entities = [AvatarModel::class], version = 1, exportSchema = false)
abstract class AppDB: RoomDatabase() {
    abstract fun dbDao(): Dao
}