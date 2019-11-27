package com.improve.latetrain.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.improve.latetrain.data.entities.LiveTotalMinutes

@Database(entities = [LiveTotalMinutes::class], version = 1)
abstract class AppDatabase : RoomDatabase() {

    abstract fun liveTotalMinutesDao(): LiveTotalMinutesDao

    companion object {

        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null)
                return tempInstance
            val instance = Room.databaseBuilder(context, AppDatabase::class.java, "app_database")
                .build()
            INSTANCE = instance
            return instance
        }
    }
}