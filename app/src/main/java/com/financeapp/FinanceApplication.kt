package com.financeapp

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.financeapp.budget.DailySnapshotWorker
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import java.util.Calendar
import java.util.concurrent.TimeUnit

class FinanceApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        PDFBoxResourceLoader.init(applicationContext)
        createNotificationChannels()
        scheduleDailySnapshotWorker()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(NotificationManager::class.java)

            // Budget alerts (existing)
            nm.createNotificationChannel(
                NotificationChannel(
                    BUDGET_CHANNEL_ID,
                    "Budget Alerts",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply { description = "Notifications when spending exceeds budget limits" }
            )

            // Daily tracking alerts (new)
            nm.createNotificationChannel(
                NotificationChannel(
                    TRACKING_CHANNEL_ID,
                    "Daily Finance Check",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply { description = "Daily summary of your financial plan progress" }
            )
        }
    }

    /**
     * Schedule [DailySnapshotWorker] to run once every 24 hours.
     * Initial delay is calculated so the first run happens near midnight.
     * Uses KEEP policy so rescheduling on every launch doesn't reset the timer.
     */
    private fun scheduleDailySnapshotWorker() {
        val initialDelayMs = millisUntilMidnight()

        val request = PeriodicWorkRequestBuilder<DailySnapshotWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(initialDelayMs, TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            DailySnapshotWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    private fun millisUntilMidnight(): Long {
        val now = System.currentTimeMillis()
        val midnight = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        return (midnight - now).coerceAtLeast(0L)
    }

    companion object {
        const val BUDGET_CHANNEL_ID   = "budget_alerts"
        const val TRACKING_CHANNEL_ID = "tracking_alerts"
    }
}
