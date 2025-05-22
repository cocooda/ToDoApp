package com.example.todoapp.data.db

import androidx.room.*
import com.example.todoapp.data.model.Task
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Insert
    suspend fun insert(task: Task)

    @Update
    suspend fun update(task: Task)

    @Delete
    suspend fun delete(task: Task)

    @Query("SELECT * FROM task_table ORDER BY id ASC")
    fun getAllTasks(): Flow<List<Task>> // Changed to Flow

    @Query("SELECT * FROM task_table WHERE id = :id")
    fun getTaskById(id: Int): Flow<Task>

    @Query("SELECT * FROM task_table WHERE priority = :priority ORDER BY dueDate ASC")
    fun getTasksByPriority(priority: Int): Flow<List<Task>>


}

