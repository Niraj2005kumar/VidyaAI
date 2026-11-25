package com.vidyanova.ai.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "topic_progress")
data class TopicProgressEntity(
    @PrimaryKey
    val clientId: String = UUID.randomUUID().toString(),
    val classLevel: Int = 10,
    val subject: String,
    val chapter: String = "General",
    val topic: String,
    val questionsAsked: Int = 0,
    val quizzesAttempted: Int = 0,
    val averageScore: Double = 0.0,
    val studyTimeMinutes: Int = 0,
    val masteryLevel: String = "learning",
    val isWeakArea: Boolean = false,
    val lastStudiedAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val synced: Boolean = false
)
