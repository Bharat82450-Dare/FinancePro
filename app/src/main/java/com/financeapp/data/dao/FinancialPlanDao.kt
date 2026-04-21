package com.financeapp.data.dao

import androidx.room.*
import com.financeapp.data.entities.FinancialPlan
import kotlinx.coroutines.flow.Flow

@Dao
interface FinancialPlanDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlan(plan: FinancialPlan): Long

    @Query("SELECT * FROM financial_plans WHERE isActive = 1 ORDER BY createdDate DESC LIMIT 1")
    fun getActivePlan(): Flow<FinancialPlan?>

    @Query("SELECT * FROM financial_plans WHERE isActive = 1 ORDER BY createdDate DESC LIMIT 1")
    suspend fun getActivePlanOnce(): FinancialPlan?

    @Query("UPDATE financial_plans SET isActive = 0")
    suspend fun deactivateAllPlans()
}
