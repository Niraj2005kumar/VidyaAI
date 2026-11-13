
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

        val styleInstruction = when (detectedStyle.mode) {
            TeachingMode.SIMPLE -> "Explain simply and concisely."
            TeachingMode.DETAILED -> "Give a detailed conceptual explanation."
            TeachingMode.STEP_BY_STEP -> "Explain step by step."
            TeachingMode.EXAMPLE_BASED -> "Use relatable real-life examples."
            TeachingMode.EXAM_READY -> "Give an exam-ready answer with key points."
            TeachingMode.BASIC -> "Explain the basic foundational concepts."
        }

        val languageInstruction = when (detectedStyle.language) {
            "hindi" -> "Hindi"
            "hinglish" -> "Hinglish"
            else -> "English"
        }

        // Temporary character limit to control prompt size.
        val trimmedContext = curriculumContext.take(1200)

        val systemPrompt = """
            You are ViyaAI, an offline educational AI tutor.
            Teach students of Class $classLevel.
            Subject: $subject.
            ${chapter?.let { "Chapter: $it." } ?: ""}
            ${topic?.let { "Topic: $it." } ?: ""}
            
            Respond in $languageInstruction.
            $styleInstruction

            Use age-appropriate language.
            Explain concepts accurately.
            Use examples when helpful.
            Do not invent textbook facts.
            If information is insufficient, clearly say so.
        """.trimIndent()

        return buildString {
            append("<|im_start|>system\n")
            append(systemPrompt)
            if (trimmedContext.isNotBlank()) {
                append("\n\nCurriculum context:\n")
                append(trimmedContext)
            }
            append("<|im_end|>\n")

            append("<|im_start|>user\n")
            append(question.trim())
            append("<|im_end|>\n")

            append("<|im_start|>assistant\n")
        }
    }
}
