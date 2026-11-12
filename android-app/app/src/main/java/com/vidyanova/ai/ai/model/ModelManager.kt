package com.vidyanova.ai.ai.model

import android.content.Context
import android.util.Log
import java.io.File
import java.io.IOException

class ModelManager(
    private val context: Context
) {

    fun getModelFile(): File {
        val fileName = ModelConfig.MODEL_FILE_NAME

        val candidateLocations = listOfNotNull(
            File(context.filesDir, "model/$fileName"),
            context.getExternalFilesDir(null)?.let { File(it, "model/$fileName") },
            context.getExternalFilesDir(null)?.let { File(it, fileName) },
            File("/sdcard/Android/data/${context.packageName}/files/model/$fileName"),
            File("/sdcard/Android/data/${context.packageName}/files/$fileName"),
            File("/sdcard/Download/$fileName"),
            File("/sdcard/$fileName"),
            File("/data/local/tmp/$fileName")
        )

        for (candidate in candidateLocations) {
            if (candidate.exists() && candidate.isFile && candidate.length() > 0L) {
                Log.i(TAG, "Found valid model file at: ${candidate.absolutePath} (${candidate.length()} bytes)")
                return candidate
            }
        }

        val defaultFile = File(context.filesDir, "model/$fileName")
        defaultFile.parentFile?.mkdirs()
        return defaultFile
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
            "Model found successfully: ${modelFile.absolutePath} (${modelFile.length()} bytes)"
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
