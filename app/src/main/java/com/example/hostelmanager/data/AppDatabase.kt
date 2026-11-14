package com.example.hostelmanager.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Hostel::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    // The front desk: hands out the Hostel staff (DAO)
    abstract fun hostelDao(): HostelDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "hostel_manager_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}