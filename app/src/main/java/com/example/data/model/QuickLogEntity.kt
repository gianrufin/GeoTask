package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quick_logs")
data class QuickLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val reminderId: Long? = null,
    val title: String,
    val locationLabel: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val note: String = ""
)
