package com.financeapp.data.repository

import com.financeapp.data.dao.*
import com.financeapp.data.entities.Budget
import com.financeapp.data.entities.MonthlyBudgetTarget
import com.financeapp.data.entities.Transaction
import com.financeapp.data.model.CategorySpending
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
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    val allTransactions: Flow<List<Transaction>> = transactionDao.getAllTransactions()
    val totalIncome: Flow<Double?> = transactionDao.getTotalIncome()
    val totalExpense: Flow<Double?> = transactionDao.getTotalExpense()

    suspend fun addTransaction(transaction: Transaction) {
        val localId = transactionDao.insertTransaction(transaction)
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

            transactionDao.updateTransaction(transaction.copy(
                isSynced = true,
                firebaseId = docRef.id
            ))
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

    fun getTransactionsInRange(startDate: Long, endDate: Long): Flow<List<Transaction>> =
        transactionDao.getTransactionsInRange(startDate, endDate)

    // Category methods
    fun getAllCategories() = categoryDao.getAllCategories()
    suspend fun addCategory(category: com.financeapp.data.entities.Category) = categoryDao.insertCategory(category)

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
}
