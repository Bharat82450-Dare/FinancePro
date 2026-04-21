package com.financeapp.data.database

import android.content.Context
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.financeapp.data.dao.*
import com.financeapp.data.entities.*

@Database(
    entities = [
        com.financeapp.data.entities.Transaction::class, Category::class, Budget::class, MonthlyBudgetTarget::class,
        UserBalance::class, SavingsGoal::class, DailySnapshot::class, FinancialPlan::class
    ],
    version = 3,
    exportSchema = false
)
abstract class FinanceDatabase : RoomDatabase() {

    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun budgetDao(): BudgetDao
    abstract fun monthlyBudgetTargetDao(): MonthlyBudgetTargetDao
    abstract fun balanceDao(): BalanceDao
    abstract fun savingsGoalDao(): SavingsGoalDao
    abstract fun dailySnapshotDao(): DailySnapshotDao
    abstract fun financialPlanDao(): FinancialPlanDao

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

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS user_balance (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "declaredBalance REAL NOT NULL, " +
                    "effectiveDate INTEGER NOT NULL, " +
                    "runningBalance REAL NOT NULL, " +
                    "note TEXT NOT NULL DEFAULT '', " +
                    "userId TEXT NOT NULL DEFAULT '')"
                )

                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS savings_goals (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "name TEXT NOT NULL, " +
                    "targetAmount REAL NOT NULL, " +
                    "savedAmount REAL NOT NULL DEFAULT 0.0, " +
                    "targetDate INTEGER NOT NULL, " +
                    "priority INTEGER NOT NULL DEFAULT 1, " +
                    "isActive INTEGER NOT NULL DEFAULT 1, " +
                    "createdDate INTEGER NOT NULL, " +
                    "color INTEGER NOT NULL DEFAULT ${0xFF2D6CDF.toInt()}, " +
                    "userId TEXT NOT NULL DEFAULT '')"
                )

                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS daily_snapshots (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "date INTEGER NOT NULL, " +
                    "balance REAL NOT NULL, " +
                    "totalIncomeToday REAL NOT NULL, " +
                    "totalExpenseToday REAL NOT NULL, " +
                    "cumulativeIncome REAL NOT NULL, " +
                    "cumulativeExpense REAL NOT NULL, " +
                    "userId TEXT NOT NULL DEFAULT '')"
                )
                database.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS index_daily_snapshots_date ON daily_snapshots(date)"
                )

                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS financial_plans (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "monthlyIncomeEstimate REAL NOT NULL, " +
                    "monthlyExpenseLimit REAL NOT NULL, " +
                    "monthlySavingsTarget REAL NOT NULL, " +
                    "isActive INTEGER NOT NULL DEFAULT 1, " +
                    "createdDate INTEGER NOT NULL, " +
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
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
