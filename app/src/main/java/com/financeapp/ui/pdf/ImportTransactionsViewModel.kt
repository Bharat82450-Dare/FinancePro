package com.financeapp.ui.pdf

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.financeapp.data.entities.Transaction
import com.financeapp.data.repository.FinanceRepository
import com.financeapp.pdf.PdfStatementScanner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ImportTransactionsViewModel(
    private val repository: FinanceRepository
) : ViewModel() {

    sealed class ImportState {
        object Idle : ImportState()
        object Loading : ImportState()
        data class Preview(val transactions: List<PdfStatementScanner.ExtractedTransaction>) : ImportState()
        data class Error(val message: String) : ImportState()
        object Success : ImportState()
    }

    private val _importState = MutableStateFlow<ImportState>(ImportState.Idle)
    val importState: StateFlow<ImportState> = _importState.asStateFlow()

    // Tracks which items are selected (by index)
    private val _selectedIndices = MutableStateFlow<Set<Int>>(emptySet())
    val selectedIndices: StateFlow<Set<Int>> = _selectedIndices.asStateFlow()

    fun processPdf(context: Context, uri: Uri) {
        _importState.value = ImportState.Loading
        viewModelScope.launch {
            val extracted = PdfStatementScanner.extractTransactions(context, uri)
            if (extracted.isEmpty()) {
                _importState.value = ImportState.Error(
                    "No transactions found. The PDF format may not be supported."
                )
            } else {
                // Select all by default
                _selectedIndices.value = extracted.indices.toSet()
                _importState.value = ImportState.Preview(extracted)
            }
        }
    }

    fun toggleSelection(index: Int) {
        val current = _selectedIndices.value.toMutableSet()
        if (index in current) current.remove(index) else current.add(index)
        _selectedIndices.value = current
    }

    fun saveSelectedTransactions(transactions: List<PdfStatementScanner.ExtractedTransaction>) {
        val selected = _selectedIndices.value
        val toSave = transactions.filterIndexed { i, _ -> i in selected }
        if (toSave.isEmpty()) {
            _importState.value = ImportState.Error("No transactions selected.")
            return
        }
        viewModelScope.launch {
            toSave.forEach { extracted ->
                repository.addTransaction(
                    Transaction(
                        title = extracted.description.take(50),
                        amount = extracted.amount,
                        type = extracted.type,
                        category = "Imported",
                        date = extracted.date,
                        note = "Imported from PDF"
                    )
                )
            }
            _importState.value = ImportState.Success
        }
    }

    fun reset() {
        _importState.value = ImportState.Idle
        _selectedIndices.value = emptySet()
    }
}
