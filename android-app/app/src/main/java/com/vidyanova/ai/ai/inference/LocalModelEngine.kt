package com.vidyanova.ai.ai.inference

import android.content.Context
import android.util.Log
import com.vidyanova.ai.ai.model.ModelConfig
import com.vidyanova.ai.ai.model.ModelManager

data class ModelResponse(
    val text: String,
    val tokensPerSecond: Double = 0.0,
    val generationTimeMs: Long = 0L
)

fun interface TokenCallback {
    fun onToken(tokenPiece: String)
}

class LocalModelEngine(context: Context) {

    private val modelManager = ModelManager(context.applicationContext)
    private var modelLoaded = false
    private var modelPath: String? = null

    fun loadModel(): Boolean = synchronized(nativeLock) {
        if (modelLoaded) return@synchronized true

        loadNativeLibrary()
        val modelFile = modelManager.prepareModel()
        Log.i(TAG, "Loading local model from: ${modelFile.absolutePath} (${modelFile.length()} bytes)")

        val startTime = System.currentTimeMillis()
        val success = nativeLoadModel(
            modelFile.absolutePath,
            ModelConfig.CONTEXT_SIZE,
            ModelConfig.BATCH_SIZE,
            ModelConfig.THREAD_COUNT
        )

        if (!success) {
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

    fun isModelLoaded(): Boolean = synchronized(nativeLock) { modelLoaded }

    fun getModelPath(): String? = synchronized(nativeLock) { modelPath }

    fun generateStream(prompt: String, callback: TokenCallback? = null): ModelResponse = synchronized(nativeLock) {
        require(prompt.isNotBlank()) { "Prompt cannot be empty." }

        if (!modelLoaded && !loadModel()) {
            throw IllegalStateException(
                "The local AI model could not be loaded from ${modelManager.getModelFile().absolutePath}."
            )
        }

        val startTime = System.currentTimeMillis()
        val response = nativeGenerateStream(
            prompt,
            ModelConfig.MAX_NEW_TOKENS,
            ModelConfig.TEMPERATURE,
            callback
        )?.trim().orEmpty()
        val generationTimeMs = System.currentTimeMillis() - startTime

        if (response.isEmpty()) {
            throw IllegalStateException("The local AI model returned an empty response.")
        }

        Log.i(TAG, "Local model generation finished in ${generationTimeMs}ms")
        ModelResponse(text = response, generationTimeMs = generationTimeMs)
    }

    fun generate(prompt: String): ModelResponse {
        return generateStream(prompt, null)
    }

    fun unloadModel() = synchronized(nativeLock) {
        if (modelLoaded) {
            nativeReleaseModel()
        }
        modelLoaded = false
        modelPath = null
    }

    private external fun nativeLoadModel(
        modelPath: String,
        nCtx: Int,
        nBatch: Int,
        nThreads: Int
    ): Boolean

    private external fun nativeGenerateStream(
        prompt: String,
        maxTokens: Int,
        temperature: Float,
        callback: TokenCallback?
    ): String?

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
