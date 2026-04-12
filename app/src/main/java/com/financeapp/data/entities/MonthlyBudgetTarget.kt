package com.financeapp.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "monthly_budget_targets")
data class MonthlyBudgetTarget(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val totalLimit: Double,
    val month: Int,
    val year: Int,
    val userId: String = ""
)
