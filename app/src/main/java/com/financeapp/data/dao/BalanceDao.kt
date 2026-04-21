package com.financeapp.data.dao

import androidx.room.*
import com.financeapp.data.entities.UserBalance
import kotlinx.coroutines.flow.Flow

@Dao
interface BalanceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBalance(balance: UserBalance): Long

    @Update
    suspend fun updateBalance(balance: UserBalance)

    @Query("SELECT * FROM user_balance ORDER BY effectiveDate DESC LIMIT 1")
    fun getLatestBalance(): Flow<UserBalance?>

    @Query("SELECT * FROM user_balance ORDER BY effectiveDate DESC LIMIT 1")
    suspend fun getLatestBalanceOnce(): UserBalance?

    @Query("SELECT * FROM user_balance ORDER BY effectiveDate DESC")
    fun getBalanceHistory(): Flow<List<UserBalance>>

    @Query("UPDATE user_balance SET runningBalance = runningBalance + :amount WHERE id = (SELECT id FROM user_balance ORDER BY effectiveDate DESC LIMIT 1)")
    suspend fun adjustRunningBalance(amount: Double)
}
