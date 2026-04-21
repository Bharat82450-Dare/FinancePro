package com.financeapp.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "financial_plans")
data class FinancialPlan(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val monthlyIncomeEstimate: Double,
    val monthlyExpenseLimit: Double,
    val monthlySavingsTarget: Double,
    val isActive: Boolean = true,
    val createdDate: Long = System.currentTimeMillis(),
    val userId: String = ""
)
