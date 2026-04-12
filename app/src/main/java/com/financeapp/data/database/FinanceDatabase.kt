package com.financeapp.data.database

import android.content.Context
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.financeapp.data.dao.TransactionDao
import com.financeapp.data.dao.CategoryDao
import com.financeapp.data.dao.BudgetDao
import com.financeapp.data.dao.MonthlyBudgetTargetDao
import com.financeapp.data.entities.Transaction
import com.financeapp.data.entities.Category
import com.financeapp.data.entities.Budget
import com.financeapp.data.entities.MonthlyBudgetTarget

@Database(
    entities = [Transaction::class, Category::class, Budget::class, MonthlyBudgetTarget::class],
    version = 2,
    exportSchema = false
)
abstract class FinanceDatabase : RoomDatabase() {

    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun budgetDao(): BudgetDao
    abstract fun monthlyBudgetTargetDao(): MonthlyBudgetTargetDao

    companion object {
        @Volatile
        private var INSTANCE: FinanceDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS monthly_budget_targets (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "totalLimit REAL NOT NULL, " +
                    "month INTEGER NOT NULL, " +
                    "year INTEGER NOT NULL, " +
                    "userId TEXT NOT NULL DEFAULT '')"
                )
            }
        }

        fun getDatabase(context: Context): FinanceDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FinanceDatabase::class.java,
                    "finance_db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
