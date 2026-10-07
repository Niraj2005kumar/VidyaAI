package com.vidyanova.ai.ai.safety

data class GuardResult(
    val allowed: Boolean,
    val message: String
)

object CurriculumGuard {

    private val unsafePatterns = listOf(
        "hack someone's account",
        "hack an account",
        "hack bank",
        "steal password",
        "make a weapon",
        "make a bomb",
        "make explosives",
        "bypass password",
        "steal money",
        "credit card fraud"
    )

    private val offTopicPatterns = listOf(
        "celebrity gossip",
        "random joke",
        "movie gossip",
        "political gossip",
        "betting tips",
        "gambling"
    )

    fun check(question: String): GuardResult {
        val normalized = question
            .trim()
            .lowercase()

        if (normalized.isBlank()) {
            return GuardResult(
                allowed = false,
                message = "Please ask an educational question."
            )
        }

        if (unsafePatterns.any { normalized.contains(it) }) {
            return GuardResult(
                allowed = false,
                message = "I can help with school learning, but I can't help with that request."
            )
        }

        if (offTopicPatterns.any { normalized.contains(it) }) {
            return GuardResult(
                allowed = false,
                message = "I'm ViyaAI, your curriculum-focused tutor. Please ask a school-related question."
            )
        }

        return GuardResult(
            allowed = true,
            message = ""
        )
    }
}