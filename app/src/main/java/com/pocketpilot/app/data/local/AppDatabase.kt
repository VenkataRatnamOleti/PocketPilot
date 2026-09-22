package com.pocketpilot.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [ExpenseEntity::class, LoanEntity::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun expenseDao(): ExpenseDao
    abstract fun loanDao(): LoanDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pocket_pilot_database"
                )
                    .addMigrations(MIGRATION_1_3, MIGRATION_2_3)
                    .build()
                INSTANCE = instance
                instance
            }
        }

        /**
         * Version 1 only contained the original amount/category/description/date columns.
         * Keep existing records while adding the optional receipt and detail fields and
         * introducing the people/borrowings table.
         */
        private val MIGRATION_1_3 = object : androidx.room.migration.Migration(1, 3) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                database.execSQL(
                        """
                        CREATE TABLE expenses_new (
                            id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                            amount REAL NOT NULL,
                            category TEXT NOT NULL,
                            description TEXT NOT NULL,
                            date INTEGER NOT NULL,
                            receiptImagePath TEXT,
                            merchant TEXT NOT NULL,
                            isProfit INTEGER NOT NULL
                        )
                        """.trimIndent()
                )
                database.execSQL(
                        """
                        INSERT INTO expenses_new
                            (id, amount, category, description, date, receiptImagePath, merchant, isProfit)
                        SELECT id, amount, category, description, date, NULL, '', 0 FROM expenses
                        """.trimIndent()
                )
                database.execSQL("DROP TABLE expenses")
                database.execSQL("ALTER TABLE expenses_new RENAME TO expenses")
                database.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS loans (
                            id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                            personName TEXT NOT NULL,
                        phoneOrNote TEXT NOT NULL,
                        amount REAL NOT NULL,
                        type TEXT NOT NULL,
                        date INTEGER NOT NULL,
                        dueDate INTEGER,
                        isSettled INTEGER NOT NULL
                        )
                    """.trimIndent()
                )
            }
        }

        // Version 2 already has the fields introduced above; only the database version changes.
        private val MIGRATION_2_3 = object : androidx.room.migration.Migration(2, 3) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) = Unit
        }
    }
}
