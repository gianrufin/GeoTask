package com.example

import android.app.Application
import com.example.data.local.AppDatabase
import com.example.data.repository.ReminderRepository
import com.example.data.repository.SettingsRepository
import com.example.location.LocationHelper
import com.example.notification.NotificationHelper

class GeoReminderApplication : Application() {
    val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }
    val reminderRepository: ReminderRepository by lazy {
        ReminderRepository(database.reminderDao(), database.quickLogDao())
    }
    val settingsRepository: SettingsRepository by lazy { SettingsRepository(this) }
    val notificationHelper: NotificationHelper by lazy { NotificationHelper(this) }
    val locationHelper: LocationHelper by lazy { LocationHelper(this) }

    override fun onCreate() {
        super.onCreate()
        // Initialize notification channels early
        notificationHelper
    }
}
