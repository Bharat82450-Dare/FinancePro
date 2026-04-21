package com.financeapp.budget

import com.financeapp.data.entities.DailySnapshot
import com.financeapp.data.entities.FinancialPlan
import com.financeapp.data.entities.SavingsGoal
import java.util.Calendar
import java.util.concurrent.TimeUnit
import kotlin.math.max

// ─── Result models ───────────────────────────────────────────────────────────

enum class TrackingLevel { ON_TRACK, SLIGHTLY_BEHIND, OFF_TRACK }

/**
 * A snapshot of how the user is tracking against their active [FinancialPlan] for the
 * current calendar month.
 */
data class TrackingStatus(
    val level: TrackingLevel,
    /** Actual savings accumulated this month (income - expense so far). */
    val savingsProgress: Double,
    /** Pro-rated savings target for today's date within the month. */
    val savingsTarget: Double,
    /** Total expense recorded this month. */
    val expenseProgress: Double,
    /** Pro-rated expense limit for today's date within the month. */
    val expenseLimit: Double,
    val daysIntoMonth: Int,
    val totalDaysInMonth: Int,
    /** Projected balance at month end if current pace continues. */
    val projectedMonthEndBalance: Double,
    /** Human-readable one-liner, e.g. "₹2,100 behind savings plan". */
    val summary: String
) {
    val isOnTrack: Boolean get() = level == TrackingLevel.ON_TRACK
}

/**
 * Per-goal tracking info derived from [SavingsGoal] and current balance.
 */
data class GoalStatus(
    val goal: SavingsGoal,
    val daysRemaining: Int,
    /** Amount still needed to reach the goal. */
    val amountRemaining: Double,
    /** Daily savings required from today to hit the goal on time. */
    val dailySavingsNeeded: Double,
    /** True if [dailySavingsNeeded] is realistic relative to the active plan's savings target. */
    val isAchievable: Boolean,
    val progressPercent: Float
)

// ─── Tracker object ───────────────────────────────────────────────────────────

object PlanTracker {

    /**
     * Compute how the user is doing against [plan] this month, using [snapshots] for
     * cumulative figures and [currentBalance] as the live balance.
     *
     * @param plan          Active financial plan.
     * @param snapshots     DailySnapshot list for the current month (may be empty early in month).
     * @param currentBalance Latest running balance (or 0.0 if never set).
     */
    fun computeTrackingStatus(
        plan: FinancialPlan,
        snapshots: List<DailySnapshot>,
        currentBalance: Double
    ): TrackingStatus {
        val cal = Calendar.getInstance()
        val daysIntoMonth = cal.get(Calendar.DAY_OF_MONTH)
        val totalDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

        // Pro-rate targets to today
        val fraction = daysIntoMonth.toDouble() / totalDays
        val proratedExpenseLimit = plan.monthlyExpenseLimit * fraction
        val proratedSavingsTarget = plan.monthlySavingsTarget * fraction

        // Aggregate actual figures from snapshots (use latest cumulative values)
        val latestSnap = snapshots.maxByOrNull { it.date }
        val actualIncome  = latestSnap?.cumulativeIncome  ?: 0.0
        val actualExpense = latestSnap?.cumulativeExpense ?: 0.0
        val actualSavings = actualIncome - actualExpense

        // Project to end of month at current daily pace
        val dailyNetRate = if (daysIntoMonth > 0) actualSavings / daysIntoMonth else 0.0
        val projectedMonthEndBalance = currentBalance + dailyNetRate * (totalDays - daysIntoMonth)

        // Evaluate
        val savingsDelta = actualSavings - proratedSavingsTarget           // +ve = ahead
        val expenseOverrun = actualExpense - proratedExpenseLimit          // +ve = over budget

        val level = when {
            expenseOverrun > plan.monthlyExpenseLimit * 0.15 -> TrackingLevel.OFF_TRACK
            savingsDelta < -(plan.monthlySavingsTarget * 0.20) -> TrackingLevel.OFF_TRACK
            expenseOverrun > 0 || savingsDelta < 0 -> TrackingLevel.SLIGHTLY_BEHIND
            else -> TrackingLevel.ON_TRACK
        }

        val summary = buildSummary(level, savingsDelta, expenseOverrun)

        return TrackingStatus(
            level                  = level,
            savingsProgress        = actualSavings,
            savingsTarget          = proratedSavingsTarget,
            expenseProgress        = actualExpense,
            expenseLimit           = proratedExpenseLimit,
            daysIntoMonth          = daysIntoMonth,
            totalDaysInMonth       = totalDays,
            projectedMonthEndBalance = projectedMonthEndBalance,
            summary                = summary
        )
    }

    /**
     * Compute per-goal tracking info for a list of active goals.
     *
     * @param goals         Active savings goals.
     * @param monthlyPlanSavings The plan's monthly savings target (used for achievability check).
     */
    fun computeGoalStatuses(
        goals: List<SavingsGoal>,
        monthlyPlanSavings: Double
    ): List<GoalStatus> {
        val now = System.currentTimeMillis()
        return goals.map { goal ->
            val msRemaining  = max(0L, goal.targetDate - now)
            val daysRemaining = TimeUnit.MILLISECONDS.toDays(msRemaining).toInt()
            val amountRemaining = max(0.0, goal.targetAmount - goal.savedAmount)
            val dailySavingsNeeded = if (daysRemaining > 0) amountRemaining / daysRemaining else amountRemaining
            val dailyPlanSavings = monthlyPlanSavings / 30.0
            val progressPercent = if (goal.targetAmount > 0)
                (goal.savedAmount / goal.targetAmount * 100).toFloat().coerceIn(0f, 100f)
            else 0f

            GoalStatus(
                goal               = goal,
                daysRemaining      = daysRemaining,
                amountRemaining    = amountRemaining,
                dailySavingsNeeded = dailySavingsNeeded,
                isAchievable       = dailySavingsNeeded <= dailyPlanSavings,
                progressPercent    = progressPercent
            )
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private fun buildSummary(
        level: TrackingLevel,
        savingsDelta: Double,
        expenseOverrun: Double
    ): String = when (level) {
        TrackingLevel.ON_TRACK -> "You're on track! 🎉 Keep it up."
        TrackingLevel.SLIGHTLY_BEHIND -> {
            val amt = if (savingsDelta < 0) "₹${"%.0f".format(-savingsDelta)} behind savings plan"
            else "₹${"%.0f".format(expenseOverrun)} over expense limit"
            "Slightly behind — $amt"
        }
        TrackingLevel.OFF_TRACK -> {
            val larger = maxOf(-savingsDelta, expenseOverrun)
            "Off track — ₹${"%.0f".format(larger)} gap. Review your spending. ⚠️"
        }
    }
}
