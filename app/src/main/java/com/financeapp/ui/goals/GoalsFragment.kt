package com.financeapp.ui.goals

import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.financeapp.budget.GoalStatus
import com.financeapp.data.repository.RepositoryProvider
import com.financeapp.databinding.FragmentGoalsBinding
import com.financeapp.viewmodel.GoalsViewModel
import com.financeapp.viewmodel.ViewModelFactory
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class GoalsFragment : Fragment() {

    private var _binding: FragmentGoalsBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: GoalsViewModel
    private lateinit var adapter: GoalAdapter

    private val goalColors = intArrayOf(
        Color.parseColor("#2D6CDF"),
        Color.parseColor("#4CAF50"),
        Color.parseColor("#FF9800"),
        Color.parseColor("#E91E63"),
        Color.parseColor("#9C27B0"),
        Color.parseColor("#00BCD4"),
        Color.parseColor("#795548"),
        Color.parseColor("#607D8B")
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGoalsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViewModel()
        setupRecyclerView()
        observeViewModel()

        binding.fabAddGoal.setOnClickListener { showAddGoalDialog() }
        binding.btnEditPlan.setOnClickListener { showEditPlanDialog() }
    }

    private fun setupViewModel() {
        val repository = RepositoryProvider.get(requireContext())
        val factory = ViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[GoalsViewModel::class.java]
    }

    private fun setupRecyclerView() {
        adapter = GoalAdapter(
            onAllocate = { showAllocateDialog(it) },
            onDelete = { confirmDelete(it) }
        )
        binding.goalsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.goalsRecyclerView.adapter = adapter
    }

    private fun observeViewModel() {
        // Goals list
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.goalStatuses.collectLatest { statuses ->
                adapter.submitList(statuses)
                binding.emptyGoalsText.visibility =
                    if (statuses.isEmpty()) View.VISIBLE else View.GONE
                binding.goalsRecyclerView.visibility =
                    if (statuses.isEmpty()) View.GONE else View.VISIBLE
            }
        }

        // Financial plan
        viewLifecycleOwner.lifecycleScope.launch {
            val repo = RepositoryProvider.get(requireContext())
            repo.getActivePlan().collectLatest { plan ->
                if (_binding == null) return@collectLatest
                if (plan == null) {
                    binding.noPlanText.visibility = View.VISIBLE
                    binding.planDetailsGroup.visibility = View.GONE
                    binding.btnEditPlan.text = "Set Up"
                } else {
                    binding.noPlanText.visibility = View.GONE
                    binding.planDetailsGroup.visibility = View.VISIBLE
                    binding.btnEditPlan.text = "Edit"
                    binding.planIncomeText.text = "₹${"%.0f".format(plan.monthlyIncomeEstimate)}"
                    binding.planExpenseText.text = "₹${"%.0f".format(plan.monthlyExpenseLimit)}"
                    binding.planSavingsText.text = "₹${"%.0f".format(plan.monthlySavingsTarget)}"
                }
            }
        }
    }

    // ── Dialogs ───────────────────────────────────────────────────────────────

    private fun showEditPlanDialog() {
        val padding = (20 * resources.displayMetrics.density).toInt()
        val container = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(padding, padding / 2, padding, 0)
        }

        val incomeInput = EditText(requireContext()).apply {
            hint = "Monthly income (₹)"
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        }
        val expenseInput = EditText(requireContext()).apply {
            hint = "Monthly expense limit (₹)"
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        }
        val savingsInput = EditText(requireContext()).apply {
            hint = "Monthly savings target (₹)"
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        }
        container.addView(incomeInput)
        container.addView(expenseInput)
        container.addView(savingsInput)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Financial Plan")
            .setMessage("Set your monthly targets. The app will auto-track your progress.")
            .setView(container)
            .setPositiveButton("Save") { _, _ ->
                val income = incomeInput.text.toString().toDoubleOrNull()
                val expense = expenseInput.text.toString().toDoubleOrNull()
                val savings = savingsInput.text.toString().toDoubleOrNull()
                if (income != null && expense != null && savings != null &&
                    income > 0 && expense > 0 && savings >= 0
                ) {
                    viewLifecycleOwner.lifecycleScope.launch {
                        val repo = RepositoryProvider.get(requireContext())
                        repo.savePlan(
                            com.financeapp.data.entities.FinancialPlan(
                                monthlyIncomeEstimate = income,
                                monthlyExpenseLimit = expense,
                                monthlySavingsTarget = savings
                            )
                        )
                        // Also sync expense limit to MonthlyBudgetTarget
                        val cal = java.util.Calendar.getInstance()
                        repo.insertMonthlyTarget(
                            com.financeapp.data.entities.MonthlyBudgetTarget(
                                totalLimit = expense,
                                month = cal.get(java.util.Calendar.MONTH) + 1,
                                year = cal.get(java.util.Calendar.YEAR)
                            )
                        )
                    }
                    Toast.makeText(context, "Plan saved!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Enter valid amounts", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showAddGoalDialog() {
        val padding = (20 * resources.displayMetrics.density).toInt()
        val container = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(padding, padding / 2, padding, 0)
        }

        val nameInput = EditText(requireContext()).apply { hint = "Goal name (e.g. Vacation)" }
        val amountInput = EditText(requireContext()).apply {
            hint = "Target amount (₹)"
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        }
        val dateBtn = com.google.android.material.button.MaterialButton(requireContext()).apply {
            text = "Pick target date"
        }
        container.addView(nameInput)
        container.addView(amountInput)
        container.addView(dateBtn)

        var selectedDate: Long? = null
        val fmt = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

        dateBtn.setOnClickListener {
            val picker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Goal Deadline")
                .build()
            picker.addOnPositiveButtonClickListener { ms ->
                selectedDate = ms
                dateBtn.text = "Deadline: ${fmt.format(Date(ms))}"
            }
            picker.show(parentFragmentManager, "GOAL_DATE")
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("New Savings Goal")
            .setView(container)
            .setPositiveButton("Create") { _, _ ->
                val name = nameInput.text.toString().trim()
                val amount = amountInput.text.toString().toDoubleOrNull()
                val date = selectedDate
                if (name.isNotEmpty() && amount != null && amount > 0 && date != null) {
                    val colorIndex = (adapter.itemCount) % goalColors.size
                    viewModel.addGoal(name, amount, date, color = goalColors[colorIndex])
                    Toast.makeText(context, "Goal created!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Fill all fields and pick a date", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showAllocateDialog(status: GoalStatus) {
        val input = EditText(requireContext()).apply {
            hint = "Amount to allocate (₹)"
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
            val pad = (20 * resources.displayMetrics.density).toInt()
            setPadding(pad, pad / 2, pad, 0)
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Fund: ${status.goal.name}")
            .setMessage("Remaining: ₹${"%.0f".format(status.amountRemaining)}")
            .setView(input)
            .setPositiveButton("Allocate") { _, _ ->
                val amount = input.text.toString().toDoubleOrNull()
                if (amount != null && amount > 0) {
                    viewModel.allocateFunds(status.goal.id, amount)
                    Toast.makeText(context, "₹${"%.0f".format(amount)} allocated!", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun confirmDelete(status: GoalStatus) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete Goal")
            .setMessage("Delete \"${status.goal.name}\"? This cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteGoal(status.goal.id)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
