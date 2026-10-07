package com.vidyanova.ai.ui.screens.tutor

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val text: String,
    val isFromUser: Boolean,
    val timestamp: String = "Just now",
    val styleTag: String? = null
)
