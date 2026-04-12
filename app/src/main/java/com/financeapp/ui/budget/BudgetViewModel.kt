package com.financeapp.ui.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.financeapp.budget.BudgetManager
import com.financeapp.budget.BudgetWithStatus
import com.financeapp.budget.OverallBudgetSummary
import com.financeapp.data.entities.Budget
import com.financeapp.data.entities.Category
import com.financeapp.data.entities.MonthlyBudgetTarget
import com.financeapp.data.repository.FinanceRepository
import com.financeapp.utils.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

class BudgetViewModel(
    private val repository: FinanceRepository,
    private val budgetManager: BudgetManager,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val calendar = Calendar.getInstance()
    private val currentMonth = calendar.get(Calendar.MONTH) + 1
    private val currentYear = calendar.get(Calendar.YEAR)
    private val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
    private val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

    private val _monthYear = MutableStateFlow(Pair(currentMonth, currentYear))

    val budgetStatuses: StateFlow<List<BudgetWithStatus>> = _monthYear
        .flatMapLatest { (month, year) ->
            budgetManager.getBudgetStatuses(month, year)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val overallBudget: StateFlow<OverallBudgetSummary?> = _monthYear
        .flatMapLatest { (month, year) ->
            budgetManager.getOverallBudgetSummary(month, year, dayOfMonth, daysInMonth)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val categories: StateFlow<List<Category>> = repository.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addBudget(category: String, limit: Double) {
        viewModelScope.launch {
            val (month, year) = _monthYear.value
            val userId = sessionManager.getUserEmail() ?: ""
            val existing = repository.getBudgetByCategory(category, month, year)
            if (existing != null) {
                repository.insertBudget(existing.copy(amountLimit = limit))
            } else {
                repository.insertBudget(
                    Budget(
                        category = category,
                        amountLimit = limit,
                        month = month,
                        year = year,
                        userId = userId
                    )
                )
            }
        }
    }

    fun setMonthlyTarget(amount: Double) {
        viewModelScope.launch {
            val (month, year) = _monthYear.value
            val userId = sessionManager.getUserEmail() ?: ""
            repository.insertMonthlyTarget(
                MonthlyBudgetTarget(
                    totalLimit = amount,
                    month = month,
                    year = year,
                    userId = userId
                )
            )
        }
    }

    fun deleteBudget(budget: Budget) {
        viewModelScope.launch {
            repository.deleteBudget(budget)
        }
    }
}
