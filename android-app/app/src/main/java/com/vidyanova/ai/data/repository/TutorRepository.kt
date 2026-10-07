package com.vidyanova.ai.data.repository

import android.content.Context
import com.vidyanova.ai.ai.inference.LocalModelEngine
import com.vidyanova.ai.ai.instruction.PromptBuilder
import com.vidyanova.ai.ai.instruction.StyleDetector
import com.vidyanova.ai.ai.retrieval.CurriculumRetriever
import com.vidyanova.ai.ai.retrieval.VectorStore
import com.vidyanova.ai.ai.safety.CurriculumGuard

data class TutorResult(
    val success: Boolean,
    val answer: String,
    val mode: String,
    val language: String,
    val blocked: Boolean = false
)

class TutorRepository(
    context: Context
) {

    private val modelEngine = LocalModelEngine(context)
    private val curriculumRetriever = CurriculumRetriever(VectorStore())

    fun askTutor(
        question: String,
        classLevel: Int,
        subject: String,
        chapter: String? = null,
        topic: String? = null
    ): TutorResult {

        if (question.isBlank()) {
            return TutorResult(
                success = false,
                answer = "Please apna question likho.",
                mode = "SIMPLE",
                language = "english"
            )
        }

        val guardResult = CurriculumGuard.check(question)

        if (!guardResult.allowed) {
            return TutorResult(
                success = false,
                answer = guardResult.message,
                mode = "SIMPLE",
                language = StyleDetector.detect(question).language,
                blocked = true
            )
        }

        val style = StyleDetector.detect(question)

        val curriculumContext = curriculumRetriever.buildContext(
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

        val answer = modelEngine.generate(prompt).text

        return TutorResult(
            success = true,
            answer = answer,
            mode = style.mode.name,
            language = style.language
        )
    }

    fun releaseModel() {
        modelEngine.unloadModel()
    }
}