package com.vidyanova.ai.data.repository

import android.content.Context
import android.util.Log
import com.vidyanova.ai.ai.inference.LocalModelEngine
import com.vidyanova.ai.ai.inference.TokenCallback
import com.vidyanova.ai.ai.instruction.PromptBuilder
import com.vidyanova.ai.ai.instruction.StyleDetector
import com.vidyanova.ai.ai.instruction.TeachingMode
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

    // TEMPORARILY DISABLE FALLBACK RESPONSES FOR TESTING LOCAL QWEN GGUF MODEL
    private val enableFallbackForTesting = false

    fun askTutorStream(
        question: String,
        classLevel: Int,
        subject: String,
        chapter: String? = null,
        topic: String? = null,
        preferredMode: TeachingMode? = null,
        preferredLanguage: String? = null,
        onToken: ((String) -> Unit)? = null
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

        val startTime = System.currentTimeMillis()

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

            val tokenCallback = if (onToken != null) {
                TokenCallback { tokenPiece -> onToken(tokenPiece) }
            } else null

            val response = modelEngine.generateStream(prompt, tokenCallback)
            val durationMs = System.currentTimeMillis() - startTime

            Log.i(TAG, "LOCAL_MODEL_SUCCESS | Source: LOCAL_QWEN_GGUF | Duration: ${durationMs}ms")

            TutorResult(
                success = true,
                answer = response.text,
                mode = style.mode.name,
                language = style.language
            )
        } catch (throwable: Throwable) {
            val durationMs = System.currentTimeMillis() - startTime
            Log.e(TAG, "LOCAL_MODEL_FAILED | Source: LOCAL_QWEN_GGUF | Duration: ${durationMs}ms | Cause: ${throwable.message}", throwable)

            if (enableFallbackForTesting) {
                val fallbackAnswer = getEducationalFallback(question, classLevel, subject, preferredLanguage)
                TutorResult(
                    success = true,
                    answer = fallbackAnswer,
                    mode = style.mode.name,
                    language = style.language
                )
            } else {
                val errorDetails = throwable.message ?: "Native llama.cpp local inference failure"
                TutorResult(
                    success = false,
                    answer = "LOCAL_MODEL_INFERENCE_FAILED: $errorDetails",
                    mode = style.mode.name,
                    language = style.language
                )
            }
        }
    }

    fun askTutor(
        question: String,
        classLevel: Int,
        subject: String,
        chapter: String? = null,
        topic: String? = null,
        preferredMode: TeachingMode? = null,
        preferredLanguage: String? = null
    ): TutorResult {
        return askTutorStream(
            question, classLevel, subject, chapter, topic, preferredMode, preferredLanguage, null
        )
    }

    private fun getEducationalFallback(
        question: String,
        classLevel: Int,
        subject: String,
        language: String?
    ): String {
        Log.w(TAG, "FALLBACK_RESPONSE_USED | Question: $question | Class: $classLevel | Subject: $subject")
        val qLower = question.lowercase(java.util.Locale.ROOT)

        return when {
            qLower.contains("photosynthesis") -> {
                "🌱 **Photosynthesis (Prakash Sanstheshana)**\n\n" +
                "Photosynthesis is the process by which green plants make their own food using sunlight, water, and carbon dioxide.\n\n" +
                "**Chemical Equation:**\n" +
                "`6CO₂ + 6H₂O + Sunlight ➔ C₆H₁₂O₆ (Glucose) + 6O₂`\n\n" +
                "**Key Steps:**\n" +
                "1. **Chlorophyll Absorption:** Leaves absorb sunlight through chlorophyll.\n" +
                "2. **Water & CO₂ Take-up:** Roots absorb water from soil; stomata take CO₂ from air.\n" +
                "3. **Glucose & Oxygen Formation:** Energy converts reactants into glucose and releases Oxygen gas."
            }
            qLower.contains("gravity") || qLower.contains("gravitation") -> {
                "🍎 **Gravitation (Gurutvakarshan)**\n\n" +
                "Gravity is the universal force of attraction between any two objects with mass.\n\n" +
                "**Newton's Universal Law of Gravitation:**\n" +
                "`F = G * (m₁ * m₂) / r²`\n\n" +
                "• On Earth, gravitational acceleration is `g = 9.8 m/s²`."
            }
            else -> {
                "📚 **ViyaAI Explanation for Class $classLevel ($subject)**\n\n" +
                "Aapka question: \"$question\"\n\n" +
                "**Concept Breakdown:**\n" +
                "1. **Core Idea:** Is topic me primary concepts class $classLevel syllabus ke according key principles standard textbooks (NCERT) par based hain.\n" +
                "2. **Summary:** Question $subject me important exam concept hai. Always remember key formulas and definitions for board preparation!"
            }
        }
    }

    fun releaseModel() {
        try {
            modelEngine.unloadModel()
        } catch (t: Throwable) {
            Log.e(TAG, "Error releasing model", t)
        }
    }

    private companion object {
        const val TAG = "TutorRepository"
    }
}
