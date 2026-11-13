
package com.vidyanova.ai.ai.model

import android.content.Context
import android.util.Log
import java.io.File
import java.io.IOException

class ModelManager(
    private val context: Context
) {

    private fun getCandidateLocations(): List<File> {
        val fileName = ModelConfig.MODEL_FILE_NAME

        return listOfNotNull(
            File(context.filesDir, "model/$fileName"),
            context.getExternalFilesDir(null)?.let {
                File(it, "model/$fileName")
            },
            context.getExternalFilesDir(null)?.let {
                File(it, fileName)
            }
        )
    }

    fun getModelFile(): File {
        val candidates = getCandidateLocations()

        for (candidate in candidates) {
            if (isValidGguf(candidate)) {
                Log.i(TAG, "Valid GGUF found: ${candidate.absolutePath}")
                return candidate
            }
        }

        val defaultFile = File(
            context.filesDir,
            "model/${ModelConfig.MODEL_FILE_NAME}"
        )

        if (!defaultFile.parentFile.exists()) {
            defaultFile.parentFile?.mkdirs()
        }

        return defaultFile
    }

    private fun isValidGguf(file: File): Boolean {
        if (!file.exists() || !file.isFile || file.length() < 4L) {
            return false
        }

        return try {
            file.inputStream().use { input ->
                val header = ByteArray(4)
                if (input.read(header) != 4) return false

                header.contentEquals(
                    byteArrayOf(
                        'G'.code.toByte(),
                        'G'.code.toByte(),
                        'U'.code.toByte(),
                        'F'.code.toByte()
                    )
                )
            }
        } catch (e: IOException) {
            Log.e(TAG, "GGUF validation failed", e)
            false
        }
    }

    fun isModelAvailable(): Boolean {
        val modelFile = getModelFile()
        val valid = isValidGguf(modelFile)

        Log.d(
            TAG,
            "Model check: path=${modelFile.absolutePath}, " +
                "valid=$valid, size=${modelFile.length()}"
        )

        return valid
    }

    fun prepareModel(): File {
        val modelFile = getModelFile()

        if (!isValidGguf(modelFile)) {
            throw IOException(
                """
                ${ModelConfig.MODEL_DISPLAY_NAME} model not found
                or invalid.

                Expected location:
                ${modelFile.absolutePath}

                Expected file:
                ${ModelConfig.MODEL_FILE_NAME}

                Please place a valid GGUF model in the app model directory.
                """.trimIndent()
            )
        }

        Log.i(
            TAG,
            "Model ready: ${modelFile.absolutePath}, " +
                "size=${modelFile.length()} bytes"
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

    /**
     * Call this only after native inference has released the model.
     */
    fun deleteModel(): Boolean {
        val modelFile = getModelFile()

        if (!modelFile.exists()) return true

        return try {
            val deleted = modelFile.delete()
            Log.i(TAG, "Model deletion result: $deleted")
            deleted
        } catch (e: SecurityException) {
            Log.e(TAG, "Unable to delete model", e)
            false
        }
    }

    private companion object {
        const val TAG = "ModelManager"
    }
}
