package com.example.todoapp.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todoapp.data.model.Task
import com.example.todoapp.utils.ReminderScheduler
import com.example.todoapp.data.repository.TaskRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class TaskViewModel(
    private val repository: TaskRepository
) : ViewModel() {

    val allTasks: StateFlow<List<Task>> = repository
        .getAllTasks()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _currentTask = MutableStateFlow<Task?>(null)
    val currentTask: StateFlow<Task?> = _currentTask.asStateFlow()

    suspend fun getTaskByIdOnce(id: Int): Task? {
        return repository.getTaskByIdOnce(id)
    }


    fun insert(task: Task, context: Context) {
        viewModelScope.launch {
            repository.insert(task)

            task.dueDate?.let { dueMillis ->
                val delayMillis = dueMillis - System.currentTimeMillis()
                if (delayMillis > 0) {
                    ReminderScheduler.scheduleReminder(context, delayMillis)
                }
            }
        }
    }

    fun update(task: Task, context: Context) {
        viewModelScope.launch {
            repository.update(task)
            task.dueDate?.let { dueMillis ->
                val delayMillis = dueMillis - System.currentTimeMillis()
                if (delayMillis > 0) {
                    ReminderScheduler.scheduleReminder(context, delayMillis)
                }
            }
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            repository.delete(task)
        }
    }


}
