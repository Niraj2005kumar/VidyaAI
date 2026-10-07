package com.vidyanova.ai.ai.model

import android.content.Context
import java.io.File

class ModelManager(
    private val context: Context
) {

    private val modelDirectory: File
        get() = File(context.filesDir, "model")

    private val modelFile: File
        get() = File(
            modelDirectory,
            ModelConfig.MODEL_FILE_NAME
        )

    fun isModelAvailable(): Boolean {
        return modelFile.exists() &&
            modelFile.length() > 0
    }

    fun getModelPath(): String? {
        return if (isModelAvailable()) {
            modelFile.absolutePath
        } else {
            null
        }
    }

    fun prepareModel(): Boolean {
        return try {
            if (isModelAvailable()) {
                return true
            }

            if (!modelDirectory.exists()) {
                modelDirectory.mkdirs()
            }

            context.assets.open(
                ModelConfig.MODEL_ASSET_PATH
            ).use { inputStream ->

                modelFile.outputStream().use { outputStream ->

                    inputStream.copyTo(
                        outputStream,
                        bufferSize = 1024 * 1024
                    )
                }
            }

            isModelAvailable()

        } catch (error: Exception) {
            modelFile.delete()
            false
        }
    }

    fun deleteModel() {
        if (modelFile.exists()) {
            modelFile.delete()
        }
    }

    fun getModelSizeBytes(): Long {
        return if (isModelAvailable()) {
            modelFile.length()
        } else {
            0L
        }
    }
}