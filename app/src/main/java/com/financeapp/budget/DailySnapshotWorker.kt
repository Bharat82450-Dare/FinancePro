package com.financeapp.budget

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.financeapp.FinanceApplication
import com.financeapp.MainActivity
import com.financeapp.R
import com.financeapp.data.entities.DailySnapshot
import com.financeapp.data.repository.RepositoryProvider
import java.util.Calendar

/**
 * Runs once a day (scheduled in [FinanceApplication]).
 *
 * 1. Reads today's income/expense totals from Room.
 * 2. Gets current running balance.
 * 3. Writes/updates today's [DailySnapshot].
 * 4. Evaluates the active plan via [PlanTracker] and fires a notification if off-track.
 */
class DailySnapshotWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        return try {
            val repo = RepositoryProvider.get(applicationContext)

            // Day boundaries (midnight → 23:59:59)
            val cal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val dayStart = cal.timeInMillis
            cal.set(Calendar.HOUR_OF_DAY, 23)
            cal.set(Calendar.MINUTE, 59)
            cal.set(Calendar.SECOND, 59)
            val dayEnd = cal.timeInMillis

            // Month boundaries for cumulative totals
            val monthCal = Calendar.getInstance().apply {
                set(Calendar.DAY_OF_MONTH, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val monthStart = monthCal.timeInMillis

            // Today's totals
            val dailyTotals = repo.getDailyTotals(dayStart, dayEnd)

            // Cumulative month transactions
            val monthTxns = repo.getTransactionsSince(monthStart)
            val cumIncome  = monthTxns.filter { it.type == "INCOME"  }.sumOf { it.amount }
            val cumExpense = monthTxns.filter { it.type == "EXPENSE" }.sumOf { it.amount }

            val balance = repo.getCurrentBalanceOnce()?.runningBalance ?: 0.0

            // Write snapshot (REPLACE on same date via unique index)
            repo.insertSnapshot(
                DailySnapshot(
                    date               = dayStart,
                    balance            = balance,
                    totalIncomeToday   = dailyTotals.income,
                    totalExpenseToday  = dailyTotals.expense,
                    cumulativeIncome   = cumIncome,
                    cumulativeExpense  = cumExpense
                )
            )

            // Evaluate plan and notify if needed
            val plan = repo.getActivePlanOnce()
            if (plan != null) {
                val snapshots = repo.getRecentSnapshots(31)
                // Collect flow to list – we're already on a background coroutine
                val snapList = mutableListOf<DailySnapshot>()
                snapshots.collect { snapList.addAll(it) }
                val status = PlanTracker.computeTrackingStatus(plan, snapList, balance)
                if (!status.isOnTrack) sendTrackingNotification(status.summary)
            }

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }

    private fun sendTrackingNotification(message: String) {
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pending = PendingIntent.getActivity(
            applicationContext, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(
            applicationContext, FinanceApplication.TRACKING_CHANNEL_ID
        )
            .setSmallIcon(R.drawable.ic_chart)
            .setContentTitle("Daily Finance Check")
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pending)
            .setAutoCancel(true)
            .build()

        val nm = applicationContext.getSystemService(NotificationManager::class.java)
        nm.notify(NOTIFICATION_ID, notification)
    }

    companion object {
        const val WORK_NAME       = "daily_snapshot_worker"
        const val NOTIFICATION_ID = 9001
    }
}
