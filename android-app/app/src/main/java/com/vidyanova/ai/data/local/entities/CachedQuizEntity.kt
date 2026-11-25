package com.vidyanova.ai.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_quizzes")
data class CachedQuizEntity(
    @PrimaryKey
    val quizId: String,
    val title: String,
    val subject: String,
    val classLevel: Int = 10,
    val chapter: String = "General",
    val questionsJson: String = "[]",
    val updatedAt: Long = System.currentTimeMillis()
)
