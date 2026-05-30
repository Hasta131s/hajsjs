package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "spam_config")
data class SpamConfig(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val message: String,
    val intervalMs: Long = 1000L,
    val repeatCount: Int = 10, // 0 means infinite flood
    val mode: String = "AUTO_CLICK", // "AUTO_CLICK" (Touch gestures simulation) or "TEXT_INJECTION" (Accessibility focus typing)
    val useTargetA: Boolean = false,
    val targetAX: Int = 0,
    val targetAY: Int = 0,
    val useTargetB: Boolean = false,
    val targetBX: Int = 0,
    val targetBY: Int = 0
)
