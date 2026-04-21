package com.financeapp.ui

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.financeapp.R
import com.financeapp.budget.TrackingLevel
import com.financeapp.data.repository.RepositoryProvider
import com.financeapp.databinding.FragmentDashboardBinding
import com.financeapp.viewmodel.DashboardViewModel
import com.financeapp.viewmodel.ViewModelFactory
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Locale

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: DashboardViewModel
    private val adapter by lazy { TransactionAdapter { /* Handle item click */ } }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
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
            Toast.makeText(context, "Syncing to cloud…", Toast.LENGTH_SHORT).show()
        }
        binding.btnSetBalance.setOnClickListener { showSetBalanceDialog() }
        binding.viewAllGoalsText.setOnClickListener {
            findNavController().navigate(R.id.nav_goals)
        }
    }

    // ── Setup ────────────────────────────────────────────────────────────────

    private fun setupViewModel() {
        val repository = RepositoryProvider.get(requireContext())
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

    // ── Observers ────────────────────────────────────────────────────────────

    private fun observeViewModel() {
        // Recent transactions + chart
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.allTransactions.collectLatest { transactions ->
                adapter.submitList(transactions.take(5))
                updateChart(transactions)
            }
        }

        // Running balance (from BalanceManager)
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.currentBalance.collectLatest { balance ->
                if (_binding == null) return@collectLatest
                val amount = balance?.runningBalance ?: 0.0
                binding.totalBalanceText.text =
                    String.format(Locale.getDefault(), "₹%.2f", amount)
                // Badge: "Verified" if user declared, else "Calculated"
                val isVerified = balance?.declaredBalance != null && balance.declaredBalance > 0
                binding.balanceBadgeText.text = if (isVerified) "✓ Verified" else "Calculated"
                binding.balanceBadgeText.setTextColor(
                    if (isVerified) Color.parseColor("#4CAF50") else Color.parseColor("#FF9800")
                )
            }
        }

        // Income / Expense summary
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.totalIncome.collectLatest { income ->
                if (_binding == null) return@collectLatest
                binding.incomeText.text = String.format(Locale.getDefault(), "₹%.0f", income ?: 0.0)
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.totalExpense.collectLatest { expense ->
                if (_binding == null) return@collectLatest
                binding.expenseText.text = String.format(Locale.getDefault(), "₹%.0f", expense ?: 0.0)
            }
        }

        // Monthly budget progress
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
                        progress.percentUsed >= 80  -> Color.parseColor("#FF9800")
                        else -> ContextCompat.getColor(requireContext(), R.color.primary)
                    }
                    binding.budgetProgressBar.progressTintList = ColorStateList.valueOf(tintColor)
                    binding.budgetProgressPercent.setTextColor(tintColor)
                }
            }
        }

        // Auto-tracking status card
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.trackingStatus.collectLatest { status ->
                if (_binding == null) return@collectLatest
                if (status == null) {
                    binding.trackingCard.visibility = View.GONE
                } else {
                    binding.trackingCard.visibility = View.VISIBLE
                    binding.trackingStatusText.text = status.summary
                    val (icon, color) = when (status.level) {
                        TrackingLevel.ON_TRACK       -> Pair("🟢", Color.parseColor("#4CAF50"))
                        TrackingLevel.SLIGHTLY_BEHIND -> Pair("🟡", Color.parseColor("#FF9800"))
                        TrackingLevel.OFF_TRACK      -> Pair("🔴", Color.parseColor("#F44336"))
                    }
                    binding.trackingIcon.text = icon
                    binding.trackingStatusText.setTextColor(color)
                    // Savings progress bar
                    val savPct = if (status.savingsTarget > 0)
                        (status.savingsProgress / status.savingsTarget * 100).toInt().coerceIn(0, 100)
                    else 0
                    binding.savingsProgressBar.progress = savPct
                    binding.savingsProgressText.text =
                        "Saved ₹${"%.0f".format(status.savingsProgress)} of ₹${"%.0f".format(status.savingsTarget)} target"
                    // Expense progress bar
                    val expPct = if (status.expenseLimit > 0)
                        (status.expenseProgress / status.expenseLimit * 100).toInt().coerceIn(0, 100)
                    else 0
                    binding.expenseProgressBar.progress = expPct
                    binding.expenseProgressText.text =
                        "Spent ₹${"%.0f".format(status.expenseProgress)} of ₹${"%.0f".format(status.expenseLimit)} limit"
                    binding.projectedBalanceText.text =
                        "Projected month-end: ₹${"%.0f".format(status.projectedMonthEndBalance)}"
                }
            }
        }

        // Goals mini-card
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.dashboardGoalStatuses.collectLatest { statuses ->
                if (_binding == null) return@collectLatest
                if (statuses.isEmpty()) {
                    binding.goalsCard.visibility = View.GONE
                } else {
                    binding.goalsCard.visibility = View.VISIBLE
                    // Goal 1
                    if (statuses.isNotEmpty()) {
                        val g1 = statuses[0]
                        binding.goal1Name.text = g1.goal.name
                        binding.goal1Progress.progress = g1.progressPercent.toInt()
                        binding.goal1Amount.text =
                            "₹${"%.0f".format(g1.goal.savedAmount)}/${"%.0f".format(g1.goal.targetAmount)}"
                        binding.goalItem1.visibility = View.VISIBLE
                    } else binding.goalItem1.visibility = View.GONE
                    // Goal 2
                    if (statuses.size > 1) {
                        val g2 = statuses[1]
                        binding.goal2Name.text = g2.goal.name
                        binding.goal2Progress.progress = g2.progressPercent.toInt()
                        binding.goal2Amount.text =
                            "₹${"%.0f".format(g2.goal.savedAmount)}/${"%.0f".format(g2.goal.targetAmount)}"
                        binding.goalItem2.visibility = View.VISIBLE
                    } else binding.goalItem2.visibility = View.GONE
                }
            }
        }
    }

    // ── Actions ───────────────────────────────────────────────────────────────

    private fun showSetBalanceDialog() {
        val input = EditText(requireContext()).apply {
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
            hint = "e.g. 50000"
            setPadding(56, 40, 56, 40)
        }
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Set Your Balance")
            .setMessage("Enter the exact amount currently in your accounts (₹):")
            .setView(input)
            .setPositiveButton("Save") { _, _ ->
                val amount = input.text.toString().toDoubleOrNull()
                if (amount != null && amount >= 0) {
                    viewModel.setUserBalance(amount)
                    Toast.makeText(context, "Balance set to ₹${"%.2f".format(amount)}", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Enter a valid amount", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // ── Chart ─────────────────────────────────────────────────────────────────

    private fun updateChart(transactions: List<com.financeapp.data.entities.Transaction>) {
        if (transactions.isEmpty()) return
        val entries = transactions.take(7).mapIndexed { index, tx ->
            BarEntry(index.toFloat(), tx.amount.toFloat())
        }
        val dataSet = BarDataSet(entries, "Spending").apply {
            color = Color.parseColor("#2D6CDF")
            valueTextColor = Color.BLACK
            valueTextSize = 10f
        }
        binding.spendingChart.data = BarData(dataSet)
        binding.spendingChart.invalidate()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
