package com.vidyanova.ai.ui.screens.quiz

data class QuizQuestion(
    val id: Int = 1,
    val question: String,
    val options: List<String>,
    val correctAnswer: Int,
    val explanation: String,
    val difficulty: String = "Medium"
)
