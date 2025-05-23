package com.example.todoapp.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todoapp.data.model.Task
import com.example.todoapp.utils.ReminderScheduler
import com.example.todoapp.repository.TaskRepository
import kotlinx.coroutines.flow.*
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

    private val _currentTask = MutableStateFlow<Task?>(null)
    val currentTask: StateFlow<Task?> = _currentTask.asStateFlow()

    fun getTaskById(id: Int): StateFlow<Task?> {
        return repository.getTaskById(id)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    }


    fun getTasksByPriority(priority: Int): StateFlow<List<Task>> {
        return repository.getTasksByPriority(priority)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
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

}
