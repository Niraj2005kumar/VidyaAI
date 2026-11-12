package com.vidyanova.ai.ai.inference

import android.content.Context
import android.util.Log
import com.vidyanova.ai.ai.model.ModelConfig
import com.vidyanova.ai.ai.model.ModelManager
import java.io.File
import java.io.IOException

data class ModelResponse(
    val text: String,
    val tokensPerSecond: Double = 0.0,
    val generationTimeMs: Long = 0L
)

class LocalModelEngine(context: Context) {

    private val modelManager = ModelManager(context.applicationContext)
    private var modelLoaded = false
    private var modelPath: String? = null

    @Synchronized
    fun loadModel(): Boolean = synchronized(nativeLock) {
        if (modelLoaded) return@synchronized true

        loadNativeLibrary()
        val modelFile = modelManager.prepareModel()
        Log.i(TAG, "Loading local model from: ${modelFile.absolutePath} (${modelFile.length()} bytes)")

        val startTime = System.currentTimeMillis()
        if (!nativeLoadModel(modelFile.absolutePath)) {
            Log.e(TAG, "nativeLoadModel returned false for: ${modelFile.absolutePath}")
            nativeReleaseModel()
            modelPath = null
            modelLoaded = false
            return@synchronized false
        }

        val loadTimeMs = System.currentTimeMillis() - startTime
        modelPath = modelFile.absolutePath
        modelLoaded = true
        Log.i(TAG, "Local model loaded successfully in ${loadTimeMs}ms from ${modelFile.absolutePath}")
        true
    }

    @Synchronized
    fun isModelLoaded(): Boolean = modelLoaded

    @Synchronized
    fun getModelPath(): String? = modelPath

    @Synchronized
    fun generate(prompt: String): ModelResponse = synchronized(nativeLock) {
        require(prompt.isNotBlank()) { "Prompt cannot be empty." }

        if (!modelLoaded && !loadModel()) {
            throw IllegalStateException(
                "The local AI model could not be loaded from ${modelManager.getModelFile().absolutePath}."
            )
        }

        val startTime = System.currentTimeMillis()
        val response = nativeGenerate(
            prompt,
            ModelConfig.MAX_NEW_TOKENS,
            ModelConfig.TEMPERATURE
        )?.trim().orEmpty()
        val generationTimeMs = System.currentTimeMillis() - startTime

        if (response.isEmpty()) {
            throw IllegalStateException("The local AI model returned an empty response.")
        }

        Log.i(TAG, "Local model generation finished in ${generationTimeMs}ms")
        ModelResponse(text = response, generationTimeMs = generationTimeMs)
    }

    @Synchronized
    fun unloadModel() = synchronized(nativeLock) {
        if (modelLoaded) {
            nativeReleaseModel()
        }
        modelLoaded = false
        modelPath = null
    }

    private external fun nativeLoadModel(modelPath: String): Boolean

    private external fun nativeGenerate(
        prompt: String,
        maxTokens: Int,
        temperature: Float
    ): String?

    private external fun nativeReleaseModel()

    companion object {
        private const val TAG = "LocalModelEngine"
        private val nativeLock = Any()
        @Volatile
        private var nativeLibraryLoaded = false

        private fun loadNativeLibrary() {
            if (!nativeLibraryLoaded) {
                System.loadLibrary("vidyanova_native")
                nativeLibraryLoaded = true
            }
        }
    }
}
