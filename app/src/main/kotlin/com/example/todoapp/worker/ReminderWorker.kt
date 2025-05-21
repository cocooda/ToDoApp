package com.example.todoapp.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.todoapp.notifications.NotificationHelper

class ReminderWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        // Show notification for task reminder
        NotificationHelper.showNotification(
            applicationContext,
            notificationId = 1, // you can generate or pass different IDs for different reminders
            title = "Task Reminder",
            content = "Reminder: Your task is due!"
        )
        return Result.success()
    }
}
