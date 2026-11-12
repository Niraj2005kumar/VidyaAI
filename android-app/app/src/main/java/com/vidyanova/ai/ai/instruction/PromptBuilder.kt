package com.vidyanova.ai.ai.instruction

object PromptBuilder {

    fun build(
        question: String,
        classLevel: Int,
        subject: String,
        chapter: String? = null,
        topic: String? = null,
        curriculumContext: String = "",
        detectedStyle: StyleDetectionResult = StyleDetector.detect(question)
    ): String {
        val style = detectedStyle

        val styleInstruction = when (style.mode) {
            TeachingMode.SIMPLE -> "simple concise summary"
            TeachingMode.DETAILED -> "detailed conceptual explanation"
            TeachingMode.STEP_BY_STEP -> "step by step explanation"
            TeachingMode.EXAMPLE_BASED -> "explanation with real life examples"
            TeachingMode.EXAM_READY -> "exam ready answer with key points"
            TeachingMode.BASIC -> "basic foundational explanation"
        }

        val languageInstruction = when (style.language) {
            "hindi" -> "Hindi"
            "hinglish" -> "Hinglish"
            else -> "English"
        }

        val trimmedContext = if (curriculumContext.length > 200) {
            curriculumContext.subSequence(0, 200).toString()
        } else {
            curriculumContext
        }

        val promptText = "System: You are ViyaAI, an offline AI tutor for Class $classLevel $subject. Explain in $languageInstruction ($styleInstruction).\n" +
            (if (trimmedContext.isNotBlank()) "Context: $trimmedContext\n" else "") +
            "User: ${question.trim()}\n" +
            "ViyaAI:"

        return promptText
    }
}
