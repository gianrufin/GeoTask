package com.example.receiver

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.local.AppDatabase
import com.example.data.model.QuickLogEntity
import com.example.notification.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotificationActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent == null) return

        if (intent.action == NotificationHelper.ACTION_MARK_DONE) {
            val reminderId = intent.getLongExtra(NotificationHelper.EXTRA_REMINDER_ID, -1L)
            val title = intent.getStringExtra(NotificationHelper.EXTRA_REMINDER_TITLE) ?: "Completed Reminder"
            val location = intent.getStringExtra(NotificationHelper.EXTRA_LOCATION_LABEL) ?: "Location"

            // Dismiss notification
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (reminderId != -1L) {
                notificationManager.cancel(reminderId.toInt())
            }

            // Save quick log entry into local Room database offline
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = AppDatabase.getDatabase(context)
                    db.quickLogDao().insertLog(
                        QuickLogEntity(
                            reminderId = if (reminderId != -1L) reminderId else null,
                            title = "Completed: $title",
                            locationLabel = location,
                            timestamp = System.currentTimeMillis(),
                            note = "Logged via notification quick action"
                        )
                    )
                    if (reminderId != -1L) {
                        db.reminderDao().updateLastTriggered(reminderId, System.currentTimeMillis())
                    }
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
