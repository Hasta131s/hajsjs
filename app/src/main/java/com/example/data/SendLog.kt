package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "send_log")
data class SendLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val message: String,
    val configTitle: String,
    val status: String, // "SUCCESS" or "ERROR"
    val errorMessage: String? = null
)
