package com.financeapp.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.financeapp.data.entities.Transaction
import com.financeapp.data.model.CategorySpending
import com.financeapp.data.model.MonthlyTotal
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<Transaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: Transaction): Long

    @Update
    suspend fun updateTransaction(transaction: Transaction)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteTransaction(id: Long)

    @Query("SELECT * FROM transactions WHERE isSynced = 0")
    suspend fun getUnsyncedTransactions(): List<Transaction>

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'INCOME'")
    fun getTotalIncome(): Flow<Double?>

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'EXPENSE'")
    fun getTotalExpense(): Flow<Double?>

    @Query("SELECT strftime('%m', date/1000, 'unixepoch') as month, SUM(amount) as total FROM transactions WHERE type = 'EXPENSE' AND strftime('%Y', date/1000, 'unixepoch') = :year GROUP BY month ORDER BY month ASC")
    fun getMonthlyExpenseTotals(year: String): Flow<List<MonthlyTotal>>

    @Query("SELECT category, SUM(amount) as total FROM transactions WHERE type = 'EXPENSE' AND strftime('%m', date/1000, 'unixepoch') = :month AND strftime('%Y', date/1000, 'unixepoch') = :year GROUP BY category")
    fun getCategorySpendingForMonth(month: String, year: String): Flow<List<CategorySpending>>

    @Query("SELECT * FROM transactions WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getTransactionsInRange(startDate: Long, endDate: Long): Flow<List<Transaction>>

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM transactions WHERE type = 'EXPENSE' AND strftime('%m', date/1000, 'unixepoch') = :month AND strftime('%Y', date/1000, 'unixepoch') = :year")
    fun getTotalExpenseForMonth(month: String, year: String): Flow<Double>
}
