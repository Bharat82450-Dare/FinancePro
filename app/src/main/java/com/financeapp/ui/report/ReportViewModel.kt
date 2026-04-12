package com.financeapp.ui.report

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.financeapp.data.model.CategorySpending
import com.financeapp.data.repository.FinanceRepository
import com.financeapp.pdf.PdfExportUtility
import com.financeapp.utils.SessionManager
import com.github.mikephil.charting.data.Entry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

data class MonthlySummary(
    val totalIncome: Double,
    val totalExpense: Double,
    val netBalance: Double,
    val monthDisplay: String
)

class ReportViewModel(
    private val repository: FinanceRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val now = Calendar.getInstance()

    // Pair(month 1-12, year)
    private val _selectedMonth = MutableStateFlow(
        Pair(now.get(Calendar.MONTH) + 1, now.get(Calendar.YEAR))
    )
    val selectedMonth: StateFlow<Pair<Int, Int>> = _selectedMonth

    val categoryBreakdown: StateFlow<List<CategorySpending>> = _selectedMonth
        .flatMapLatest { (month, year) ->
            repository.getCategorySpendingForMonth(
                String.format("%02d", month),
                year.toString()
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val monthlySummary: StateFlow<MonthlySummary> = _selectedMonth
        .flatMapLatest { (month, year) ->
            val monthStr = String.format("%02d", month)
            val yearStr = year.toString()
            val start = getMonthStart(month, year)
            val end = getMonthEnd(month, year)
            combine(
                repository.getTransactionsInRange(start, end),
                repository.getCategorySpendingForMonth(monthStr, yearStr)
            ) { transactions, _ ->
                val income = transactions.filter { it.type == "INCOME" }.sumOf { it.amount }
                val expense = transactions.filter { it.type == "EXPENSE" }.sumOf { it.amount }
                MonthlySummary(income, expense, income - expense, getMonthDisplay(month, year))
            }
        }
        .stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000),
            MonthlySummary(0.0, 0.0, 0.0, "")
        )

    // 6-month trend for LineChart: entries indexed 0..5 (oldest to newest)
    val monthlyTrend: StateFlow<List<Entry>> = _selectedMonth
        .flatMapLatest { (month, year) ->
            repository.getMonthlyExpenseTotals(year.toString())
                .map { totals ->
                    // Build 6 months ending at selected month
                    val months = buildLast6Months(month, year)
                    months.mapIndexed { index, (m, _) ->
                        val monthStr = String.format("%02d", m)
                        val total = totals.find { it.month == monthStr }?.total ?: 0.0
                        Entry(index.toFloat(), total.toFloat())
                    }
                }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectPreviousMonth() {
        val (month, year) = _selectedMonth.value
        if (month == 1) _selectedMonth.value = Pair(12, year - 1)
        else _selectedMonth.value = Pair(month - 1, year)
    }

    fun selectNextMonth() {
        val (month, year) = _selectedMonth.value
        val nowMonth = now.get(Calendar.MONTH) + 1
        val nowYear = now.get(Calendar.YEAR)
        if (year > nowYear || (year == nowYear && month >= nowMonth)) return
        if (month == 12) _selectedMonth.value = Pair(1, year + 1)
        else _selectedMonth.value = Pair(month + 1, year)
    }

    fun generatePdfReport(context: Context, onComplete: (Uri?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val (month, year) = _selectedMonth.value
            val start = getMonthStart(month, year)
            val end = getMonthEnd(month, year)
            val transactions = mutableListOf<com.financeapp.data.entities.Transaction>()
            repository.getTransactionsInRange(start, end).collect { transactions.addAll(it) }

            val income = transactions.filter { it.type == "INCOME" }.sumOf { it.amount }
            val expense = transactions.filter { it.type == "EXPENSE" }.sumOf { it.amount }
            val categoryBreakdownList = transactions
                .filter { it.type == "EXPENSE" }
                .groupBy { it.category }
                .map { (cat, txns) -> CategorySpending(cat, txns.sumOf { it.amount }) }

            val data = PdfExportUtility.ReportData(
                month = String.format("%02d", month),
                year = year.toString(),
                totalIncome = income,
                totalExpense = expense,
                netBalance = income - expense,
                categoryBreakdown = categoryBreakdownList,
                transactions = transactions
            )
            val uri = PdfExportUtility.generateReport(context, data)
            launch(Dispatchers.Main) { onComplete(uri) }
        }
    }

    private fun getMonthStart(month: Int, year: Int): Long {
        val cal = Calendar.getInstance()
        cal.set(year, month - 1, 1, 0, 0, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    private fun getMonthEnd(month: Int, year: Int): Long {
        val cal = Calendar.getInstance()
        cal.set(year, month - 1, 1, 23, 59, 59)
        cal.set(Calendar.MILLISECOND, 999)
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
        return cal.timeInMillis
    }

    private fun buildLast6Months(month: Int, year: Int): List<Pair<Int, Int>> {
        val result = mutableListOf<Pair<Int, Int>>()
        var m = month
        var y = year
        repeat(6) {
            result.add(0, Pair(m, y))
            m--
            if (m == 0) { m = 12; y-- }
        }
        return result
    }

    fun getMonthDisplay(month: Int, year: Int): String {
        val monthNames = arrayOf(
            "Jan", "Feb", "Mar", "Apr", "May", "Jun",
            "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
        )
        return "${monthNames[month - 1]} $year"
    }

    fun getLast6MonthLabels(): List<String> {
        val (month, year) = _selectedMonth.value
        val monthNames = arrayOf(
            "Jan", "Feb", "Mar", "Apr", "May", "Jun",
            "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
        )
        return buildLast6Months(month, year).map { (m, _) -> monthNames[m - 1] }
    }
}
