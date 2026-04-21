package com.financeapp.data.model

import androidx.room.ColumnInfo

data class MonthlyTotal(
    @ColumnInfo(name = "month") val month: String,
    @ColumnInfo(name = "total") val total: Double
)

data class CategorySpending(
    @ColumnInfo(name = "category") val category: String,
    @ColumnInfo(name = "total") val total: Double
)

data class DailyTotals(
    @ColumnInfo(name = "income") val income: Double,
    @ColumnInfo(name = "expense") val expense: Double
)
