package com.financeapp.receiver

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import androidx.core.app.NotificationCompat
import com.financeapp.FinanceApplication
import com.financeapp.MainActivity
import com.financeapp.R
import com.financeapp.data.entities.Transaction
import com.financeapp.data.repository.RepositoryProvider
import com.financeapp.utils.UpiSmsParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent) ?: return

        // Concatenate multi-part messages from the same sender
        val grouped = mutableMapOf<String, StringBuilder>()
        for (msg in messages) {
            val sender = msg.originatingAddress ?: continue
            grouped.getOrPut(sender) { StringBuilder() }.append(msg.messageBody)
        }

        grouped.forEach { (sender, bodyBuilder) ->
            val body = bodyBuilder.toString()

            CoroutineScope(Dispatchers.IO).launch {
                val repo = RepositoryProvider.get(context)

                // 1. Parse transaction
                val parsed = UpiSmsParser.parse(sender, body)
                if (parsed != null) {
                    val transaction = Transaction(
                        title    = parsed.merchant,
                        amount   = parsed.amount,
                        type     = parsed.type,
                        category = UpiSmsParser.guessCategory(parsed.merchant),
                        date     = parsed.timestamp,
                        note     = "Auto-detected via SMS"
                    )
                    repo.addTransaction(transaction)
                    sendDetectionNotification(context, parsed)
                }

                // 2. Parse balance for auto-reconciliation (even without a transaction match)
                val smsBalance = UpiSmsParser.parseBalance(body)
                if (smsBalance != null) {
                    repo.autoReconcileBalance(smsBalance)
                }
            }
        }
    }

    private fun sendDetectionNotification(context: Context, tx: UpiSmsParser.ParsedTransaction) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pending = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val sign = if (tx.type == "EXPENSE") "-" else "+"
        val notification = NotificationCompat.Builder(context, FinanceApplication.BUDGET_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_sync)
            .setContentTitle("UPI Transaction Detected")
            .setContentText("${sign}₹${"%.0f".format(tx.amount)} · ${tx.merchant}")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pending)
            .setAutoCancel(true)
            .build()

        val nm = context.getSystemService(NotificationManager::class.java)
        nm.notify(("sms_${tx.timestamp}").hashCode(), notification)
    }
}
