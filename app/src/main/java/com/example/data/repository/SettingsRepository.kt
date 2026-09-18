package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.ui.theme.AppColorPalette
import com.example.ui.theme.DarkModePreference
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class NotificationSoundStyle(val displayName: String, val description: String) {
    SUBTLE_CHIME("Subtle Chime", "High pitched, soft 2-tone chime (minimal disturbance)"),
    GENTLE_BELL("Gentle Bell", "Warm, mellow acoustic chime tone"),
    SOFT_POP("Soft Pop", "Discreet low-amplitude tick/pop"),
    VIBRATE_ONLY("Discreet Vibrate", "Gentle 2-pulse haptic, no audio"),
    SILENT("Silent Alert", "Visual notification banner only")
}

data class UserSettings(
    val darkMode: DarkModePreference = DarkModePreference.SYSTEM,
    val colorPalette: AppColorPalette = AppColorPalette.SLATE_CYAN,
    val soundStyle: NotificationSoundStyle = NotificationSoundStyle.SUBTLE_CHIME,
    val soundVolume: Float = 0.5f,
    val batterySaverIntervalMinutes: Int = 5,
    val isBackgroundMonitoringEnabled: Boolean = false
)

class SettingsRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("geo_reminder_settings", Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<UserSettings> = _settings.asStateFlow()

    private fun loadSettings(): UserSettings {
        val darkModeName = prefs.getString("dark_mode", DarkModePreference.SYSTEM.name) ?: DarkModePreference.SYSTEM.name
        val paletteName = prefs.getString("color_palette", AppColorPalette.SLATE_CYAN.name) ?: AppColorPalette.SLATE_CYAN.name
        val soundName = prefs.getString("sound_style", NotificationSoundStyle.SUBTLE_CHIME.name) ?: NotificationSoundStyle.SUBTLE_CHIME.name
        val volume = prefs.getFloat("sound_volume", 0.5f)
        val interval = prefs.getInt("battery_interval", 5)
        val bgMonitoring = prefs.getBoolean("bg_monitoring", false)

        val darkMode = try { DarkModePreference.valueOf(darkModeName) } catch (_: Exception) { DarkModePreference.SYSTEM }
        val palette = try { AppColorPalette.valueOf(paletteName) } catch (_: Exception) { AppColorPalette.SLATE_CYAN }
        val sound = try { NotificationSoundStyle.valueOf(soundName) } catch (_: Exception) { NotificationSoundStyle.SUBTLE_CHIME }

        return UserSettings(
            darkMode = darkMode,
            colorPalette = palette,
            soundStyle = sound,
            soundVolume = volume,
            batterySaverIntervalMinutes = interval,
            isBackgroundMonitoringEnabled = bgMonitoring
        )
    }

    fun updateDarkMode(mode: DarkModePreference) {
        prefs.edit().putString("dark_mode", mode.name).apply()
        _settings.value = _settings.value.copy(darkMode = mode)
    }

    fun updateColorPalette(palette: AppColorPalette) {
        prefs.edit().putString("color_palette", palette.name).apply()
        _settings.value = _settings.value.copy(colorPalette = palette)
    }

    fun updateSoundStyle(style: NotificationSoundStyle) {
        prefs.edit().putString("sound_style", style.name).apply()
        _settings.value = _settings.value.copy(soundStyle = style)
    }

    fun updateSoundVolume(volume: Float) {
        prefs.edit().putFloat("sound_volume", volume).apply()
        _settings.value = _settings.value.copy(soundVolume = volume)
    }

    fun updateBatterySaverInterval(minutes: Int) {
        prefs.edit().putInt("battery_interval", minutes).apply()
        _settings.value = _settings.value.copy(batterySaverIntervalMinutes = minutes)
    }

    fun updateBackgroundMonitoring(enabled: Boolean) {
        prefs.edit().putBoolean("bg_monitoring", enabled).apply()
        _settings.value = _settings.value.copy(isBackgroundMonitoringEnabled = enabled)
    }
}
