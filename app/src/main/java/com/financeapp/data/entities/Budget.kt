package com.financeapp.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "budgets")
data class Budget(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val category: String,
    val amountLimit: Double,
    val month: Int, // 1-12
    val year: Int,
    val userId: String = ""
)
