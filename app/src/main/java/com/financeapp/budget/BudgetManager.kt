package com.financeapp.budget

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.financeapp.FinanceApplication
import com.financeapp.MainActivity
import com.financeapp.R
import com.financeapp.data.entities.Budget
import com.financeapp.data.model.CategorySpending
import com.financeapp.data.repository.FinanceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

data class BudgetWithStatus(
    val budget: Budget,
    val spentAmount: Double,
    val status: BudgetStatus
)

data class OverallBudgetSummary(
    val totalLimit: Double,
    val totalSpent: Double,
    val projectedSpend: Double,
    val percentUsed: Double
)

class BudgetManager(
    private val repository: FinanceRepository,
    private val context: Context
) {
    private val notifiedCategories = mutableSetOf<String>()
    private var overallExceededNotified = false

    fun getBudgetStatuses(month: Int, year: Int): Flow<List<BudgetWithStatus>> {
        val monthStr = String.format("%02d", month)
        val yearStr = year.toString()
        val budgetsFlow = repository.getBudgetsForMonth(month, year)
        val spendingFlow = repository.getCategorySpendingForMonth(monthStr, yearStr)

        return combine(budgetsFlow, spendingFlow) { budgets, spendings ->
            val spendingMap = spendings.associate { it.category to it.total }
            budgets.map { budget ->
                val spent = spendingMap[budget.category] ?: 0.0
                val percent = if (budget.amountLimit > 0) (spent / budget.amountLimit) * 100 else 0.0
                val status = when {
                    percent >= 100.0 -> BudgetStatus.Exceeded(budget.category, spent, budget.amountLimit)
                    percent >= 80.0 -> BudgetStatus.Warning(budget.category, spent, budget.amountLimit)
                    else -> BudgetStatus.Ok(budget.category, spent, budget.amountLimit)
                }
                if (status is BudgetStatus.Exceeded && budget.category !in notifiedCategories) {
                    notifiedCategories.add(budget.category)
                    sendExceededNotification(budget.category, spent, budget.amountLimit)
                }
                BudgetWithStatus(budget, spent, status)
            }
        }
    }

    fun getOverallBudgetSummary(month: Int, year: Int, dayOfMonth: Int, daysInMonth: Int): Flow<OverallBudgetSummary?> {
        val monthStr = String.format("%02d", month)
        val yearStr = year.toString()
        val targetFlow = repository.getMonthlyTargetForMonth(month, year)
        val spentFlow = repository.getTotalExpenseForMonth(monthStr, yearStr)

        return combine(targetFlow, spentFlow) { target, spent ->
            val limit = target?.totalLimit ?: return@combine null
            val percent = if (limit > 0) (spent / limit) * 100 else 0.0
            val projected = if (dayOfMonth > 0) (spent / dayOfMonth) * daysInMonth else spent

            if (percent >= 100.0 && !overallExceededNotified) {
                overallExceededNotified = true
                sendOverallExceededNotification(spent, limit)
            } else if (percent < 100.0) {
                overallExceededNotified = false
            }

            OverallBudgetSummary(
                totalLimit = limit,
                totalSpent = spent,
                projectedSpend = projected,
                percentUsed = percent
            )
        }
    }

    private fun sendExceededNotification(category: String, spent: Double, limit: Double) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, FinanceApplication.BUDGET_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_budget)
            .setContentTitle("Budget Exceeded: $category")
            .setContentText("Spent ₹${"%.0f".format(spent)} of ₹${"%.0f".format(limit)} limit")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val nm = context.getSystemService(NotificationManager::class.java)
        nm.notify(category.hashCode(), notification)
    }

    private fun sendOverallExceededNotification(spent: Double, limit: Double) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, FinanceApplication.BUDGET_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_budget)
            .setContentTitle("Monthly Budget Exceeded!")
            .setContentText("Total spent ₹${"%.0f".format(spent)} exceeds your ₹${"%.0f".format(limit)} monthly target")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val nm = context.getSystemService(NotificationManager::class.java)
        nm.notify("overall_budget".hashCode(), notification)
    }
}
