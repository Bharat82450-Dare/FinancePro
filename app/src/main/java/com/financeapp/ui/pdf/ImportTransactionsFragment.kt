package com.financeapp.ui.pdf

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.financeapp.data.repository.RepositoryProvider
import com.financeapp.databinding.FragmentImportTransactionsBinding
import com.financeapp.viewmodel.ViewModelFactory
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ImportTransactionsFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentImportTransactionsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ImportTransactionsViewModel by viewModels {
        val repo = RepositoryProvider.get(requireContext())
        ViewModelFactory(repo)
    }

    private lateinit var adapter: ExtractedTransactionAdapter

    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { viewModel.processPdf(requireContext(), it) }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentImportTransactionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = ExtractedTransactionAdapter { index -> viewModel.toggleSelection(index) }
        binding.previewRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.previewRecyclerView.adapter = adapter

        binding.selectFileButton.setOnClickListener {
            filePickerLauncher.launch(arrayOf("application/pdf"))
        }

        binding.importButton.setOnClickListener {
            val state = viewModel.importState.value
            if (state is ImportTransactionsViewModel.ImportState.Preview) {
                viewModel.saveSelectedTransactions(state.transactions)
            }
        }

        observeViewModel()
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.importState.collectLatest { state ->
                when (state) {
                    is ImportTransactionsViewModel.ImportState.Idle -> {
                        binding.loadingIndicator.visibility = View.GONE
                        binding.statusText.visibility = View.GONE
                        binding.previewRecyclerView.visibility = View.GONE
                        binding.previewHeader.visibility = View.GONE
                        binding.importButton.visibility = View.GONE
                    }
                    is ImportTransactionsViewModel.ImportState.Loading -> {
                        binding.loadingIndicator.visibility = View.VISIBLE
                        binding.statusText.visibility = View.VISIBLE
                        binding.statusText.text = "Scanning PDF..."
                        binding.previewRecyclerView.visibility = View.GONE
                        binding.previewHeader.visibility = View.GONE
                        binding.importButton.visibility = View.GONE
                    }
                    is ImportTransactionsViewModel.ImportState.Preview -> {
                        binding.loadingIndicator.visibility = View.GONE
                        binding.statusText.visibility = View.GONE
                        binding.previewHeader.visibility = View.VISIBLE
                        binding.previewHeader.text = "${state.transactions.size} transactions found. Select which to import:"
                        binding.previewRecyclerView.visibility = View.VISIBLE
                        binding.importButton.visibility = View.VISIBLE
                        updatePreview(state.transactions)
                    }
                    is ImportTransactionsViewModel.ImportState.Error -> {
                        binding.loadingIndicator.visibility = View.GONE
                        binding.statusText.visibility = View.VISIBLE
                        binding.statusText.text = state.message
                        binding.previewRecyclerView.visibility = View.GONE
                        binding.previewHeader.visibility = View.GONE
                        binding.importButton.visibility = View.GONE
                    }
                    is ImportTransactionsViewModel.ImportState.Success -> {
                        Toast.makeText(requireContext(), "Transactions imported successfully!", Toast.LENGTH_SHORT).show()
                        dismiss()
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.selectedIndices.collectLatest { selected ->
                val state = viewModel.importState.value
                if (state is ImportTransactionsViewModel.ImportState.Preview) {
                    adapter.update(state.transactions, selected)
                    binding.importButton.text = "Import ${selected.size} transactions"
                }
            }
        }
    }

    private fun updatePreview(transactions: List<com.financeapp.pdf.PdfStatementScanner.ExtractedTransaction>) {
        adapter.update(transactions, viewModel.selectedIndices.value)
        binding.importButton.text = "Import ${viewModel.selectedIndices.value.size} transactions"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
