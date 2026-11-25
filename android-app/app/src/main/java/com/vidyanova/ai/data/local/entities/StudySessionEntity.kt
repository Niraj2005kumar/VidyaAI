package com.vidyanova.ai.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "study_sessions")
data class StudySessionEntity(
    @PrimaryKey
    val clientId: String = UUID.randomUUID().toString(),
    val subject: String,
    val topic: String = "",
    val startTime: Long = System.currentTimeMillis(),
    val endTime: Long? = null,
    val durationMinutes: Int = 0,
    val updatedAt: Long = System.currentTimeMillis(),
    val synced: Boolean = false
)
