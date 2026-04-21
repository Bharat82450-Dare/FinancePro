package com.financeapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.financeapp.budget.BalanceManager
import com.financeapp.budget.BudgetManager
import com.financeapp.data.repository.FinanceRepository
import com.financeapp.ui.budget.BudgetViewModel
import com.financeapp.ui.pdf.ImportTransactionsViewModel
import com.financeapp.ui.report.ReportViewModel
import com.financeapp.utils.SessionManager

class ViewModelFactory(
    private val repository: FinanceRepository,
    private val sessionManager: SessionManager? = null,
    private val budgetManager: BudgetManager? = null,
    private val balanceManager: BalanceManager? = null
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return when {
            modelClass.isAssignableFrom(DashboardViewModel::class.java) ->
                DashboardViewModel(
                    repository     = repository,
                    balanceManager = balanceManager ?: BalanceManager(repository)
                ) as T

            modelClass.isAssignableFrom(AddTransactionViewModel::class.java) ->
                AddTransactionViewModel(repository) as T

            modelClass.isAssignableFrom(HistoryViewModel::class.java) ->
                HistoryViewModel(repository) as T

            modelClass.isAssignableFrom(GoalsViewModel::class.java) ->
                GoalsViewModel(repository) as T

            modelClass.isAssignableFrom(BudgetViewModel::class.java) ->
                BudgetViewModel(
                    repository,
                    budgetManager  ?: error("BudgetManager required for BudgetViewModel"),
                    sessionManager ?: error("SessionManager required for BudgetViewModel")
                ) as T

            modelClass.isAssignableFrom(ReportViewModel::class.java) ->
                ReportViewModel(
                    repository,
                    sessionManager ?: error("SessionManager required for ReportViewModel")
                ) as T

            modelClass.isAssignableFrom(ImportTransactionsViewModel::class.java) ->
                ImportTransactionsViewModel(repository) as T

            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
