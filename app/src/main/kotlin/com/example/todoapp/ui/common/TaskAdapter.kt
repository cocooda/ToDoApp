package com.example.todoapp.ui.common

import com.example.todoapp.data.model.Task
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.todoapp.R
import java.text.SimpleDateFormat
import java.util.*

class TaskAdapter(private val taskList: List<Task>) :
    RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

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
        val task = taskList[position]

        holder.tvTitle.text = task.title

        // Format the due date (if it exists)
        if (task.dueDate != null) {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val dateString = sdf.format(Date(task.dueDate))
            holder.tvDueDate.text = holder.itemView.context.getString(R.string.due_label, dateString)
        } else {
            holder.tvDueDate.text = holder.itemView.context.getString(R.string.due_no_date)
        }

        // Show priority as text
        val priorityText = when (task.priority) {
            0 -> "Priority: Low"
            1 -> "Priority: Medium"
            2 -> "Priority: High"
            else -> "Priority: Unknown"
        }
        holder.tvPriority.text = priorityText
    }

    override fun getItemCount(): Int = taskList.size
}
