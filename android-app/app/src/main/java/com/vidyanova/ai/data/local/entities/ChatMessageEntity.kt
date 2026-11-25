package com.vidyanova.ai.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey
    val clientId: String = UUID.randomUUID().toString(),
    val sessionId: String = "",
    val role: String = "user", // "user" or "assistant"
    val text: String,
    val mode: String = "STEP_BY_STEP",
    val language: String = "hinglish",
    val timestamp: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val synced: Boolean = false
)
