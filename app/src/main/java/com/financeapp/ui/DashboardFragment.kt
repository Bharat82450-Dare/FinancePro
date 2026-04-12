package com.financeapp.ui

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.financeapp.R
import com.financeapp.data.database.FinanceDatabase
import com.financeapp.data.repository.FinanceRepository
import com.financeapp.databinding.FragmentDashboardBinding
import com.financeapp.viewmodel.DashboardViewModel
import com.financeapp.viewmodel.ViewModelFactory
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Locale

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: DashboardViewModel
    private val adapter by lazy { TransactionAdapter { /* Handle item click */ } }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViewModel()
        setupRecyclerView()
        setupChart()
        observeViewModel()

        binding.addTransactionFab.setOnClickListener {
            findNavController().navigate(R.id.nav_add_transaction)
        }

        binding.seeAllText.setOnClickListener {
            findNavController().navigate(R.id.nav_history)
        }

        binding.btnSync.setOnClickListener {
            viewModel.syncData()
            Toast.makeText(context, "Syncing to cloud...", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupViewModel() {
        val database = FinanceDatabase.getDatabase(requireContext())
        val repository = FinanceRepository(
            database.transactionDao(),
            database.categoryDao(),
            database.budgetDao(),
            database.monthlyBudgetTargetDao()
        )
        val factory = ViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[DashboardViewModel::class.java]
    }

    private fun setupRecyclerView() {
        binding.recentRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.recentRecycler.adapter = adapter
    }

    private fun setupChart() {
        binding.spendingChart.apply {
            description.isEnabled = false
            setDrawGridBackground(false)
            setDrawBarShadow(false)
            setDrawValueAboveBar(true)
            setPinchZoom(false)
            xAxis.isEnabled = false
            axisRight.isEnabled = false
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.allTransactions.collectLatest { transactions ->
                adapter.submitList(transactions.take(5))
                updateChart(transactions)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.totalIncome.collectLatest { income ->
                if (_binding == null) return@collectLatest
                binding.incomeText.text = String.format(Locale.getDefault(), "₹%.0f", income ?: 0.0)
                updateBalance()
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.totalExpense.collectLatest { expense ->
                if (_binding == null) return@collectLatest
                binding.expenseText.text = String.format(Locale.getDefault(), "₹%.0f", expense ?: 0.0)
                updateBalance()
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.monthlyBudgetProgress.collectLatest { progress ->
                if (_binding == null) return@collectLatest
                if (progress == null) {
                    binding.budgetProgressCard.visibility = View.GONE
                } else {
                    binding.budgetProgressCard.visibility = View.VISIBLE
                    binding.budgetProgressSpent.text =
                        "₹${"%.0f".format(progress.totalSpent)} of ₹${"%.0f".format(progress.totalLimit)}"
                    val pct = progress.percentUsed.coerceIn(0.0, 100.0).toInt()
                    binding.budgetProgressBar.progress = pct
                    binding.budgetProgressPercent.text = "$pct% used"

                    val tintColor = when {
                        progress.percentUsed >= 100 -> Color.parseColor("#F44336")
                        progress.percentUsed >= 80 -> Color.parseColor("#FF9800")
                        else -> ContextCompat.getColor(requireContext(), R.color.primary)
                    }
                    binding.budgetProgressBar.progressTintList =
                        android.content.res.ColorStateList.valueOf(tintColor)
                    binding.budgetProgressPercent.setTextColor(tintColor)
                }
            }
        }
    }

    private fun updateBalance() {
        val income = viewModel.totalIncome.value ?: 0.0
        val expense = viewModel.totalExpense.value ?: 0.0
        binding.totalBalanceText.text = String.format(Locale.getDefault(), "₹%.2f", income - expense)
    }

    private fun updateChart(transactions: List<com.financeapp.data.entities.Transaction>) {
        if (transactions.isEmpty()) return
        val entries = transactions.take(7).mapIndexed { index, transaction ->
            BarEntry(index.toFloat(), transaction.amount.toFloat())
        }
        val dataSet = BarDataSet(entries, "Spending")
        dataSet.color = Color.parseColor("#2D6CDF")
        dataSet.valueTextColor = Color.BLACK
        dataSet.valueTextSize = 10f
        binding.spendingChart.data = BarData(dataSet)
        binding.spendingChart.invalidate()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
