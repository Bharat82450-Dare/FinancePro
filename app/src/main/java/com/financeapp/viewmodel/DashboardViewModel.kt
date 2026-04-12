package com.financeapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.financeapp.data.entities.Transaction
import com.financeapp.data.repository.FinanceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

data class MonthlyBudgetProgress(
    val totalLimit: Double,
    val totalSpent: Double,
    val percentUsed: Double
)

class DashboardViewModel(private val repository: FinanceRepository) : ViewModel() {

    val allTransactions: Flow<List<Transaction>> = repository.allTransactions

    val totalIncome: StateFlow<Double?> = repository.totalIncome
        .stateIn(viewModelScope, SharingStarted.Lazily, 0.0)

    val totalExpense: StateFlow<Double?> = repository.totalExpense
        .stateIn(viewModelScope, SharingStarted.Lazily, 0.0)

    val monthlyBudgetProgress: StateFlow<MonthlyBudgetProgress?> = run {
        val cal = Calendar.getInstance()
        val month = cal.get(Calendar.MONTH) + 1
        val year = cal.get(Calendar.YEAR)
        val monthStr = String.format("%02d", month)
        val yearStr = year.toString()
        combine(
            repository.getMonthlyTargetForMonth(month, year),
            repository.getTotalExpenseForMonth(monthStr, yearStr)
        ) { target, spent ->
            val limit = target?.totalLimit ?: return@combine null
            MonthlyBudgetProgress(
                totalLimit = limit,
                totalSpent = spent,
                percentUsed = if (limit > 0) (spent / limit) * 100 else 0.0
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    }

    fun syncData() {
        viewModelScope.launch {
            repository.syncPendingData()
        }
    }
}
