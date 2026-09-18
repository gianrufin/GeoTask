package com.example.data.repository

import com.example.data.local.QuickLogDao
import com.example.data.local.ReminderDao
import com.example.data.model.QuickLogEntity
import com.example.data.model.ReminderEntity
import kotlinx.coroutines.flow.Flow

class ReminderRepository(
    private val reminderDao: ReminderDao,
    private val quickLogDao: QuickLogDao
) {
    val allReminders: Flow<List<ReminderEntity>> = reminderDao.getAllReminders()
    val activeReminders: Flow<List<ReminderEntity>> = reminderDao.getActiveReminders()
    val favoriteReminders: Flow<List<ReminderEntity>> = reminderDao.getFavoriteReminders()
    val allQuickLogs: Flow<List<QuickLogEntity>> = quickLogDao.getAllLogs()

    suspend fun insertReminder(reminder: ReminderEntity): Long {
        return reminderDao.insertReminder(reminder)
    }

    suspend fun updateReminder(reminder: ReminderEntity) {
        reminderDao.updateReminder(reminder)
    }

    suspend fun deleteReminder(reminder: ReminderEntity) {
        reminderDao.deleteReminder(reminder)
    }

    suspend fun deleteReminderById(id: Long) {
        reminderDao.deleteReminderById(id)
    }

    suspend fun toggleReminderActive(id: Long, currentActive: Boolean) {
        reminderDao.updateActiveStatus(id, !currentActive)
    }

    suspend fun toggleFavorite(id: Long, currentFavorite: Boolean) {
        reminderDao.updateFavoriteStatus(id, !currentFavorite)
    }

    suspend fun updateLastTriggered(id: Long, timestamp: Long) {
        reminderDao.updateLastTriggered(id, timestamp)
    }

    suspend fun insertQuickLog(log: QuickLogEntity): Long {
        return quickLogDao.insertLog(log)
    }

    suspend fun deleteQuickLog(id: Long) {
        quickLogDao.deleteLogById(id)
    }

    suspend fun clearQuickLogs() {
        quickLogDao.clearAllLogs()
    }
}
