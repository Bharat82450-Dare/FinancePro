package com.financeapp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.financeapp.R
import com.financeapp.data.database.FinanceDatabase
import com.financeapp.data.repository.FinanceRepository
import com.financeapp.databinding.FragmentAddTransactionBinding
import com.financeapp.viewmodel.AddTransactionViewModel
import com.financeapp.viewmodel.ViewModelFactory
import com.google.android.material.datepicker.MaterialDatePicker
import java.text.SimpleDateFormat
import java.util.*

class AddTransactionFragment : Fragment() {

    private var _binding: FragmentAddTransactionBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: AddTransactionViewModel
    private var selectedDate: Long = System.currentTimeMillis()
    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    private val categories = listOf("Food", "Travel", "Rent", "Shopping", "Entertainment", "Health", "Salary", "Gift", "Other")

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAddTransactionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViewModel()
        setupUI()
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
        viewModel = ViewModelProvider(this, factory)[AddTransactionViewModel::class.java]
    }

    private fun setupUI() {
        binding.toolbar.setNavigationOnClickListener { findNavController().popBackStack() }

        // Category Selection
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, categories)
        binding.categoryDropdown.setAdapter(adapter)

        // Date Picker
        binding.datePickerBtn.text = "Date: ${dateFormat.format(Date(selectedDate))}"
        binding.datePickerBtn.setOnClickListener {
            showDatePicker()
        }

        // Save Button
        binding.saveBtn.setOnClickListener {
            saveTransaction()
        }
    }

    private fun showDatePicker() {
        val picker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select Date")
            .setSelection(selectedDate)
            .build()

        picker.addOnPositiveButtonClickListener { selection ->
            selectedDate = selection
            binding.datePickerBtn.text = "Date: ${dateFormat.format(Date(selectedDate))}"
        }
        picker.show(parentFragmentManager, "DATE_PICKER")
    }

    private fun saveTransaction() {
        val amountStr = binding.amountEditText.text.toString().replace("₹", "").trim()
        val title = binding.titleEditText.text.toString().trim()
        val category = binding.categoryDropdown.text.toString().trim()
        val note = binding.noteEditText.text.toString().trim()
        val type = if (binding.typeToggleGroup.checkedButtonId == R.id.btn_income) "INCOME" else "EXPENSE"

        if (amountStr.isEmpty() || title.isEmpty() || category.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill all required fields", Toast.LENGTH_SHORT).show()
            return
        }

        val amount = amountStr.toDoubleOrNull() ?: 0.0
        
        viewModel.addTransaction(title, amount, type, category, selectedDate, note)
        
        Toast.makeText(requireContext(), "Transaction Saved", Toast.LENGTH_SHORT).show()
        findNavController().popBackStack()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
