package com.vidyanova.ai.ai.model

import android.content.Context
import android.util.Log
import java.io.File
import java.io.FileOutputStream
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

        if (defaultFile.parentFile?.exists() != true) {
            defaultFile.parentFile?.mkdirs()
        }

        return defaultFile
    }

    fun isValidGguf(file: File?): Boolean {
        if (file == null || !file.exists() || !file.isFile || file.length() < 4L) {
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

    @Throws(IOException::class)
    fun ensureModelFromAssets(onProgress: ((Int) -> Unit)? = null): File {
        val targetFile = File(context.filesDir, "model/${ModelConfig.MODEL_FILE_NAME}")

        if (targetFile.parentFile?.exists() != true) {
            targetFile.parentFile?.mkdirs()
        }

        val assetName = ModelConfig.MODEL_FILE_NAME
        val hasAsset = try {
            context.assets.list("")?.contains(assetName) == true
        } catch (e: Exception) {
            false
        }

        if (isValidGguf(targetFile)) {
            if (hasAsset) {
                try {
                    val assetFd = context.assets.openFd(assetName)
                    val assetLength = assetFd.length
                    assetFd.close()
                    if (targetFile.length() == assetLength) {
                        Log.i(TAG, "Model file already matches asset length: ${targetFile.length()} bytes")
                        onProgress?.invoke(100)
                        return targetFile
                    } else {
                        Log.i(TAG, "Asset model length ($assetLength) differs from local model (${targetFile.length()}). Re-copying...")
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Unable to inspect asset file descriptor: ${e.message}")
                    onProgress?.invoke(100)
                    return targetFile
                }
            } else {
                onProgress?.invoke(100)
                return targetFile
            }
        }

        if (!hasAsset) {
            val candidate = getCandidateLocations().firstOrNull { isValidGguf(it) }
            if (candidate != null) {
                onProgress?.invoke(100)
                return candidate
            }
            throw IOException("${ModelConfig.MODEL_DISPLAY_NAME} asset not found and no local model exists at ${targetFile.absolutePath}")
        }

        // Check storage space (minimum 1.5 GBusable space)
        val minRequiredBytes = 1500 * 1024 * 1024L
        val usableStorage = context.filesDir.usableSpace
        if (usableStorage < minRequiredBytes) {
            throw IOException("Kam se kam 1.5 GB storage khali karein. (Usable: ${usableStorage / (1024 * 1024)} MB)")
        }

        Log.i(TAG, "Copying model from assets/$assetName to ${targetFile.absolutePath}...")

        val tmpFile = File(targetFile.parentFile, "${ModelConfig.MODEL_FILE_NAME}.tmp")
        if (tmpFile.exists()) {
            tmpFile.delete()
        }

        try {
            val assetFd = context.assets.openFd(assetName)
            val totalBytes = assetFd.length
            assetFd.close()

            context.assets.open(assetName).use { input ->
                FileOutputStream(tmpFile).use { output ->
                    val buffer = ByteArray(8 * 1024 * 1024) // 8 MB buffer
                    var bytesCopied = 0L
                    var bytesRead: Int

                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        bytesCopied += bytesRead
                        if (totalBytes > 0) {
                            val percent = ((bytesCopied * 100) / totalBytes).toInt()
                            onProgress?.invoke(percent)
                        }
                    }
                }
            }

            if (!isValidGguf(tmpFile)) {
                tmpFile.delete()
                throw IOException("Copied GGUF model failed header validation.")
            }

            if (targetFile.exists()) {
                targetFile.delete()
            }

            if (!tmpFile.renameTo(targetFile)) {
                tmpFile.copyTo(targetFile, overwrite = true)
                tmpFile.delete()
            }

            Log.i(TAG, "Model asset copy completed successfully (${targetFile.length()} bytes)")
            onProgress?.invoke(100)
            return targetFile
        } catch (e: Exception) {
            if (tmpFile.exists()) tmpFile.delete()
            Log.e(TAG, "Error copying model from assets: ${e.message}", e)
            throw IOException("Model setup failed: ${e.message}", e)
        }
    }

    fun prepareModel(): File {
        return ensureModelFromAssets(null)
    }

    fun getModelSize(): Long {
        return if (isModelAvailable()) {
            getModelFile().length()
        } else {
            0L
        }
    }

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
