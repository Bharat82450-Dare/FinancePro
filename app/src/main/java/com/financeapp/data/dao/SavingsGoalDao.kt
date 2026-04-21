package com.financeapp.data.dao

import androidx.room.*
import com.financeapp.data.entities.SavingsGoal
import kotlinx.coroutines.flow.Flow

@Dao
interface SavingsGoalDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: SavingsGoal): Long

    @Update
    suspend fun updateGoal(goal: SavingsGoal)

    @Query("DELETE FROM savings_goals WHERE id = :id")
    suspend fun deleteGoal(id: Long)

    @Query("SELECT * FROM savings_goals WHERE isActive = 1 ORDER BY priority ASC, targetDate ASC")
    fun getActiveGoals(): Flow<List<SavingsGoal>>

    @Query("SELECT * FROM savings_goals ORDER BY createdDate DESC")
    fun getAllGoals(): Flow<List<SavingsGoal>>

    @Query("SELECT * FROM savings_goals WHERE id = :id")
    suspend fun getGoalById(id: Long): SavingsGoal?

    @Query("SELECT COALESCE(SUM(targetAmount - savedAmount), 0.0) FROM savings_goals WHERE isActive = 1")
    fun getTotalRemainingGoalAmount(): Flow<Double>
}
