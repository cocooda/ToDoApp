package com.example.todoapp.repository

import com.example.todoapp.data.db.TaskDao
import com.example.todoapp.data.model.Task
import kotlinx.coroutines.flow.Flow

class TaskRepository(private val taskDao: TaskDao) {

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
        return taskDao.getAllTasks() // returns live data stream
    }

    suspend fun getTaskById(id: Int): Task? {
        return taskDao.getTaskById(id)
    }
}
