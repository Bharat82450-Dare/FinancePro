package com.financeapp.ui.pdf

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.financeapp.databinding.ItemExtractedTransactionBinding
import com.financeapp.pdf.PdfStatementScanner
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ExtractedTransactionAdapter(
    private val onToggle: (Int) -> Unit
) : RecyclerView.Adapter<ExtractedTransactionAdapter.ViewHolder>() {

    private val dateFormat = SimpleDateFormat("dd/MM/yy", Locale.getDefault())
    private var items: List<PdfStatementScanner.ExtractedTransaction> = emptyList()
    private var selectedIndices: Set<Int> = emptySet()

    inner class ViewHolder(private val binding: ItemExtractedTransactionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: PdfStatementScanner.ExtractedTransaction, index: Int, selected: Boolean) {
            binding.dateText.text = dateFormat.format(Date(item.date))
            binding.descriptionText.text = item.description
            val sign = if (item.type == "INCOME") "+" else "-"
            binding.amountText.text = "$sign₹${"%.0f".format(item.amount)}"
            binding.amountText.setTextColor(
                if (item.type == "INCOME") Color.parseColor("#2FAC66")
                else Color.parseColor("#EB4335")
            )
            binding.selectCheckbox.isChecked = selected
            binding.selectCheckbox.setOnClickListener { onToggle(index) }
            binding.root.setOnClickListener { onToggle(index) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemExtractedTransactionBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position], position, position in selectedIndices)
    }

    override fun getItemCount() = items.size

    fun update(newItems: List<PdfStatementScanner.ExtractedTransaction>, newSelected: Set<Int>) {
        items = newItems
        selectedIndices = newSelected
        notifyDataSetChanged()
    }
}
