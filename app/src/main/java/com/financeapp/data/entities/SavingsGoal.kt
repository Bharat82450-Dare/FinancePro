package com.financeapp.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "savings_goals")
data class SavingsGoal(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val targetAmount: Double,
    val savedAmount: Double = 0.0,
    val targetDate: Long,
    val priority: Int = 1,
    val isActive: Boolean = true,
    val createdDate: Long = System.currentTimeMillis(),
    val color: Int = 0xFF2D6CDF.toInt(),
    val userId: String = ""
)
