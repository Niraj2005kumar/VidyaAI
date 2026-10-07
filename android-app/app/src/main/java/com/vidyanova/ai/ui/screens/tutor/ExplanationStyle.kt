package com.vidyanova.ai.ui.screens.tutor

enum class ExplanationStyle(
    val displayName: String,
    val description: String,
    val promptInstruction: String
) {
    Simple("Simple", "Easy & short", "2 line mein batao"),
    Detailed("Detailed", "In-depth concept", "Deep detailed explanation"),
    StepByStep("Step-by-Step", "Step-wise guide", "step by step samjhao"),
    Example("Example", "Real-world cases", "example ke saath batao"),
    ExamReady("Exam Ready", "Board exam style", "exam ke liye answer do"),
    Basic("Basic", "Zero to hero", "bhai basic se samjha")
}
