package com.financeapp.ui.goals

import android.content.res.ColorStateList
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.financeapp.budget.GoalStatus
import com.financeapp.databinding.ItemGoalBinding

class GoalAdapter(
    private val onAllocate: (GoalStatus) -> Unit,
    private val onDelete: (GoalStatus) -> Unit
) : ListAdapter<GoalStatus, GoalAdapter.GoalViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GoalViewHolder {
        val binding = ItemGoalBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return GoalViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GoalViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class GoalViewHolder(private val b: ItemGoalBinding) : RecyclerView.ViewHolder(b.root) {

        fun bind(status: GoalStatus) {
            val goal = status.goal

            b.goalName.text = goal.name
            b.goalProgressBar.progress = status.progressPercent.toInt()
            b.goalProgressBar.progressTintList = ColorStateList.valueOf(goal.color)
            b.goalPercent.text = "${status.progressPercent.toInt()}%"
            b.goalSavedAmount.text =
                "₹${"%.0f".format(goal.savedAmount)} / ₹${"%.0f".format(goal.targetAmount)}"

            // Color dot
            val dot = b.goalColorDot.background
            if (dot is GradientDrawable) {
                dot.setColor(goal.color)
            } else {
                b.goalColorDot.backgroundTintList = ColorStateList.valueOf(goal.color)
            }

            // Days remaining
            b.goalDaysLeft.text = when {
                status.daysRemaining <= 0 -> "Overdue"
                status.daysRemaining == 1 -> "1 day left"
                else -> "${status.daysRemaining} days left"
            }

            // Daily savings needed
            if (status.dailySavingsNeeded > 0 && status.daysRemaining > 0) {
                b.goalDailyNeeded.text =
                    "Need ₹${"%.0f".format(status.dailySavingsNeeded)}/day to stay on track"
            } else if (status.progressPercent >= 100f) {
                b.goalDailyNeeded.text = "Goal achieved!"
            } else {
                b.goalDailyNeeded.text = "Overdue — allocate funds to complete"
            }

            b.btnAllocate.setOnClickListener { onAllocate(status) }
            b.btnDeleteGoal.setOnClickListener { onDelete(status) }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<GoalStatus>() {
            override fun areItemsTheSame(a: GoalStatus, b: GoalStatus) = a.goal.id == b.goal.id
            override fun areContentsTheSame(a: GoalStatus, b: GoalStatus) = a == b
        }
    }
}
