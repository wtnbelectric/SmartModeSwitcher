package com.example.smartmodeswitcher.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "rules")
data class Rule(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val enabled: Boolean = true,
    val startTime: String,
    val endTime: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val radius: Int? = null,
    val mode: Int
)