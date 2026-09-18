package com.example.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.model.ReminderEntity
import com.example.data.repository.NotificationSoundStyle
import com.example.receiver.NotificationActionReceiver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.sin

class NotificationHelper(private val context: Context) {

    companion object {
        const val CHANNEL_ID_GEO = "geo_reminder_alerts"
        const val CHANNEL_ID_TRACKING = "geo_location_service"
        const val CHANNEL_NAME_GEO = "Location Reminders"
        const val CHANNEL_NAME_TRACKING = "Background Location Service"

        const val ACTION_MARK_DONE = "com.example.georeminder.ACTION_MARK_DONE"
        const val ACTION_DISMISS = "com.example.georeminder.ACTION_DISMISS"
        const val EXTRA_REMINDER_ID = "extra_reminder_id"
        const val EXTRA_REMINDER_TITLE = "extra_reminder_title"
        const val EXTRA_LOCATION_LABEL = "extra_location_label"
    }

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val geoChannel = NotificationChannel(
                CHANNEL_ID_GEO,
                CHANNEL_NAME_GEO,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifies when entering or leaving configured geographic reminder zones"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 100, 80, 100)
                setSound(null, null) // Handled by our custom subtle synthesized tone generator
            }

            val trackingChannel = NotificationChannel(
                CHANNEL_ID_TRACKING,
                CHANNEL_NAME_TRACKING,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows status while passive battery-friendly location monitoring is active"
                enableVibration(false)
                setSound(null, null)
            }

