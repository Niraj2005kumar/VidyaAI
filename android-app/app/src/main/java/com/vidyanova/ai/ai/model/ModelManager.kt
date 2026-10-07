package com.vidyanova.ai.ai.model

import android.content.Context
import java.io.File

class ModelManager(
    private val context: Context
) {

    fun getModelFile(): File {
        val modelDir = File(
            context.filesDir,
            "model"
        )

        if (!modelDir.exists()) {
            modelDir.mkdirs()
        }

        return File(
            modelDir,
            ModelConfig.MODEL_FILE_NAME
        )
    }

    fun isModelAvailable(): Boolean {
        val modelFile = getModelFile()

        return modelFile.exists() &&
                modelFile.length() > 0
    }

    fun prepareModel(): File {
        val modelFile = getModelFile()

        if (isModelAvailable()) {
            return modelFile
        }

        context.assets.open(
            ModelConfig.MODEL_ASSET_PATH
        ).use { input ->

            modelFile.outputStream().use { output ->
                input.copyTo(
                    output,
                    bufferSize = 1024 * 1024
                )
            }
        }

        return modelFile
    }

    fun getModelSize(): Long {
        return if (isModelAvailable()) {
            getModelFile().length()
        } else {
            0L
        }
    }

    fun deleteModel() {
        val modelFile = getModelFile()

        if (modelFile.exists()) {
            modelFile.delete()
        }
    }
}