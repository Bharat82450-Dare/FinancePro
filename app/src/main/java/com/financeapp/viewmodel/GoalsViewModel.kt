package com.financeapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.financeapp.budget.GoalStatus
import com.financeapp.budget.PlanTracker
import com.financeapp.data.entities.SavingsGoal
import com.financeapp.data.repository.FinanceRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class GoalsViewModel(private val repository: FinanceRepository) : ViewModel() {

    /** All savings goals (active + completed). */
    val allGoals: StateFlow<List<SavingsGoal>> =
        repository.getAllGoals()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Active goals only, with computed status. */
    val goalStatuses: StateFlow<List<GoalStatus>> = combine(
        repository.getActiveGoals(),
        repository.getActivePlan()
    ) { goals, plan ->
        PlanTracker.computeGoalStatuses(
            goals              = goals,
            monthlyPlanSavings = plan?.monthlySavingsTarget ?: 0.0
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ── CRUD ─────────────────────────────────────────────────────────────────

    fun addGoal(name: String, targetAmount: Double, targetDate: Long, priority: Int = 1, color: Int) {
        viewModelScope.launch {
            repository.addGoal(
                SavingsGoal(
                    name         = name,
                    targetAmount = targetAmount,
                    targetDate   = targetDate,
                    priority     = priority,
                    color        = color
                )
            )
        }
    }

    fun updateGoal(goal: SavingsGoal) {
        viewModelScope.launch { repository.updateGoal(goal) }
    }

    fun deleteGoal(id: Long) {
        viewModelScope.launch { repository.deleteGoal(id) }
    }

    fun allocateFunds(goalId: Long, amount: Double) {
        if (amount <= 0) return
        viewModelScope.launch { repository.allocateToGoal(goalId, amount) }
    }

    /** Mark goal as inactive (soft-delete). */
    fun archiveGoal(goal: SavingsGoal) {
        viewModelScope.launch { repository.updateGoal(goal.copy(isActive = false)) }
    }
}