            notificationManager.createNotificationChannel(geoChannel)
            notificationManager.createNotificationChannel(trackingChannel)
        }
    }

    fun showReminderNotification(
        reminder: ReminderEntity,
        distanceMeters: Float,
        soundStyle: NotificationSoundStyle,
        volume: Float
    ) {
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            context,
            reminder.id.toInt(),
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val markDoneIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = ACTION_MARK_DONE
            putExtra(EXTRA_REMINDER_ID, reminder.id)
            putExtra(EXTRA_REMINDER_TITLE, reminder.title)
            putExtra(EXTRA_LOCATION_LABEL, reminder.locationLabel)
        }
        val markDonePendingIntent = PendingIntent.getBroadcast(
            context,
            (reminder.id * 10 + 1).toInt(),
            markDoneIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val distanceText = if (distanceMeters < 1000f) {
            "${distanceMeters.toInt()}m away"
        } else {
            String.format("%.1f km away", distanceMeters / 1000f)
        }

        val conditionText = when (reminder.triggerCondition) {
            "ENTER" -> "Arrived at"
            "EXIT" -> "Departed from"
            else -> "Near"
        }

        val contentText = "$conditionText ${reminder.locationLabel} ($distanceText)" +
                if (reminder.description.isNotBlank()) " • ${reminder.description}" else ""

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_GEO)
            .setSmallIcon(R.drawable.app_logo_1789769739960)
            .setContentTitle("📍 Location Alert: ${reminder.title}")
            .setContentText(contentText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(contentText))
            .setContentIntent(openAppPendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .addAction(
                android.R.drawable.ic_menu_agenda,
                "Log One-Tap Done",
                markDonePendingIntent
            )
            .build()

        notificationManager.notify(reminder.id.toInt(), notification)

        // Play subtle sound & haptic according to user settings
        playSubtleSoundAndVibration(soundStyle, volume)
    }

    fun playSubtleSoundAndVibration(style: NotificationSoundStyle, volume: Float) {
        // Haptic feedback
        if (style != NotificationSoundStyle.SILENT) {
            triggerSubtleVibration()
        }

        if (style == NotificationSoundStyle.SILENT || style == NotificationSoundStyle.VIBRATE_ONLY) {
            return
        }

        // Run tone synthesis in IO thread to keep UI super smooth
        CoroutineScope(Dispatchers.IO).launch {
            try {
                when (style) {
                    NotificationSoundStyle.SUBTLE_CHIME -> playSubtleChime(volume)
                    NotificationSoundStyle.GENTLE_BELL -> playGentleBell(volume)
                    NotificationSoundStyle.SOFT_POP -> playSoftPop(volume)
                    else -> {}
                }
            } catch (_: Exception) {
                // Graceful fallback
            }
        }
    }

    private fun triggerSubtleVibration() {
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(
                    VibrationEffect.createWaveform(
                        longArrayOf(0, 50, 60, 50),
                        intArrayOf(0, 100, 0, 120),
                        -1
                    )
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(80)
            }
        } catch (_: Exception) {}
    }

    /**
     * Synthesizes a soft, harmonic two-tone chime (880Hz -> 1318.5Hz, A5 to E6)
     * with an acoustic exponential fade out envelope.
     */
    private fun playSubtleChime(volume: Float) {
        val sampleRate = 22050
        val durationMs = 280
        val totalSamples = (sampleRate * (durationMs / 1000.0)).toInt()
        val buffer = ShortArray(totalSamples)

        val splitPoint = totalSamples / 2
        val gain = (32767 * volume.coerceIn(0.05f, 1.0f) * 0.4f).toInt()

        for (i in 0 until totalSamples) {
            val envelope = (1.0 - (i.toDouble() / totalSamples))
            val freq = if (i < splitPoint) 880.0 else 1318.5
            val angle = 2.0 * Math.PI * freq * i / sampleRate
            val sample = (sin(angle) * envelope * gain).toInt().coerceIn(-32768, 32767)
            buffer[i] = sample.toShort()
        }

        playRawAudio(buffer, sampleRate)
    }

    /**
     * Synthesizes a warm, mellow bell ping (523.25Hz, C5) with warm decay.
     */
    private fun playGentleBell(volume: Float) {
        val sampleRate = 22050
        val durationMs = 380
        val totalSamples = (sampleRate * (durationMs / 1000.0)).toInt()
        val buffer = ShortArray(totalSamples)

        val gain = (32767 * volume.coerceIn(0.05f, 1.0f) * 0.45f).toInt()
        val baseFreq = 523.25
        val harmonicFreq = 1046.5

        for (i in 0 until totalSamples) {
            val progress = i.toDouble() / totalSamples
            val envelope = Math.exp(-progress * 4.0) // exponential decay
            val base = sin(2.0 * Math.PI * baseFreq * i / sampleRate) * 0.8
            val harmonic = sin(2.0 * Math.PI * harmonicFreq * i / sampleRate) * 0.2
            val sample = ((base + harmonic) * envelope * gain).toInt().coerceIn(-32768, 32767)
            buffer[i] = sample.toShort()
        }

        playRawAudio(buffer, sampleRate)
    }

    /**
     * Synthesizes a discreet, ultra-short soft bubble/pop (620Hz, 60ms).
     */
    private fun playSoftPop(volume: Float) {
        val sampleRate = 22050
        val durationMs = 70
        val totalSamples = (sampleRate * (durationMs / 1000.0)).toInt()
        val buffer = ShortArray(totalSamples)

        val gain = (32767 * volume.coerceIn(0.05f, 1.0f) * 0.35f).toInt()

        for (i in 0 until totalSamples) {
            val progress = i.toDouble() / totalSamples
            val envelope = sin(Math.PI * progress) // window envelope
            val freq = 550.0 + (150.0 * (1.0 - progress)) // slight pitch drop
            val angle = 2.0 * Math.PI * freq * i / sampleRate
            val sample = (sin(angle) * envelope * gain).toInt().coerceIn(-32768, 32767)
            buffer[i] = sample.toShort()
        }

        playRawAudio(buffer, sampleRate)
    }

    private fun playRawAudio(buffer: ShortArray, sampleRate: Int) {
        var track: AudioTrack? = null
        try {
            val minBufferSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            val bufferSize = maxOf(minBufferSize, buffer.size * 2)

            track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(bufferSize)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            track.write(buffer, 0, buffer.size)
            track.play()
            Thread.sleep(buffer.size * 1000L / sampleRate + 50)
        } catch (_: Exception) {
        } finally {
            try {
                track?.stop()
                track?.release()
            } catch (_: Exception) {}
        }
    }
}
