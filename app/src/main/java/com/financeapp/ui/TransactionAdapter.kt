package com.financeapp.ui

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.financeapp.data.entities.Transaction
import com.financeapp.databinding.ItemTransactionBinding
import java.text.SimpleDateFormat
import java.util.Locale

class TransactionAdapter(private val onItemClick: (Transaction) -> Unit) :
    ListAdapter<Transaction, TransactionAdapter.TransactionViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val binding = ItemTransactionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TransactionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val transaction = getItem(position)
        holder.bind(transaction, onItemClick)
    }

    class TransactionViewHolder(private val binding: ItemTransactionBinding) :
        RecyclerView.ViewHolder(binding.root) {
        
        private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

        fun bind(transaction: Transaction, onClick: (Transaction) -> Unit) {
            binding.titleText.text = transaction.title
            binding.categoryText.text = transaction.category
            binding.dateText.text = dateFormat.format(transaction.date)
            
            val amountFormatted = String.format(Locale.getDefault(), "₹%.2f", transaction.amount)
            binding.amountText.text = if (transaction.type == "INCOME") "+$amountFormatted" else "-$amountFormatted"
            binding.amountText.setTextColor(if (transaction.type == "INCOME") Color.parseColor("#4CAF50") else Color.parseColor("#F44336"))
            
            binding.statusIcon.setImageResource(if (transaction.isSynced) com.financeapp.R.drawable.ic_check_circle else com.financeapp.R.drawable.ic_sync_pending)
            
            binding.root.setOnClickListener { onClick(transaction) }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Transaction>() {
        override fun areItemsTheSame(oldItem: Transaction, newItem: Transaction): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Transaction, newItem: Transaction): Boolean = oldItem == newItem
    }
}
