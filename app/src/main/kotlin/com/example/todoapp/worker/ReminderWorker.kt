package com.example.todoapp.worker

import android.content.Context
import android.widget.Toast
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ReminderWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.Main) {
        Toast.makeText(applicationContext, "Reminder: Task is due!", Toast.LENGTH_LONG).show()
        Result.success()
    }
}
