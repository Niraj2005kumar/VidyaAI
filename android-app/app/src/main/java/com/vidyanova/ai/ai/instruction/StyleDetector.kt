package com.vidyanova.ai.ai.instruction

enum class TeachingMode {
    SIMPLE,
    DETAILED,
    STEP_BY_STEP,
    EXAMPLE_BASED,
    EXAM_READY,
    BASIC
}

data class StyleDetectionResult(
    val mode: TeachingMode,
    val language: String
)

object StyleDetector {

    fun detect(question: String): StyleDetectionResult {
        val text = question.trim().lowercase()

        val language = detectLanguage(text)
        val mode = detectStyle(text)

        return StyleDetectionResult(
            mode = mode,
            language = language
        )
    }

    private fun detectLanguage(text: String): String {
        val hinglishWords = listOf(
            "bhai",
            "samjha",
            "samjhao",
            "batao",
            "kaise",
            "kyun",
            "kitna",
            "hota hai",
            "mein",
            "me",
            "basic se"
        )

        val hindiWords = listOf(
            "क्या",
            "कैसे",
            "क्यों",
            "समझाओ",
            "बताओ",
            "है",
            "में",
            "कितना"
        )

        return when {
            hindiWords.any { text.contains(it) } -> "hindi"
            hinglishWords.any { text.contains(it) } -> "hinglish"
            else -> "english"
        }
    }

    private fun detectStyle(text: String): TeachingMode {
        return when {
            text.contains("step by step") ||
            text.contains("step-by-step") ||
            text.contains("steps mein") ||
            text.contains("steps me") ||
            text.contains("ek ek step") ->
                TeachingMode.STEP_BY_STEP

            text.contains("example") ||
            text.contains("example ke saath") ||
            text.contains("udaharan") ->
                TeachingMode.EXAMPLE_BASED

            text.contains("exam ready") ||
            text.contains("exam-ready") ||
            text.contains("exam ke liye") ||
            text.contains("exam mein") ->
                TeachingMode.EXAM_READY

            text.contains("detail mein") ||
            text.contains("detailed") ||
            text.contains("detail se") ||
            text.contains("deeply") ->
                TeachingMode.DETAILED

            text.contains("basic se") ||
            text.contains("bilkul basic") ||
            text.contains("beginner") ->
                TeachingMode.BASIC

            text.contains("2 line") ||
            text.contains("2 lines") ||
            text.contains("short mein") ||
            text.contains("short me") ||
            text.contains("simple") ->
                TeachingMode.SIMPLE

            else ->
                TeachingMode.SIMPLE
        }
    }
}