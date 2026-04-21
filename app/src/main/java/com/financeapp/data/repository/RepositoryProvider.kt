package com.financeapp.data.repository

import android.content.Context
import com.financeapp.data.database.FinanceDatabase

/**
 * Singleton that constructs [FinanceRepository] once and caches it for the process lifetime.
 * All fragments and receivers should obtain the repository via [RepositoryProvider.get] instead
 * of constructing it individually, to avoid repeatedly wiring up all 8 DAOs.
 */
object RepositoryProvider {

    @Volatile
    private var INSTANCE: FinanceRepository? = null

    fun get(context: Context): FinanceRepository {
        return INSTANCE ?: synchronized(this) {
            INSTANCE ?: buildRepository(context).also { INSTANCE = it }
        }
    }

    private fun buildRepository(context: Context): FinanceRepository {
        val db = FinanceDatabase.getDatabase(context.applicationContext)
        return FinanceRepository(
            transactionDao          = db.transactionDao(),
            categoryDao             = db.categoryDao(),
            budgetDao               = db.budgetDao(),
            monthlyBudgetTargetDao  = db.monthlyBudgetTargetDao(),
            balanceDao              = db.balanceDao(),
            savingsGoalDao          = db.savingsGoalDao(),
            dailySnapshotDao        = db.dailySnapshotDao(),
            financialPlanDao        = db.financialPlanDao()
        )
    }
}
