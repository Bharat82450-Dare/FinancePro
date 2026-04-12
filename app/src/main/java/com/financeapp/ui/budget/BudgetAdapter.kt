package com.financeapp.ui.budget

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.financeapp.budget.BudgetStatus
import com.financeapp.budget.BudgetWithStatus
import com.financeapp.data.entities.Budget
import com.financeapp.databinding.ItemBudgetBinding

class BudgetAdapter(
    private val onDeleteClick: (Budget) -> Unit
) : ListAdapter<BudgetWithStatus, BudgetAdapter.BudgetViewHolder>(DiffCallback()) {

    inner class BudgetViewHolder(private val binding: ItemBudgetBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: BudgetWithStatus) {
            binding.categoryName.text = item.budget.category
            binding.amountText.text = "₹${"%.0f".format(item.spentAmount)} / ₹${"%.0f".format(item.budget.amountLimit)}"

            val percent = if (item.budget.amountLimit > 0)
                ((item.spentAmount / item.budget.amountLimit) * 100).toInt().coerceAtMost(100)
            else 0

            binding.budgetProgress.progress = percent
            binding.percentageText.text = "$percent%"

            val color = when (item.status) {
                is BudgetStatus.Ok -> Color.parseColor("#4CAF50")
                is BudgetStatus.Warning -> Color.parseColor("#FFC107")
                is BudgetStatus.Exceeded -> Color.parseColor("#F44336")
            }
            binding.budgetProgress.progressTintList = ColorStateList.valueOf(color)
            binding.percentageText.setTextColor(color)

            binding.deleteButton.setOnClickListener { onDeleteClick(item.budget) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BudgetViewHolder {
        val binding = ItemBudgetBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BudgetViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BudgetViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<BudgetWithStatus>() {
        override fun areItemsTheSame(a: BudgetWithStatus, b: BudgetWithStatus) =
            a.budget.id == b.budget.id

        override fun areContentsTheSame(a: BudgetWithStatus, b: BudgetWithStatus) = a == b
    }
}
