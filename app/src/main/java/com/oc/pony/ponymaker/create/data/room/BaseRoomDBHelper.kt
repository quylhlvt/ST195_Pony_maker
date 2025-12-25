package com.oc.pony.ponymaker.create.data.room

import android.content.Context
import androidx.room.Room


open class BaseRoomDBHelper(context: Context) {
    val db = Room.databaseBuilder(context, AppDB::class.java,"Avatar").build()
    companion object : com.oc.pony.ponymaker.create.utils.SingletonHolder<BaseRoomDBHelper, Context>(::BaseRoomDBHelper)
}