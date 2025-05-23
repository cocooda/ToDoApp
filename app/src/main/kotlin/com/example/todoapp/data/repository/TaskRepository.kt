package com.example.todoapp.data.repository

import com.example.todoapp.data.db.TaskDao
import com.example.todoapp.data.model.Task
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskRepository @Inject constructor(
    private val taskDao: TaskDao
) {

    suspend fun insert(task: Task) {
        taskDao.insert(task)
    }

    suspend fun update(task: Task) {
        taskDao.update(task)
    }

    suspend fun delete(task: Task) {
        taskDao.delete(task)
    }

    fun getAllTasks(): Flow<List<Task>> {
        return taskDao.getAllTasks()
    }

    fun getTaskById(id: Int): Flow<Task> = taskDao.getTaskById(id)


    fun getTasksByPriority(priority: Int): Flow<List<Task>> {
        return taskDao.getTasksByPriority(priority)
    }

}
