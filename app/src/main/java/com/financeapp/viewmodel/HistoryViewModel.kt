package com.financeapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.financeapp.data.entities.Transaction
import com.financeapp.data.repository.FinanceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class HistoryViewModel(private val repository: FinanceRepository) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _filterType = MutableStateFlow("ALL") // ALL, INCOME, EXPENSE

    val filteredTransactions: StateFlow<List<Transaction>> = repository.allTransactions
        .combine(_searchQuery) { transactions, query ->
            if (query.isEmpty()) transactions else transactions.filter { 
                it.title.contains(query, ignoreCase = true) || it.category.contains(query, ignoreCase = true)
            }
        }
        .combine(_filterType) { transactions, type ->
            if (type == "ALL") transactions else transactions.filter { it.type == type }
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateFilterType(type: String) {
        _filterType.value = type
    }
}
