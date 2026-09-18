package com.example.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.local.AppDatabase
import com.example.data.repository.SettingsRepository
import com.example.location.LocationHelper
import com.example.notification.NotificationHelper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class LocationTrackingService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + Job())
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var notificationHelper: NotificationHelper
    private lateinit var settingsRepository: SettingsRepository
    private var locationCallback: LocationCallback? = null

    companion object {
        const val NOTIFICATION_ID = 9991
        const val ACTION_START = "ACTION_START_TRACKING"
        const val ACTION_STOP = "ACTION_STOP_TRACKING"

        fun startService(context: Context) {
            val intent = Intent(context, LocationTrackingService::class.java).apply {
                action = ACTION_START
            }
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (_: Exception) {}
        }

        fun stopService(context: Context) {
            val intent = Intent(context, LocationTrackingService::class.java).apply {
                action = ACTION_STOP
            }
            context.stopService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        notificationHelper = NotificationHelper(this)
        settingsRepository = SettingsRepository(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return START_NOT_STICKY
        }

        val foregroundNotification = buildForegroundNotification("Passive battery-saver location active")
        startForeground(NOTIFICATION_ID, foregroundNotification)

        startBatteryEfficientLocationTracking()
        return START_STICKY
    }

    private fun buildForegroundNotification(status: String): Notification {
        val openIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, NotificationHelper.CHANNEL_ID_TRACKING)
            .setSmallIcon(R.drawable.app_logo_1789769739960)
            .setContentTitle("GeoReminder Active")
            .setContentText(status)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    @SuppressLint("MissingPermission")
    private fun startBatteryEfficientLocationTracking() {
        val intervalMinutes = settingsRepository.settings.value.batterySaverIntervalMinutes
        val intervalMillis = (intervalMinutes * 60 * 1000L).coerceAtLeast(60_000L)

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_BALANCED_POWER_ACCURACY,
            intervalMillis
        )
            .setMinUpdateIntervalMillis(intervalMillis / 2)
            .setMinUpdateDistanceMeters(50f) // Only update after moving 50 meters to conserve battery
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val loc = result.lastLocation ?: return
                onLocationChanged(loc)
            }
        }

        try {
            locationCallback?.let {
                fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    it,
                    Looper.getMainLooper()
                )
            }
        } catch (_: SecurityException) {
        } catch (_: Exception) {}
    }

    private fun onLocationChanged(loc: Location) {
        serviceScope.launch {
            try {
                val db = AppDatabase.getDatabase(applicationContext)
                val activeReminders = db.reminderDao().getActiveReminders().first()
                val triggered = LocationHelper.evaluateRemindersAtLocation(
                    loc.latitude,
                    loc.longitude,
                    activeReminders
                )

                val settings = settingsRepository.settings.value

                for ((reminder, distance) in triggered) {
                    notificationHelper.showReminderNotification(
                        reminder = reminder,
                        distanceMeters = distance,
                        soundStyle = settings.soundStyle,
                        volume = settings.soundVolume
                    )
                    db.reminderDao().updateLastTriggered(reminder.id, System.currentTimeMillis())
                }
            } catch (_: Exception) {}
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        locationCallback?.let { fusedLocationClient.removeLocationUpdates(it) }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
