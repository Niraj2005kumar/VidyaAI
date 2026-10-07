package com.vidyanova.ai.ai.instruction

import com.vidyanova.ai.ai.safety.SafetyRules

object PromptBuilder {

    fun build(
        question: String,
        classLevel: Int,
        subject: String,
        chapter: String? = null,
        topic: String? = null,
        curriculumContext: String = ""
    ): String {
        val style = StyleDetector.detect(question)

        val styleInstruction = when (style.mode) {
            TeachingMode.SIMPLE ->
                "Explain simply and keep the answer concise."

            TeachingMode.DETAILED ->
                "Give a detailed explanation with important concepts."

            TeachingMode.STEP_BY_STEP ->
                "Explain the solution step by step in a clear sequence."

            TeachingMode.EXAMPLE_BASED ->
                "Explain the concept using a simple real-life or curriculum-related example."

            TeachingMode.EXAM_READY ->
                "Give an exam-ready answer with important points and appropriate terminology."

            TeachingMode.BASIC ->
                "Start from the absolute basics and explain as if the student is learning the topic for the first time."
        }

        val languageInstruction = when (style.language) {
            "hindi" ->
                "Answer in simple Hindi."

            "hinglish" ->
                "Answer in natural, easy-to-understand Hinglish."

            else ->
                "Answer in clear and simple English."
        }

        return buildString {
            appendLine(SafetyRules.buildSystemPrompt())
            appendLine()
            appendLine("Student Class: $classLevel")
            appendLine("Subject: $subject")

            if (!chapter.isNullOrBlank()) {
                appendLine("Chapter: $chapter")
            }

            if (!topic.isNullOrBlank()) {
                appendLine("Topic: $topic")
            }

            if (curriculumContext.isNotBlank()) {
                appendLine()
                appendLine(curriculumContext)
            }

            appendLine()
            appendLine("Requested Teaching Style:")
            appendLine(styleInstruction)

            appendLine()
            appendLine("Language:")
            appendLine(languageInstruction)

            appendLine()
            appendLine("Student Question:")
            appendLine(question.trim())

            appendLine()
            appendLine("Instructions:")
            appendLine("- Stay within the selected curriculum.")
            appendLine("- Use the provided curriculum context when relevant.")
            appendLine("- Do not invent facts.")
            appendLine("- Use age-appropriate explanations.")
            appendLine("- Follow the requested teaching style.")
            appendLine("- Use steps when the question requires a solution.")
            appendLine("- Give the final answer clearly after the explanation.")
            appendLine("- Do not answer unrelated or unsafe requests.")

            appendLine()
            appendLine("ViyaAI Answer:")
        }
    }
}