package com.financeapp.budget

import com.financeapp.data.entities.UserBalance
import com.financeapp.data.repository.FinanceRepository
import kotlinx.coroutines.flow.Flow

/**
 * Centralises all balance-related operations. Keeps fragments/ViewModels thin by
 * providing a single, clear surface for reading and writing the user's balance.
 */
class BalanceManager(private val repository: FinanceRepository) {

    /** Live stream of the most recent [UserBalance] record. Null if never set. */
    val currentBalance: Flow<UserBalance?> = repository.getCurrentBalance()

    /**
     * Declare the user's real balance (e.g. from a bank statement).
     * This resets the running-balance baseline to [amount].
     */
    suspend fun setBalance(amount: Double, note: String = "User declared") {
        repository.setBalance(amount, note)
    }

    /**
     * One-shot read of the current running balance value (for Workers / non-UI use).
     * Returns 0.0 if no balance record exists.
     */
    suspend fun getRunningBalance(): Double =
        repository.getCurrentBalanceOnce()?.runningBalance ?: 0.0

    /**
     * Correct the running balance when an SMS reports a different figure.
     * Only updates if the difference is > ₹0.01.
     */
    suspend fun reconcileFromSms(smsBalance: Double) {
        repository.autoReconcileBalance(smsBalance)
    }

    /** Full history of balance declarations (newest first). */
    val balanceHistory: Flow<List<UserBalance>> = repository.getBalanceHistory()
}
