package com.financeapp.data.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "daily_snapshots",
    indices = [Index(value = ["date"], unique = true)]
)
data class DailySnapshot(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: Long,
    val balance: Double,
    val totalIncomeToday: Double,
    val totalExpenseToday: Double,
    val cumulativeIncome: Double,
    val cumulativeExpense: Double,
    val userId: String = ""
)
