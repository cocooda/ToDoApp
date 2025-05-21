package com.example.todoapp.utils

import android.content.Context
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.todoapp.worker.ReminderWorker
import java.util.concurrent.TimeUnit

object ReminderScheduler {
    fun scheduleReminder(context: Context, delayMinutes: Long) {
        val request = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(delayMinutes, TimeUnit.MINUTES)
            .build()

        WorkManager.getInstance(context).enqueue(request)
    }
}
