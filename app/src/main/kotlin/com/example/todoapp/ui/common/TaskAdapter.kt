package com.example.todoapp.ui.common

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.todoapp.R
import com.example.todoapp.data.model.Task
import java.text.SimpleDateFormat
import java.util.*

class TaskAdapter : ListAdapter<Task, TaskAdapter.TaskViewHolder>(DiffCallback()) {

    class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        val tvDueDate: TextView = itemView.findViewById(R.id.tvDueDate)
        val tvPriority: TextView = itemView.findViewById(R.id.tvPriority)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = getItem(position)
        val context = holder.itemView.context
        holder.tvTitle.text = task.title

        if (task.dueDate != null) {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            holder.tvDueDate.text = context.getString(R.string.due_label, sdf.format(Date(task.dueDate)))
        } else {
            holder.tvDueDate.text = context.getString(R.string.due_no_date)
        }

        holder.tvPriority.text = when (task.priority) {
            0 -> "Priority: Low"
            1 -> "Priority: Medium"
            2 -> "Priority: High"
            else -> "Priority: Unknown"
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Task>() {
        override fun areItemsTheSame(oldItem: Task, newItem: Task): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Task, newItem: Task): Boolean = oldItem == newItem
    }
}
