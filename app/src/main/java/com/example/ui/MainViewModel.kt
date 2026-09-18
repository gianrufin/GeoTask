package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.GeoReminderApplication
import com.example.data.model.QuickLogEntity
import com.example.data.model.ReminderEntity
import com.example.data.repository.NotificationSoundStyle
import com.example.data.repository.UserSettings
import com.example.location.GeoLocationData
import com.example.location.LocationHelper
import com.example.service.LocationTrackingService
import com.example.ui.theme.AppColorPalette
import com.example.ui.theme.DarkModePreference
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class ReminderFilter(val label: String) {
    ALL("All"),
    WEEKDAYS("Weekdays Only"),
    ACTIVE("Active"),
    FAVORITES("Quick Log Favs")
}

data class MainUiState(
    val reminders: List<ReminderEntity> = emptyList(),
    val quickLogs: List<QuickLogEntity> = emptyList(),
    val settings: UserSettings = UserSettings(),
    val currentLocation: GeoLocationData? = null,
    val isLocating: Boolean = false,
    val selectedFilter: ReminderFilter = ReminderFilter.ALL,
    val userNotice: String? = null
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as GeoReminderApplication
    private val reminderRepo = app.reminderRepository
    private val settingsRepo = app.settingsRepository
    private val locationHelper = app.locationHelper
    private val notificationHelper = app.notificationHelper

    private val _selectedFilter = MutableStateFlow(ReminderFilter.ALL)
    private val _currentLocation = MutableStateFlow<GeoLocationData?>(null)
    private val _isLocating = MutableStateFlow(false)
    private val _userNotice = MutableStateFlow<String?>(null)

    private val remindersFlow = combine(
        reminderRepo.allReminders,
        _selectedFilter
    ) { reminders, filter ->
        val filtered = when (filter) {
            ReminderFilter.ALL -> reminders
            ReminderFilter.WEEKDAYS -> reminders.filter { it.weekdaysOnly || it.selectedDaysMask == ReminderEntity.MASK_WEEKDAYS }
            ReminderFilter.ACTIVE -> reminders.filter { it.isActive }
            ReminderFilter.FAVORITES -> reminders.filter { it.isQuickActionFavorite }
        }
        filtered to filter
    }

    private val locationFlow = combine(
        _currentLocation,
        _isLocating
    ) { location, isLocating ->
        location to isLocating
    }

    val uiState: StateFlow<MainUiState> = combine(
        remindersFlow,
        reminderRepo.allQuickLogs,
        settingsRepo.settings,
        locationFlow,
        _userNotice
    ) { (filteredReminders, filter), logs, settings, (location, isLocating), notice ->
        MainUiState(
            reminders = filteredReminders,
            quickLogs = logs,
            settings = settings,
            currentLocation = location,
            isLocating = isLocating,
            selectedFilter = filter,
            userNotice = notice
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MainUiState()
    )

    init {
        fetchLocation()
    }

    fun setFilter(filter: ReminderFilter) {
        _selectedFilter.value = filter
    }

    fun clearNotice() {
        _userNotice.value = null
    }

    fun fetchLocation() {
        viewModelScope.launch {
            _isLocating.value = true
            try {
                val loc = locationHelper.getCurrentLocation()
                _currentLocation.value = loc
            } catch (_: Exception) {
                _currentLocation.value = locationHelper.getLastKnownLocation()
            } finally {
                _isLocating.value = false
            }
        }
    }

    fun saveReminder(reminder: ReminderEntity) {
        viewModelScope.launch {
            if (reminder.id == 0L) {
                reminderRepo.insertReminder(reminder)
                _userNotice.value = "Saved reminder: ${reminder.title}"
            } else {
                reminderRepo.updateReminder(reminder)
                _userNotice.value = "Updated reminder: ${reminder.title}"
            }
        }
    }

    fun toggleReminderActive(reminder: ReminderEntity) {
        viewModelScope.launch {
            reminderRepo.toggleReminderActive(reminder.id, reminder.isActive)
            val state = if (!reminder.isActive) "enabled" else "paused"
            _userNotice.value = "Reminder '${reminder.title}' $state"
        }
    }

    fun toggleFavorite(reminder: ReminderEntity) {
        viewModelScope.launch {
            reminderRepo.toggleFavorite(reminder.id, reminder.isQuickActionFavorite)
            val state = if (!reminder.isQuickActionFavorite) "added to" else "removed from"
            _userNotice.value = "'${reminder.title}' $state Quick-Actions"
        }
    }

    fun deleteReminder(reminder: ReminderEntity) {
        viewModelScope.launch {
            reminderRepo.deleteReminder(reminder)
            _userNotice.value = "Deleted reminder '${reminder.title}'"
        }
    }

    /**
     * One-tap quick logging for repetitive tasks without internet connectivity.
     */
    fun logQuickAction(
        title: String,
        locationLabel: String,
        latitude: Double? = _currentLocation.value?.latitude,
        longitude: Double? = _currentLocation.value?.longitude,
        reminderId: Long? = null,
        note: String = ""
    ) {
        viewModelScope.launch {
            val log = QuickLogEntity(
                reminderId = reminderId,
                title = title,
                locationLabel = locationLabel,
                latitude = latitude,
                longitude = longitude,
                timestamp = System.currentTimeMillis(),
                note = note
            )
            reminderRepo.insertQuickLog(log)
            if (reminderId != null) {
                reminderRepo.updateLastTriggered(reminderId, System.currentTimeMillis())
            }
            _userNotice.value = "✓ Quick-logged: $title (Offline)"
            // Give subtle haptic confirmation
            notificationHelper.playSubtleSoundAndVibration(
                uiState.value.settings.soundStyle,
                uiState.value.settings.soundVolume
            )
        }
    }

    fun deleteQuickLog(id: Long) {
        viewModelScope.launch {
            reminderRepo.deleteQuickLog(id)
            _userNotice.value = "Log entry removed"
        }
    }

    fun clearAllLogs() {
        viewModelScope.launch {
            reminderRepo.clearQuickLogs()
            _userNotice.value = "All logs cleared"
        }
    }

    /**
     * Triggers simulation/evaluation of reminders at specified or current location.
     */
    fun evaluateLocationTriggers(lat: Double, lon: Double) {
        viewModelScope.launch {
            val reminders = uiState.value.reminders
            val triggered = LocationHelper.evaluateRemindersAtLocation(
                lat,
                lon,
                reminders,
                minCooldownMs = 0L // Force trigger during explicit test
            )

            if (triggered.isEmpty()) {
                _userNotice.value = "No reminder zones triggered at this location."
            } else {
                val settings = uiState.value.settings
                for ((rem, distance) in triggered) {
                    notificationHelper.showReminderNotification(
                        reminder = rem,
                        distanceMeters = distance,
                        soundStyle = settings.soundStyle,
                        volume = settings.soundVolume
                    )
                    reminderRepo.updateLastTriggered(rem.id, System.currentTimeMillis())
                }
                _userNotice.value = "Triggered ${triggered.size} geo-reminder alert(s)!"
            }
        }
    }

    fun updateDarkMode(mode: DarkModePreference) {
        settingsRepo.updateDarkMode(mode)
    }

    fun updateColorPalette(palette: AppColorPalette) {
        settingsRepo.updateColorPalette(palette)
    }

    fun updateSoundStyle(style: NotificationSoundStyle) {
        settingsRepo.updateSoundStyle(style)
        notificationHelper.playSubtleSoundAndVibration(style, uiState.value.settings.soundVolume)
    }

    fun updateSoundVolume(volume: Float) {
        settingsRepo.updateSoundVolume(volume)
    }

    fun updateBatterySaverInterval(minutes: Int) {
        settingsRepo.updateBatterySaverInterval(minutes)
        if (uiState.value.settings.isBackgroundMonitoringEnabled) {
            // Restart service with updated interval
            LocationTrackingService.stopService(getApplication())
            LocationTrackingService.startService(getApplication())
        }
    }

    fun toggleBackgroundMonitoring(enabled: Boolean) {
        settingsRepo.updateBackgroundMonitoring(enabled)
        if (enabled) {
            LocationTrackingService.startService(getApplication())
            _userNotice.value = "Background location monitor started"
        } else {
            LocationTrackingService.stopService(getApplication())
            _userNotice.value = "Background location monitor stopped"
        }
    }

    fun previewSound(style: NotificationSoundStyle) {
        notificationHelper.playSubtleSoundAndVibration(style, uiState.value.settings.soundVolume)
    }

    fun sendTestNotification() {
        val settings = uiState.value.settings
        val sampleReminder = ReminderEntity(
            id = 9999,
            title = "Test Location Reminder",
            description = "Subtle sound & local push notification test",
            locationLabel = "Current Coordinates",
            latitude = _currentLocation.value?.latitude ?: 37.7749,
            longitude = _currentLocation.value?.longitude ?: -122.4194,
            radiusMeters = 200,
            weekdaysOnly = false
        )
        notificationHelper.showReminderNotification(
            reminder = sampleReminder,
            distanceMeters = 85f,
            soundStyle = settings.soundStyle,
            volume = settings.soundVolume
        )
        _userNotice.value = "Push notification delivered with ${settings.soundStyle.displayName}"
    }
}
