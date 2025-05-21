package com.example.todoapp.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todoapp.data.model.Task
import com.example.todoapp.utils.ReminderScheduler
import com.example.todoapp.repository.TaskRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TaskViewModel @Inject constructor(
    private val repository: TaskRepository
) : ViewModel() {

    val allTasks: StateFlow<List<Task>> = repository
        .getAllTasks()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun insert(task: Task, context: Context) {
        viewModelScope.launch {
            repository.insert(task)

            // Schedule reminder if dueDate is in the future
            task.dueDateMillis?.let { dueMillis ->
                val delayMillis = dueMillis - System.currentTimeMillis()
                if (delayMillis > 0) {
                    ReminderScheduler.scheduleReminder(context, delayMillis)
                }
            }
        }
    }

    fun update(task: Task) {
        viewModelScope.launch {
            repository.update(task)
        }
    }

    fun delete(task: Task) {
        viewModelScope.launch {
            repository.delete(task)
        }
    }
}
