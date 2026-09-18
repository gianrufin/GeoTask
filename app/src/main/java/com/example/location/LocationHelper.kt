package com.example.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import com.example.data.model.ReminderEntity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.util.Calendar
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

data class GeoLocationData(
    val latitude: Double,
    val longitude: Double,
    val accuracyMeters: Float = 0f,
    val provider: String = "GPS",
    val timestamp: Long = System.currentTimeMillis()
)

data class LocationPreset(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val category: String
)

object LocationPresets {
    val PRESETS = listOf(
        LocationPreset("Office HQ", 37.7749, -122.4194, "Work"),
        LocationPreset("Local Supermarket", 37.7833, -122.4167, "Groceries"),
        LocationPreset("Central Library", 37.7790, -122.4150, "Errands"),
        LocationPreset("Downtown Fitness Gym", 37.7712, -122.4101, "Health"),
        LocationPreset("Home Residence", 37.7650, -122.4200, "Home"),
        LocationPreset("Neighborhood Pharmacy", 37.7770, -122.4220, "Health"),
        LocationPreset("Civic Center Station", 37.7795, -122.4137, "Transit")
    )
}

class LocationHelper(private val context: Context) {
    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    private val fusedClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): GeoLocationData? = suspendCancellableCoroutine { continuation ->
        try {
            fusedClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null)
                .addOnSuccessListener { loc: Location? ->
                    if (loc != null) {
                        continuation.resume(
                            GeoLocationData(
                                latitude = loc.latitude,
                                longitude = loc.longitude,
                                accuracyMeters = loc.accuracy,
                                provider = loc.provider ?: "Fused"
                            )
                        )
                    } else {
                        // Fallback to LocationManager last known
                        continuation.resume(getLastKnownLocation())
                    }
                }
                .addOnFailureListener {
                    continuation.resume(getLastKnownLocation())
                }
        } catch (_: SecurityException) {
            continuation.resume(getLastKnownLocation())
        } catch (_: Exception) {
            continuation.resume(getLastKnownLocation())
        }
    }

    @SuppressLint("MissingPermission")
    fun getLastKnownLocation(): GeoLocationData? {
        try {
            val providers = listOf(
                LocationManager.GPS_PROVIDER,
                LocationManager.NETWORK_PROVIDER,
                LocationManager.PASSIVE_PROVIDER
            )
            for (provider in providers) {
                if (locationManager.isProviderEnabled(provider)) {
                    val loc = locationManager.getLastKnownLocation(provider)
                    if (loc != null) {
                        return GeoLocationData(
                            latitude = loc.latitude,
                            longitude = loc.longitude,
                            accuracyMeters = loc.accuracy,
                            provider = provider
                        )
                    }
                }
            }
        } catch (_: SecurityException) {}
        // Fallback offline default anchor if device has never polled location
        return GeoLocationData(latitude = 37.7749, longitude = -122.4194, provider = "Default Offline")
    }

    companion object {
        fun calculateDistanceMeters(
            lat1: Double,
            lon1: Double,
            lat2: Double,
            lon2: Double
        ): Float {
            val results = FloatArray(1)
            Location.distanceBetween(lat1, lon1, lat2, lon2, results)
            return results[0]
        }

        fun isTodayAllowed(weekdaysOnly: Boolean, mask: Int): Boolean {
            val calendar = Calendar.getInstance()
            val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
            // Calendar: SUNDAY = 1, MONDAY = 2, ..., SATURDAY = 7
            val isWeekend = (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY)

            if (weekdaysOnly && isWeekend) {
                return false
            }

            // Convert Calendar dayOfWeek to 0-indexed Mon..Sun
            // Mon=0, Tue=1, Wed=2, Thu=3, Fri=4, Sat=5, Sun=6
            val dayIndex = when (dayOfWeek) {
                Calendar.MONDAY -> 0
                Calendar.TUESDAY -> 1
                Calendar.WEDNESDAY -> 2
                Calendar.THURSDAY -> 3
                Calendar.FRIDAY -> 4
                Calendar.SATURDAY -> 5
                Calendar.SUNDAY -> 6
                else -> 0
            }

            return ReminderEntity.isDaySelected(mask, dayIndex)
        }

        fun evaluateRemindersAtLocation(
            currentLat: Double,
            currentLon: Double,
            reminders: List<ReminderEntity>,
            minCooldownMs: Long = 10 * 60 * 1000L // 10 minutes debounce between alert sounds
        ): List<Pair<ReminderEntity, Float>> {
            val now = System.currentTimeMillis()
            val triggered = mutableListOf<Pair<ReminderEntity, Float>>()

            for (reminder in reminders) {
                if (!reminder.isActive || reminder.isCompleted) continue

                // Check day of week rule (e.g. weekdays only)
                if (!isTodayAllowed(reminder.weekdaysOnly, reminder.selectedDaysMask)) {
                    continue
                }

                // Check cooldown to avoid redundant alerts
                if (reminder.lastTriggeredTimestamp != null &&
                    (now - reminder.lastTriggeredTimestamp) < minCooldownMs
                ) {
                    continue
                }

                val distance = calculateDistanceMeters(
                    currentLat,
                    currentLon,
                    reminder.latitude,
                    reminder.longitude
                )

                if (distance <= reminder.radiusMeters) {
                    triggered.add(reminder to distance)
                }
            }

            return triggered
        }
    }
}
