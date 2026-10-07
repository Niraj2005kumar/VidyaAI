package com.vidyanova.ai.ai.inference

import android.content.Context
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
        if (!modelFile.isFile || modelFile.length() == 0L) {
            modelManager.deleteModel()
            throw IOException(
                "The bundled ${ModelConfig.MODEL_DISPLAY_NAME} model is missing or empty."
            )
        }

        if (!nativeLoadModel(modelFile.absolutePath)) {
            nativeReleaseModel()
            modelPath = null
            modelLoaded = false
            return@synchronized false
        }

        modelPath = modelFile.absolutePath
        modelLoaded = true
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
                "The local AI model could not be loaded. Check that the Qwen GGUF model is installed."
            )
        }

        val response = nativeGenerate(
            prompt,
            ModelConfig.MAX_NEW_TOKENS,
            ModelConfig.TEMPERATURE
        )?.trim().orEmpty()

        if (response.isEmpty()) {
            throw IllegalStateException("The local AI model returned an empty response.")
        }

        ModelResponse(text = response)
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
