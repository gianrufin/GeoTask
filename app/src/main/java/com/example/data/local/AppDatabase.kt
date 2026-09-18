package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.QuickLogEntity
import com.example.data.model.ReminderEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [ReminderEntity::class, QuickLogEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun reminderDao(): ReminderDao
    abstract fun quickLogDao(): QuickLogDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope = CoroutineScope(Dispatchers.IO)): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "geo_reminder_database"
                )
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch {
                        populateInitialData(database.reminderDao(), database.quickLogDao())
                    }
                }
            }

            private suspend fun populateInitialData(
                reminderDao: ReminderDao,
                quickLogDao: QuickLogDao
            ) {
                // Pre-populate with realistic, helpful reminders
                reminderDao.insertReminder(
                    ReminderEntity(
                        title = "Morning Standup & Timecard",
                        description = "Review team board and log daily hours",
                        locationLabel = "Office HQ",
                        latitude = 37.7749,
                        longitude = -122.4194,
                        radiusMeters = 200,
                        triggerCondition = "ENTER",
                        weekdaysOnly = true,
                        selectedDaysMask = ReminderEntity.MASK_WEEKDAYS, // Mon-Fri
                        isActive = true,
                        isQuickActionFavorite = true
                    )
                )

                reminderDao.insertReminder(
                    ReminderEntity(
                        title = "Grab Fresh Groceries",
                        description = "Oat milk, spinach, fruit, eggs",
                        locationLabel = "Local Supermarket",
                        latitude = 37.7833,
                        longitude = -122.4167,
                        radiusMeters = 300,
                        triggerCondition = "NEARBY",
                        weekdaysOnly = false,
                        selectedDaysMask = ReminderEntity.MASK_ALL,
                        isActive = true,
                        isQuickActionFavorite = true
                    )
                )

                reminderDao.insertReminder(
                    ReminderEntity(
                        title = "Return Library Books",
                        description = "Drop off books at the reception bin",
                        locationLabel = "City Central Library",
                        latitude = 37.7790,
                        longitude = -122.4150,
                        radiusMeters = 150,
                        triggerCondition = "ENTER",
                        weekdaysOnly = true,
                        selectedDaysMask = ReminderEntity.MASK_WEEKDAYS,
                        isActive = true,
                        isQuickActionFavorite = false
                    )
                )

                quickLogDao.insertLog(
                    QuickLogEntity(
                        title = "Logged arrival at Office HQ",
                        locationLabel = "Office HQ",
                        latitude = 37.7749,
                        longitude = -122.4194,
                        timestamp = System.currentTimeMillis() - 7200000,
                        note = "Arrived on time for 9:30 standup"
                    )
                )
            }
        }
    }
}
