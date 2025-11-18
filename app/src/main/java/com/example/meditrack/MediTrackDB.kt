package com.example.meditrack

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [ReminderEntity::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class MediTrackDB : RoomDatabase() {

    abstract fun reminderDao(): ReminderDao

    companion object {
        @Volatile private var instance: MediTrackDB? = null

        fun getDatabase(context: Context): MediTrackDB =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    MediTrackDB::class.java,
                    "meditrack_db"
                ).build().also { instance = it }
            }
    }
}