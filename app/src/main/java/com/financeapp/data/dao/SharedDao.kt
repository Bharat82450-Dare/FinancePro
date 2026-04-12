package com.financeapp.data.dao

import androidx.room.*
import com.financeapp.data.entities.Category
import com.financeapp.data.entities.MonthlyBudgetTarget
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: Category)

    @Query("SELECT * FROM categories")
    fun getAllCategories(): Flow<List<Category>>

    @Query("DELETE FROM categories WHERE name = :name")
    suspend fun deleteCategory(name: String)
}


@Dao
interface BudgetDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudget(budget: com.financeapp.data.entities.Budget)

    @Query("SELECT * FROM budgets WHERE month = :month AND year = :year")
    fun getBudgetsForMonth(month: Int, year: Int): Flow<List<com.financeapp.data.entities.Budget>>

    @Query("SELECT * FROM budgets WHERE category = :category AND month = :month AND year = :year")
    suspend fun getBudgetByCategory(category: String, month: Int, year: Int): com.financeapp.data.entities.Budget?

    @androidx.room.Delete
    suspend fun deleteBudget(budget: com.financeapp.data.entities.Budget)
}

@Dao
interface MonthlyBudgetTargetDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTarget(target: MonthlyBudgetTarget)

    @Query("SELECT * FROM monthly_budget_targets WHERE month = :month AND year = :year LIMIT 1")
    fun getTargetForMonth(month: Int, year: Int): Flow<MonthlyBudgetTarget?>

    @Query("SELECT * FROM monthly_budget_targets WHERE month = :month AND year = :year LIMIT 1")
    suspend fun getTargetForMonthOnce(month: Int, year: Int): MonthlyBudgetTarget?
}
