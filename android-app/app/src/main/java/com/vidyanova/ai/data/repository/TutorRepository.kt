package com.vidyanova.ai.data.repository

import android.content.Context
import com.vidyanova.ai.ai.inference.LocalModelEngine
import com.vidyanova.ai.ai.instruction.PromptBuilder
import com.vidyanova.ai.ai.retrieval.CurriculumRetriever
import com.vidyanova.ai.ai.safety.CurriculumGuard
import com.vidyanova.ai.ai.retrieval.VectorStore

data class TutorResult(
    val success: Boolean,
    val answer: String,
    val message: String = ""
)

class TutorRepository(
    context: Context,
    private val curriculumRetriever: CurriculumRetriever,
    private val modelEngine: LocalModelEngine
) {

    fun ask(
        question: String,
        classLevel: Int,
        subject: String,
        chapter: String? = null,
        topic: String? = null
    ): TutorResult {

        val guardResult = CurriculumGuard.check(question)

        if (!guardResult.allowed) {
            return TutorResult(
                success = false,
                answer = "",
                message = guardResult.message
            )
        }

        return try {
            val curriculumContext =
                curriculumRetriever.buildContext(
                    question = question,
                    classLevel = classLevel,
                    subject = subject
                )

            val prompt = PromptBuilder.build(
                question = question,
                classLevel = classLevel,
                subject = subject,
                chapter = chapter,
                topic = topic,
                curriculumContext = curriculumContext
            )

            val response = modelEngine.generate(prompt)

            TutorResult(
                success = true,
                answer = response.text
            )

        } catch (error: Exception) {
            TutorResult(
                success = false,
                answer = "",
                message = error.message
                    ?: "ViyaAI could not generate an answer."
            )
        }
    }
}