package com.financeapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.financeapp.data.entities.Transaction
import com.financeapp.data.repository.FinanceRepository
import kotlinx.coroutines.launch

class AddTransactionViewModel(private val repository: FinanceRepository) : ViewModel() {

    fun addTransaction(
        title: String,
        amount: Double,
        type: String,
        category: String,
        date: Long,
        note: String
    ) {
        viewModelScope.launch {
            val transaction = Transaction(
                title = title,
                amount = amount,
                type = type,
                category = category,
                date = date,
                note = note
            )
            repository.addTransaction(transaction)
        }
    }
}
