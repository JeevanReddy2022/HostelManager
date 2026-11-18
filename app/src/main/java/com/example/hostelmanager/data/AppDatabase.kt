package com.example.hostelmanager.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [Hostel::class, RoomEntity::class, Person::class, Stay::class],
    version = 6,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun hostelDao(): HostelDao
    abstract fun roomDao(): RoomDao
    abstract fun personDao(): PersonDao
    abstract fun stayDao(): StayDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE stays ADD COLUMN monthlyRent INTEGER NOT NULL DEFAULT 0")
                db.execSQL(
                    "UPDATE stays SET monthlyRent = COALESCE(" +
                            "(SELECT defaultRentPerBed FROM rooms WHERE rooms.id = stays.roomId), 0)"
                )
            }
        }

        // NEW: adds the Govt ID back-side column, keeps all existing data
        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE persons ADD COLUMN govtIdBackPath TEXT NOT NULL DEFAULT ''")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "hostel_manager_db"
                )
                    .addMigrations(MIGRATION_4_5, MIGRATION_5_6)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}