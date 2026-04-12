package com.financeapp.ui.report

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.financeapp.data.model.CategorySpending
import com.financeapp.databinding.ItemCategoryBreakdownBinding

class CategoryBreakdownAdapter :
    ListAdapter<CategorySpending, CategoryBreakdownAdapter.ViewHolder>(DiffCallback()) {

    private val colors = listOf(
        "#F44336", "#E91E63", "#9C27B0", "#3F51B5",
        "#2196F3", "#009688", "#4CAF50", "#FF9800",
        "#795548", "#607D8B"
    )

    inner class ViewHolder(private val binding: ItemCategoryBreakdownBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CategorySpending, total: Double, index: Int) {
            binding.categoryName.text = item.category
            binding.amountText.text = "₹${"%.0f".format(item.total)}"
            val percent = if (total > 0) (item.total / total * 100).toInt() else 0
            binding.percentageText.text = "$percent%"
            val color = Color.parseColor(colors[index % colors.size])
            binding.colorIndicator.backgroundTintList =
                android.content.res.ColorStateList.valueOf(color)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCategoryBreakdownBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val total = currentList.sumOf { it.total }
        holder.bind(getItem(position), total, position)
    }

    class DiffCallback : DiffUtil.ItemCallback<CategorySpending>() {
        override fun areItemsTheSame(a: CategorySpending, b: CategorySpending) =
            a.category == b.category
        override fun areContentsTheSame(a: CategorySpending, b: CategorySpending) = a == b
    }
}
