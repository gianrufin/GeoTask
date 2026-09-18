package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.QuickLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QuickLogDao {
    @Query("SELECT * FROM quick_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<QuickLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: QuickLogEntity): Long

    @Query("DELETE FROM quick_logs WHERE id = :id")
    suspend fun deleteLogById(id: Long)

    @Query("DELETE FROM quick_logs")
    suspend fun clearAllLogs()
}
