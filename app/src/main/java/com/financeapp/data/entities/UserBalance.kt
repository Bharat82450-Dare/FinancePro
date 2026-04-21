package com.financeapp.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_balance")
data class UserBalance(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val declaredBalance: Double,
    val effectiveDate: Long,
    val runningBalance: Double,
    val note: String = "",
    val userId: String = ""
)
