package com.vidyanova.ai.ai.model

import android.content.Context
import android.util.Log
import java.io.File
import java.io.IOException

class ModelManager(
    private val context: Context
) {

    fun getModelFile(): File {
        val baseDir = context.filesDir

        val modelDir = File(baseDir, "model")

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

        val exists = modelFile.exists()
        val isFile = modelFile.isFile
        val size = modelFile.length()

        Log.d(
            TAG,
            "Model check: path=${modelFile.absolutePath}, " +
                    "exists=$exists, isFile=$isFile, size=$size"
        )

        return exists && isFile && size > 0L
    }

    fun prepareModel(): File {
        val modelFile = getModelFile()

        if (!isModelAvailable()) {
            throw IOException(
                """
                ${ModelConfig.MODEL_DISPLAY_NAME} model not found.

                Expected model location:
                ${modelFile.absolutePath}

                exists=${modelFile.exists()}
                isFile=${modelFile.isFile}
                size=${modelFile.length()}

                Please copy:
                ${ModelConfig.MODEL_FILE_NAME}
                to the app model directory.
                """.trimIndent()
            )
        }

        Log.d(
            TAG,
            "Model found successfully: ${modelFile.absolutePath}"
        )

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

    private companion object {
        const val TAG = "ModelManager"
    }
}