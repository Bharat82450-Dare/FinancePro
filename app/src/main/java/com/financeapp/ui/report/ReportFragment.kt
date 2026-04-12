package com.financeapp.ui.report

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.financeapp.data.database.FinanceDatabase
import com.financeapp.data.repository.FinanceRepository
import com.financeapp.databinding.FragmentReportBinding
import com.financeapp.utils.SessionManager
import com.financeapp.viewmodel.ViewModelFactory
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ReportFragment : Fragment() {

    private var _binding: FragmentReportBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ReportViewModel by viewModels {
        val db = FinanceDatabase.getDatabase(requireContext())
        val repo = FinanceRepository(db.transactionDao(), db.categoryDao(), db.budgetDao(), db.monthlyBudgetTargetDao())
        val session = SessionManager(requireContext())
        ViewModelFactory(repo, session)
    }

    private lateinit var categoryAdapter: CategoryBreakdownAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReportBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupLineChart()
        setupCategoryRecyclerView()
        observeViewModel()

        binding.prevMonthButton.setOnClickListener { viewModel.selectPreviousMonth() }
        binding.nextMonthButton.setOnClickListener { viewModel.selectNextMonth() }
        binding.exportPdfButton.setOnClickListener { handleExportClick() }
    }

    private fun setupLineChart() {
        val chart = binding.spendingLineChart
        chart.description.isEnabled = false
        chart.legend.isEnabled = false
        chart.setTouchEnabled(false)
        chart.axisRight.isEnabled = false
        chart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        chart.xAxis.granularity = 1f
        chart.xAxis.setDrawGridLines(false)
        chart.axisLeft.setDrawGridLines(true)
        chart.axisLeft.gridColor = Color.parseColor("#E0E0E0")
        chart.setExtraOffsets(8f, 8f, 8f, 8f)
    }

    private fun setupCategoryRecyclerView() {
        categoryAdapter = CategoryBreakdownAdapter()
        binding.categoryBreakdownRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.categoryBreakdownRecyclerView.adapter = categoryAdapter
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.selectedMonth.collectLatest { (month, year) ->
                binding.monthYearLabel.text = viewModel.getMonthDisplay(month, year)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.monthlySummary.collectLatest { summary ->
                binding.summaryIncome.text = "₹${"%.0f".format(summary.totalIncome)}"
                binding.summaryExpense.text = "₹${"%.0f".format(summary.totalExpense)}"
                binding.summaryBalance.text = "₹${"%.0f".format(summary.netBalance)}"
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.monthlyTrend.collectLatest { entries ->
                updateLineChart(entries)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.categoryBreakdown.collectLatest { breakdown ->
                categoryAdapter.submitList(breakdown)
                binding.noCategoriesText.visibility =
                    if (breakdown.isEmpty()) View.VISIBLE else View.GONE
                binding.categoryBreakdownRecyclerView.visibility =
                    if (breakdown.isEmpty()) View.GONE else View.VISIBLE
            }
        }
    }

    private fun updateLineChart(entries: List<com.github.mikephil.charting.data.Entry>) {
        if (entries.isEmpty()) {
            binding.spendingLineChart.clear()
            return
        }
        val dataSet = LineDataSet(entries, "Expenses").apply {
            color = Color.parseColor("#2D6CDF")
            valueTextColor = Color.parseColor("#757575")
            lineWidth = 2.5f
            setCircleColor(Color.parseColor("#2D6CDF"))
            circleRadius = 4f
            setDrawCircleHole(true)
            circleHoleRadius = 2f
            mode = LineDataSet.Mode.CUBIC_BEZIER
            setDrawFilled(true)
            fillColor = Color.parseColor("#EAF1FF")
            fillAlpha = 180
            valueTextSize = 9f
        }
        binding.spendingLineChart.xAxis.valueFormatter =
            IndexAxisValueFormatter(viewModel.getLast6MonthLabels())
        binding.spendingLineChart.data = LineData(dataSet)
        binding.spendingLineChart.invalidate()
    }

    private fun handleExportClick() {
        binding.exportPdfButton.isEnabled = false
        binding.exportPdfButton.text = "Generating..."

        viewModel.generatePdfReport(requireContext()) { uri ->
            binding.exportPdfButton.isEnabled = true
            binding.exportPdfButton.text = "Export PDF Report"
            if (uri != null) {
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/pdf"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                startActivity(Intent.createChooser(shareIntent, "Share PDF Report"))
            } else {
                Toast.makeText(requireContext(), "Failed to generate PDF", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
