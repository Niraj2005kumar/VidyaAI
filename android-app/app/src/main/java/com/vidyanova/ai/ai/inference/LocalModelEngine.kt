
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

class LocalModelEngine(context: Context) {

    private val modelManager =
        ModelManager(context.applicationContext)

    fun loadModel(): Boolean = synchronized(nativeLock) {
        if (nativeModelLoaded) {
            return@synchronized true
        }

        try {
            loadNativeLibrary()

            val modelFile = modelManager.prepareModel()

            require(modelFile.exists() && modelFile.isFile) {
                "Model file does not exist: ${modelFile.absolutePath}"
            }

            Log.i(
                TAG,
                "Loading model: ${modelFile.absolutePath}, size=${modelFile.length()}"
            )

            val startTime = System.currentTimeMillis()

            val loaded = nativeLoadModel(modelFile.absolutePath)

            if (!loaded) {
                Log.e(TAG, "Native model loading failed")
                nativeReleaseModel()
                nativeModelLoaded = false
                nativeModelPath = null
                return@synchronized false
            }

            nativeModelPath = modelFile.absolutePath
            nativeModelLoaded = true

            Log.i(
                TAG,
                "Model loaded successfully in ${System.currentTimeMillis() - startTime}ms"
            )

            true

        } catch (e: Exception) {
            Log.e(TAG, "Model loading error", e)
            nativeModelLoaded = false
            nativeModelPath = null
            false
        }
    }

    fun isModelLoaded(): Boolean =
        synchronized(nativeLock) {
            nativeModelLoaded
        }

    fun getModelPath(): String? =
        synchronized(nativeLock) {
            nativeModelPath
        }

    fun generate(prompt: String): ModelResponse =
        synchronized(nativeLock) {

            require(prompt.isNotBlank()) {
                "Prompt cannot be empty."
            }

            if (!nativeModelLoaded && !loadModel()) {
                throw IllegalStateException(
                    "Local AI model could not be loaded."
                )
            }

            val startTime = System.currentTimeMillis()

            val response = nativeGenerate(
                prompt,
                ModelConfig.MAX_NEW_TOKENS,
                ModelConfig.TEMPERATURE
            )?.trim().orEmpty()

            val generationTimeMs =
                System.currentTimeMillis() - startTime

            if (response.isEmpty()) {
                throw IllegalStateException(
                    "The local AI model returned an empty response."
                )
            }

            Log.i(
                TAG,
                "Generation finished in ${generationTimeMs}ms"
            )

            ModelResponse(
                text = response,
                generationTimeMs = generationTimeMs
            )
        }

    fun unloadModel() {
        synchronized(nativeLock) {
            if (nativeModelLoaded) {
                Log.i(TAG, "Releasing shared native model")
                nativeReleaseModel()
            }

            nativeModelLoaded = false
            nativeModelPath = null
        }
    }

    private external fun nativeLoadModel(
        modelPath: String
    ): Boolean

    private external fun nativeGenerate(
        prompt: String,
        maxTokens: Int,
        temperature: Float
    ): String?

    private external fun nativeReleaseModel()

    companion object {
        private const val TAG = "LocalModelEngine"

        private val nativeLock = Any()

        private var nativeModelLoaded = false
        private var nativeModelPath: String? = null

        @Volatile
        private var nativeLibraryLoaded = false

        private fun loadNativeLibrary() {
            if (!nativeLibraryLoaded) {
                System.loadLibrary("vidyanova_native")
                nativeLibraryLoaded = true

                Log.i(TAG, "Native library loaded successfully")
            }
        }
    }
}
