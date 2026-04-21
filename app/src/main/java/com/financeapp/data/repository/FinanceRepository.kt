package com.financeapp.data.repository

import com.financeapp.data.dao.*
import com.financeapp.data.entities.*
import com.financeapp.data.model.CategorySpending
import com.financeapp.data.model.DailyTotals
import com.financeapp.data.model.MonthlyTotal
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await

class FinanceRepository(
    private val transactionDao: TransactionDao,
    private val categoryDao: CategoryDao,
    private val budgetDao: BudgetDao,
    private val monthlyBudgetTargetDao: MonthlyBudgetTargetDao,
    private val balanceDao: BalanceDao,
    private val savingsGoalDao: SavingsGoalDao,
    private val dailySnapshotDao: DailySnapshotDao,
    private val financialPlanDao: FinancialPlanDao,
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    val allTransactions: Flow<List<Transaction>> = transactionDao.getAllTransactions()
    val totalIncome: Flow<Double?> = transactionDao.getTotalIncome()
    val totalExpense: Flow<Double?> = transactionDao.getTotalExpense()

    suspend fun addTransaction(transaction: Transaction) {
        val localId = transactionDao.insertTransaction(transaction)
        val adjustment = if (transaction.type == "INCOME") transaction.amount else -transaction.amount
        balanceDao.adjustRunningBalance(adjustment)
        syncToFirestore(transaction.copy(id = localId))
    }

    private suspend fun syncToFirestore(transaction: Transaction) {
        val user = auth.currentUser ?: return
        try {
            val docRef = if (transaction.firebaseId != null) {
                firestore.collection("users").document(user.uid)
                    .collection("transactions").document(transaction.firebaseId)
            } else {
                firestore.collection("users").document(user.uid)
                    .collection("transactions").document()
            }
            val data = hashMapOf(
                "title" to transaction.title,
                "amount" to transaction.amount,
                "type" to transaction.type,
                "category" to transaction.category,
                "date" to transaction.date,
                "note" to transaction.note
            )
            docRef.set(data).await()
            transactionDao.updateTransaction(transaction.copy(isSynced = true, firebaseId = docRef.id))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun syncPendingData() {
        val unsynced = transactionDao.getUnsyncedTransactions()
        unsynced.forEach { syncToFirestore(it) }
    }

    fun getMonthlyExpenseTotals(year: String): Flow<List<MonthlyTotal>> =
        transactionDao.getMonthlyExpenseTotals(year)

    fun getCategorySpendingForMonth(month: String, year: String): Flow<List<CategorySpending>> =
        transactionDao.getCategorySpendingForMonth(month, year)

    fun getTotalExpenseForMonth(month: String, year: String): Flow<Double> =
        transactionDao.getTotalExpenseForMonth(month, year)

    fun getTotalIncomeForMonth(month: String, year: String): Flow<Double> =
        transactionDao.getTotalIncomeForMonth(month, year)

    fun getTransactionsInRange(startDate: Long, endDate: Long): Flow<List<Transaction>> =
        transactionDao.getTransactionsInRange(startDate, endDate)

    suspend fun getTransactionsSince(timestamp: Long): List<Transaction> =
        transactionDao.getTransactionsSince(timestamp)

    suspend fun getDailyTotals(dayStart: Long, dayEnd: Long): DailyTotals =
        transactionDao.getDailyTotals(dayStart, dayEnd)

    // Category methods
    fun getAllCategories() = categoryDao.getAllCategories()
    suspend fun addCategory(category: Category) = categoryDao.insertCategory(category)

    // Budget methods
    fun getBudgetsForMonth(month: Int, year: Int): Flow<List<Budget>> =
        budgetDao.getBudgetsForMonth(month, year)

    suspend fun getBudgetByCategory(category: String, month: Int, year: Int): Budget? =
        budgetDao.getBudgetByCategory(category, month, year)

    suspend fun insertBudget(budget: Budget) = budgetDao.insertBudget(budget)

    suspend fun deleteBudget(budget: Budget) = budgetDao.deleteBudget(budget)

    // Monthly budget target methods
    fun getMonthlyTargetForMonth(month: Int, year: Int): Flow<MonthlyBudgetTarget?> =
        monthlyBudgetTargetDao.getTargetForMonth(month, year)

    suspend fun getMonthlyTargetOnce(month: Int, year: Int): MonthlyBudgetTarget? =
        monthlyBudgetTargetDao.getTargetForMonthOnce(month, year)

    suspend fun insertMonthlyTarget(target: MonthlyBudgetTarget) =
        monthlyBudgetTargetDao.insertTarget(target)

    // Balance methods
    fun getCurrentBalance(): Flow<UserBalance?> = balanceDao.getLatestBalance()

    suspend fun getCurrentBalanceOnce(): UserBalance? = balanceDao.getLatestBalanceOnce()

    suspend fun setBalance(declaredBalance: Double, note: String = "") {
        val balance = UserBalance(
            declaredBalance = declaredBalance,
            effectiveDate = System.currentTimeMillis(),
            runningBalance = declaredBalance,
            note = note
        )
        balanceDao.insertBalance(balance)
    }

    suspend fun autoReconcileBalance(smsReportedBalance: Double) {
        val current = balanceDao.getLatestBalanceOnce() ?: return
        val diff = kotlin.math.abs(current.runningBalance - smsReportedBalance)
        if (diff > 0.01) {
            balanceDao.updateBalance(current.copy(runningBalance = smsReportedBalance))
        }
    }

    fun getBalanceHistory(): Flow<List<UserBalance>> = balanceDao.getBalanceHistory()

    // Savings goal methods
    fun getActiveGoals(): Flow<List<SavingsGoal>> = savingsGoalDao.getActiveGoals()

    fun getAllGoals(): Flow<List<SavingsGoal>> = savingsGoalDao.getAllGoals()

    suspend fun addGoal(goal: SavingsGoal): Long = savingsGoalDao.insertGoal(goal)

    suspend fun updateGoal(goal: SavingsGoal) = savingsGoalDao.updateGoal(goal)

    suspend fun deleteGoal(id: Long) = savingsGoalDao.deleteGoal(id)

    suspend fun allocateToGoal(goalId: Long, amount: Double) {
        val goal = savingsGoalDao.getGoalById(goalId) ?: return
        savingsGoalDao.updateGoal(goal.copy(savedAmount = goal.savedAmount + amount))
    }

    fun getTotalRemainingGoalAmount(): Flow<Double> = savingsGoalDao.getTotalRemainingGoalAmount()

    // Snapshot methods
    fun getRecentSnapshots(days: Int): Flow<List<DailySnapshot>> =
        dailySnapshotDao.getRecentSnapshots(days)

    fun getSnapshotsInRange(startDate: Long, endDate: Long): Flow<List<DailySnapshot>> =
        dailySnapshotDao.getSnapshotsInRange(startDate, endDate)

    suspend fun insertSnapshot(snapshot: DailySnapshot) =
        dailySnapshotDao.insertSnapshot(snapshot)

    suspend fun getSnapshotForDate(date: Long): DailySnapshot? =
        dailySnapshotDao.getSnapshotForDate(date)

    // Financial plan methods
    fun getActivePlan(): Flow<FinancialPlan?> = financialPlanDao.getActivePlan()

    suspend fun getActivePlanOnce(): FinancialPlan? = financialPlanDao.getActivePlanOnce()

    suspend fun savePlan(plan: FinancialPlan) {
        financialPlanDao.deactivateAllPlans()
        financialPlanDao.insertPlan(plan)
    }
}
