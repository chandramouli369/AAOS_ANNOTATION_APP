package com.example.adas_application

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "annotations")
data class Annotation(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val sessionId: String,
    val eventType: String,
    val timestamp: Long = System.currentTimeMillis(),
    val speed: Float = 0f,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val syncStatus: String = "PENDING"
)
