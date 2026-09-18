package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reminders")
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val locationLabel: String,
    val latitude: Double,
    val longitude: Double,
    val radiusMeters: Int = 200,
    val triggerCondition: String = "ENTER", // "ENTER", "EXIT", "NEARBY"
    val weekdaysOnly: Boolean = false,
    val selectedDaysMask: Int = 127, // Bitmask: 1=Mon, 2=Tue, 4=Wed, 8=Thu, 16=Fri, 32=Sat, 64=Sun. Weekdays=31, All=127
    val isActive: Boolean = true,
    val isCompleted: Boolean = false,
    val lastTriggeredTimestamp: Long? = null,
    val createdTimestamp: Long = System.currentTimeMillis(),
    val isQuickActionFavorite: Boolean = false
) {
    companion object {
        const val MASK_WEEKDAYS = 31 // Mon to Fri
        const val MASK_ALL = 127 // All 7 days
        const val MASK_WEEKENDS = 96 // Sat + Sun

        val DAY_NAMES = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        val DAY_FULL_NAMES = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")

        fun isDaySelected(mask: Int, dayIndex: Int): Boolean {
            return (mask and (1 shl dayIndex)) != 0
        }

        fun toggleDay(mask: Int, dayIndex: Int): Int {
            return mask xor (1 shl dayIndex)
        }

        fun getScheduleSummary(weekdaysOnly: Boolean, mask: Int): String {
            if (weekdaysOnly || mask == MASK_WEEKDAYS) {
                return "Weekdays only (Mon - Fri)"
            }
            if (mask == MASK_ALL) {
                return "Every day"
            }
            if (mask == MASK_WEEKENDS) {
                return "Weekends only (Sat - Sun)"
            }
            val activeDays = mutableListOf<String>()
            for (i in 0 until 7) {
                if (isDaySelected(mask, i)) {
                    activeDays.add(DAY_NAMES[i])
                }
            }
            return if (activeDays.isEmpty()) "No days active" else activeDays.joinToString(", ")
        }
    }
}
