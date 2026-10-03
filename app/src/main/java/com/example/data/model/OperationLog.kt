package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Entity(tableName = "operation_logs")
data class OperationLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val timeFormatted: String = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date()),
    val category: String, // "AUTO", "WEED", "DISEASE", "SOIL", "WATER", "SAFETY", "MANUAL"
    val title: String,
    val detail: String,
    val level: String = "INFO" // "INFO", "WARNING", "ALERT", "SUCCESS"
)
