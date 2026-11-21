package com.example.hostelmanager.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [Hostel::class, RoomEntity::class, Person::class, Stay::class, Payment::class, Expense::class],
    version = 10, // do not lower this number
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun hostelDao(): HostelDao
    abstract fun roomDao(): RoomDao
    abstract fun personDao(): PersonDao
    abstract fun stayDao(): StayDao
    abstract fun paymentDao(): PaymentDao
    abstract fun expenseDao(): ExpenseDao

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

        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE persons ADD COLUMN govtIdBackPath TEXT NOT NULL DEFAULT ''")
            }
        }

        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE stays ADD COLUMN billingAnchorDay INTEGER NOT NULL DEFAULT 1")
                db.execSQL("""
                    UPDATE stays 
                    SET billingAnchorDay = CAST(SUBSTR(joinDate, 9, 2) AS INTEGER) 
                    WHERE joinDate IS NOT NULL 
                      AND joinDate != '' 
                      AND LENGTH(joinDate) >= 10
                """.trimIndent())
            }
        }

        private val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS payments (
                        id TEXT NOT NULL PRIMARY KEY,
                        stayId TEXT NOT NULL,
                        personId TEXT NOT NULL,
                        hostelId TEXT NOT NULL,
                        amountPaise INTEGER NOT NULL,
                        mode TEXT NOT NULL,
                        date TEXT NOT NULL,
                        note TEXT NOT NULL DEFAULT '',
                        createdAt INTEGER NOT NULL,
                        FOREIGN KEY(stayId) REFERENCES stays(id) ON DELETE CASCADE
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS index_payments_stayId ON payments(stayId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_payments_date ON payments(date)")
            }
        }

        private val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE payments ADD COLUMN paymentDate TEXT NOT NULL DEFAULT ''")
                db.execSQL("UPDATE payments SET paymentDate = date")
            }
        }

        // NEW: adds expenses table
        private val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS expenses (
                        id TEXT NOT NULL PRIMARY KEY,
                        hostelId TEXT NOT NULL,
                        expenseDate TEXT NOT NULL,
                        enteredDate TEXT NOT NULL,
                        category TEXT NOT NULL,
                        amountPaise INTEGER NOT NULL,
                        note TEXT NOT NULL DEFAULT '',
                        receiptImagePath TEXT,
                        createdAt INTEGER NOT NULL,
                        FOREIGN KEY(hostelId) REFERENCES hostels(id) ON DELETE CASCADE
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS index_expenses_hostelId ON expenses(hostelId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_expenses_expenseDate ON expenses(expenseDate)")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "hostel_manager_db"
                )
                    .addMigrations(
                        MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7,
                        MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10
                    )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}