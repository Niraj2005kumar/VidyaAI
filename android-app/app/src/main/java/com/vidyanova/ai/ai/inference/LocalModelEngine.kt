package com.vidyanova.ai.ai.inference

import android.content.Context
import java.io.File

data class ModelResponse(
    val text: String,
    val tokensPerSecond: Double = 0.0,
    val generationTimeMs: Long = 0L
)

class LocalModelEngine(
    private val context: Context
) {

    private var modelLoaded = false
    private var modelPath: String? = null

    fun loadModel(): Boolean {
        return try {
            val modelFile = File(
                context.filesDir,
                "model/tutor-model.gguf"
            )

            if (!modelFile.exists()) {
                modelLoaded = false
                modelPath = null
                return false
            }

            modelPath = modelFile.absolutePath
            modelLoaded = true

            true
        } catch (error: Exception) {
            modelLoaded = false
            modelPath = null
            false
        }
    }

    fun isModelLoaded(): Boolean {
        return modelLoaded
    }

    fun getModelPath(): String? {
        return modelPath
    }

    fun generate(prompt: String): ModelResponse {
        if (!modelLoaded) {
            throw IllegalStateException(
                "ViyaAI model is not loaded"
            )
        }

        if (prompt.isBlank()) {
            throw IllegalArgumentException(
                "Prompt cannot be empty"
            )
        }

        throw UnsupportedOperationException(
            "Native llama.cpp inference is not connected yet"
        )
    }

    fun unloadModel() {
        modelLoaded = false
        modelPath = null
    }
}