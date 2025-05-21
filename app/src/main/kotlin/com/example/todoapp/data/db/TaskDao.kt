package com.example.todoapp.data.db

import androidx.room.*
import com.example.todoapp.data.model.Task

@Dao
interface TaskDao {

    @Insert
    suspend fun insert(task: Task)

    @Update
    suspend fun update(task: Task)

    @Delete
    suspend fun delete(task: Task)

    @Query("SELECT * FROM task_table ORDER BY id ASC")
    fun getAllTasks(): kotlinx.coroutines.flow.Flow<List<Task>> // Changed to Flow

    @Query("SELECT * FROM task_table WHERE id = :taskId")
    suspend fun getTaskById(taskId: Int): Task?
}

