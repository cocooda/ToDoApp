package com.example.todoapp.data.model

import androidx.room.*

@Entity(tableName = "task_table")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val priority: Int = 0,
    val isCompleted: Boolean = false,
    val dueDateMillis: Long? = null //  store due date as epoch millis
)

