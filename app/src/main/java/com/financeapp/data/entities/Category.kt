package com.financeapp.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val icon: String, // String resource name or icon ID
    val color: Int, // Hex value
    val userId: String = "" // For multi-user support or syncing
)
