package com.financeapp.budget

sealed class BudgetStatus {
    data class Ok(
        val category: String,
        val spent: Double,
        val limit: Double
    ) : BudgetStatus()

    data class Warning(
        val category: String,
        val spent: Double,
        val limit: Double
    ) : BudgetStatus()

    data class Exceeded(
        val category: String,
        val spent: Double,
        val limit: Double
    ) : BudgetStatus()
}
