package com.financeapp.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val amount: Double,
    val type: String, // "INCOME" or "EXPENSE"
    val category: String,
    val date: Long, // Timestamp
    val note: String = "",
    val isSynced: Boolean = false,
    val firebaseId: String? = null
)
