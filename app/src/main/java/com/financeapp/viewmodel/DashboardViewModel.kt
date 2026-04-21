package com.financeapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.financeapp.budget.BalanceManager
import com.financeapp.budget.GoalStatus
import com.financeapp.budget.PlanTracker
import com.financeapp.budget.TrackingStatus
import com.financeapp.data.entities.SavingsGoal
import com.financeapp.data.entities.Transaction
import com.financeapp.data.entities.UserBalance
import com.financeapp.data.repository.FinanceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

data class MonthlyBudgetProgress(
    val totalLimit: Double,
    val totalSpent: Double,
    val percentUsed: Double
)

class DashboardViewModel(
    private val repository: FinanceRepository,
    private val balanceManager: BalanceManager
) : ViewModel() {

    val allTransactions: Flow<List<Transaction>> = repository.allTransactions

    val totalIncome: StateFlow<Double?> = repository.totalIncome
        .stateIn(viewModelScope, SharingStarted.Lazily, 0.0)

    val totalExpense: StateFlow<Double?> = repository.totalExpense
        .stateIn(viewModelScope, SharingStarted.Lazily, 0.0)

    // ── Balance ──────────────────────────────────────────────────────────────

    /** Latest UserBalance record from Room (null = never set by user). */
    val currentBalance: StateFlow<UserBalance?> =
        balanceManager.currentBalance
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    /** Convenience: running balance value for UI calculations. */
    val runningBalance: StateFlow<Double> =
        balanceManager.currentBalance
            .map { it?.runningBalance ?: 0.0 }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // ── Budget progress ───────────────────────────────────────────────────────

    val monthlyBudgetProgress: StateFlow<MonthlyBudgetProgress?> = run {
        val cal = Calendar.getInstance()
        val month = cal.get(Calendar.MONTH) + 1
        val year = cal.get(Calendar.YEAR)
        val monthStr = String.format("%02d", month)
        val yearStr  = year.toString()
        combine(
            repository.getMonthlyTargetForMonth(month, year),
            repository.getTotalExpenseForMonth(monthStr, yearStr)
        ) { target, spent ->
            val limit = target?.totalLimit ?: return@combine null
            MonthlyBudgetProgress(
                totalLimit   = limit,
                totalSpent   = spent,
                percentUsed  = if (limit > 0) (spent / limit) * 100 else 0.0
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    }

    // ── Plan tracking ─────────────────────────────────────────────────────────

    /**
     * Reactive tracking status derived from the active plan + recent snapshots + balance.
     * Emits null if no active plan is configured.
     */
    val trackingStatus: StateFlow<TrackingStatus?> = run {
        combine(
            repository.getActivePlan(),
            repository.getRecentSnapshots(31),
            balanceManager.currentBalance
        ) { plan, snapshots, balance ->
            if (plan == null) return@combine null
            PlanTracker.computeTrackingStatus(
                plan           = plan,
                snapshots      = snapshots,
                currentBalance = balance?.runningBalance ?: 0.0
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    }

    // ── Goals (top 3) for the dashboard mini-card ─────────────────────────────

    val activeGoals: StateFlow<List<SavingsGoal>> =
        repository.getActiveGoals()
            .map { it.take(3) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Goal statuses shown on the dashboard mini-card. */
    val dashboardGoalStatuses: StateFlow<List<GoalStatus>> =
        combine(
            repository.getActivePlan(),
            repository.getActiveGoals()
        ) { plan, goals ->
            PlanTracker.computeGoalStatuses(
                goals              = goals.take(3),
                monthlyPlanSavings = plan?.monthlySavingsTarget ?: 0.0
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ── Actions ───────────────────────────────────────────────────────────────

    fun syncData() {
        viewModelScope.launch { repository.syncPendingData() }
    }

    fun setUserBalance(amount: Double, note: String = "User declared") {
        viewModelScope.launch { balanceManager.setBalance(amount, note) }
    }
}
