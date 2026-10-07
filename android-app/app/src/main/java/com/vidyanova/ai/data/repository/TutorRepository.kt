package com.vidyanova.ai.data.repository

import android.content.Context
import android.util.Log
import com.vidyanova.ai.ai.inference.LocalModelEngine
import com.vidyanova.ai.ai.instruction.PromptBuilder
import com.vidyanova.ai.ai.instruction.StyleDetector
import com.vidyanova.ai.ai.instruction.TeachingMode
import com.vidyanova.ai.ai.retrieval.CurriculumRetriever
import com.vidyanova.ai.ai.retrieval.VectorStore
import com.vidyanova.ai.ai.safety.CurriculumGuard
import java.io.IOException

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
        topic: String? = null,
        preferredMode: TeachingMode? = null,
        preferredLanguage: String? = null
    ): TutorResult {

        if (question.isBlank()) {
            return TutorResult(
                success = false,
                answer = "Please apna question likho.",
                mode = "SIMPLE",
                language = preferredLanguage ?: "english"
            )
        }

        val style = StyleDetector.detect(
            question = question,
            preferredMode = preferredMode,
            preferredLanguage = preferredLanguage
        )
        return try {
            val curriculumContext = curriculumRetriever.buildContext(
                question = question,
                classLevel = classLevel,
                subject = subject
            )

            val guardResult = CurriculumGuard.check(question)
            if (!guardResult.allowed) {
                return TutorResult(
                    success = false,
                    answer = guardResult.message,
                    mode = style.mode.name,
                    language = style.language,
                    blocked = true
                )
            }

            val prompt = PromptBuilder.build(
                question = question,
                classLevel = classLevel,
                subject = subject,
                chapter = chapter,
                topic = topic,
                curriculumContext = curriculumContext,
                detectedStyle = style
            )

            TutorResult(
                success = true,
                answer = modelEngine.generate(prompt).text,
                mode = style.mode.name,
                language = style.language
            )
        } catch (error: Exception) {
            Log.e(TAG, "Local tutor inference failed", error)
            TutorResult(
                success = false,
                answer = when (error) {
                    is IOException -> "ViyaAI's local model is not installed yet. Please add the Qwen GGUF model and try again."
                    is UnsatisfiedLinkError -> "ViyaAI's on-device AI engine is unavailable in this app build."
                    else -> "ViyaAI couldn't generate an answer right now. Please try again."
                },
                mode = style.mode.name,
                language = style.language
            )
        } catch (error: UnsatisfiedLinkError) {
            Log.e(TAG, "Native llama.cpp library is unavailable", error)
            TutorResult(
                success = false,
                answer = "ViyaAI's on-device AI engine is unavailable in this app build.",
                mode = style.mode.name,
                language = style.language
            )
        }
    }

    fun releaseModel() {
        modelEngine.unloadModel()
    }

    private companion object {
        const val TAG = "TutorRepository"
    }
}