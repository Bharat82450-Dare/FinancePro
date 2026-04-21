package com.financeapp.ui.budget

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.financeapp.budget.BudgetManager
import com.financeapp.data.repository.RepositoryProvider
import com.financeapp.databinding.DialogAddBudgetBinding
import com.financeapp.databinding.FragmentBudgetBinding
import com.financeapp.utils.SessionManager
import com.financeapp.viewmodel.ViewModelFactory
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import androidx.recyclerview.widget.LinearLayoutManager

class BudgetFragment : Fragment() {

    private var _binding: FragmentBudgetBinding? = null
    private val binding get() = _binding!!

    private val viewModel: BudgetViewModel by viewModels {
        val repo = RepositoryProvider.get(requireContext())
        val session = SessionManager(requireContext())
        val budgetManager = BudgetManager(repo, requireContext())
        ViewModelFactory(repo, session, budgetManager)
    }

    private lateinit var adapter: BudgetAdapter

    private val smsPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            saveSmsTrackingEnabled(true)
            Toast.makeText(requireContext(), "UPI auto-tracking enabled", Toast.LENGTH_SHORT).show()
        } else {
            binding.smsTrackSwitch.isChecked = false
            Toast.makeText(requireContext(), "SMS permission needed for auto-tracking", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBudgetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeViewModel()
        setupSmsTrackSwitch()
        binding.fabAddBudget.setOnClickListener { showAddBudgetDialog() }
        binding.btnSetTarget.setOnClickListener { showSetTargetDialog() }
    }

    private fun setupRecyclerView() {
        adapter = BudgetAdapter { budget -> viewModel.deleteBudget(budget) }
        binding.budgetRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.budgetRecyclerView.adapter = adapter
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.budgetStatuses.collectLatest { items ->
                adapter.submitList(items)
                binding.emptyState.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
                binding.budgetRecyclerView.visibility = if (items.isEmpty()) View.GONE else View.VISIBLE
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.overallBudget.collectLatest { summary ->
                if (summary == null) {
                    binding.noTargetText.visibility = View.VISIBLE
                    binding.targetSetGroup.visibility = View.GONE
                    binding.btnSetTarget.text = "Set Target"
                } else {
                    binding.noTargetText.visibility = View.GONE
                    binding.targetSetGroup.visibility = View.VISIBLE
                    binding.btnSetTarget.text = "Edit"

                    binding.totalSpentText.text = "₹${"%.0f".format(summary.totalSpent)}"
                    binding.totalLimitText.text = "of ₹${"%.0f".format(summary.totalLimit)}"

                    val progress = summary.percentUsed.coerceIn(0.0, 100.0).toInt()
                    binding.overallProgressBar.progress = progress
                    val tint = when {
                        summary.percentUsed >= 100 -> "#F44336"
                        summary.percentUsed >= 80 -> "#FF9800"
                        else -> null
                    }
                    if (tint != null) {
                        binding.overallProgressBar.progressTintList =
                            android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor(tint))
                    } else {
                        val primaryColor = ContextCompat.getColor(requireContext(), com.financeapp.R.color.primary)
                        binding.overallProgressBar.progressTintList =
                            android.content.res.ColorStateList.valueOf(primaryColor)
                    }

                    val remaining = summary.totalLimit - summary.totalSpent
                    binding.remainingText.text = "₹${"%.0f".format(remaining.coerceAtLeast(0.0))}"
                    binding.remainingText.setTextColor(
                        android.graphics.Color.parseColor(if (remaining <= 0) "#F44336" else "#4CAF50")
                    )

                    binding.projectedText.text = "₹${"%.0f".format(summary.projectedSpend)}"
                    binding.projectedText.setTextColor(
                        android.graphics.Color.parseColor(
                            if (summary.projectedSpend > summary.totalLimit) "#F44336" else "#2D6CDF"
                        )
                    )
                }
            }
        }
    }

    private fun setupSmsTrackSwitch() {
        // Restore saved state
        binding.smsTrackSwitch.isChecked = isSmsTrackingEnabled()

        binding.smsTrackSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                requestSmsPermission()
            } else {
                saveSmsTrackingEnabled(false)
                Toast.makeText(requireContext(), "UPI auto-tracking disabled", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun requestSmsPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECEIVE_SMS)
            == PackageManager.PERMISSION_GRANTED) {
            saveSmsTrackingEnabled(true)
            return
        }
        AlertDialog.Builder(requireContext())
            .setTitle("Enable UPI Auto-Track")
            .setMessage("This app will read incoming bank SMS to automatically log your UPI transactions. No SMS data is sent externally.")
            .setPositiveButton("Allow") { _, _ ->
                smsPermissionLauncher.launch(Manifest.permission.RECEIVE_SMS)
            }
            .setNegativeButton("Cancel") { _, _ ->
                binding.smsTrackSwitch.isChecked = false
            }
            .show()
    }

    private fun showSetTargetDialog() {
        val current = viewModel.overallBudget.value?.totalLimit
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(android.R.layout.simple_list_item_1, null) // placeholder root
        val input = com.google.android.material.textfield.TextInputEditText(requireContext()).apply {
            hint = "Enter monthly budget (₹)"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
            if (current != null) setText("%.0f".format(current))
        }
        val container = android.widget.FrameLayout(requireContext()).apply {
            val padding = (20 * resources.displayMetrics.density).toInt()
            setPadding(padding, padding / 2, padding, 0)
            addView(input)
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Set Monthly Budget")
            .setView(container)
            .setPositiveButton("Save") { _, _ ->
                val amount = input.text.toString().toDoubleOrNull()
                if (amount != null && amount > 0) {
                    viewModel.setMonthlyTarget(amount)
                } else {
                    Toast.makeText(requireContext(), "Enter a valid amount", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
        input.requestFocus()
    }

    private fun showAddBudgetDialog() {
        val dialogBinding = DialogAddBudgetBinding.inflate(layoutInflater)
        val predefinedCategories = listOf(
            "Food", "Transport", "Shopping", "Entertainment",
            "Health", "Education", "Utilities", "Other"
        )
        val categoryAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            predefinedCategories
        )
        dialogBinding.categoryDropdown.setAdapter(categoryAdapter)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .create()

        dialogBinding.saveButton.setOnClickListener {
            val category = dialogBinding.categoryDropdown.text.toString().trim()
            val limitText = dialogBinding.amountInput.text.toString().trim()
            if (category.isNotEmpty() && limitText.isNotEmpty()) {
                val limit = limitText.toDoubleOrNull()
                if (limit != null && limit > 0) {
                    viewModel.addBudget(category, limit)
                    dialog.dismiss()
                } else {
                    dialogBinding.amountInputLayout.error = "Enter a valid amount"
                }
            } else {
                if (category.isEmpty()) dialogBinding.categoryInputLayout.error = "Select a category"
                if (limitText.isEmpty()) dialogBinding.amountInputLayout.error = "Enter an amount"
            }
        }
        dialogBinding.cancelButton.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun isSmsTrackingEnabled(): Boolean =
        requireContext().getSharedPreferences("finance_prefs", 0)
            .getBoolean("sms_tracking_enabled", false)

    private fun saveSmsTrackingEnabled(enabled: Boolean) {
        requireContext().getSharedPreferences("finance_prefs", 0)
            .edit().putBoolean("sms_tracking_enabled", enabled).apply()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
