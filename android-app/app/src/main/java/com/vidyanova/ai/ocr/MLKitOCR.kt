package com.vidyanova.ai.ocr

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.util.Log
import androidx.exifinterface.media.ExifInterface
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.devanagari.DevanagariTextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.util.concurrent.TimeUnit

class MLKitOCR {

    private val recognizer by lazy {
        TextRecognition.getClient(DevanagariTextRecognizerOptions.Builder().build())
    }

    suspend fun extractText(context: Context, imageUri: Uri): String = withContext(Dispatchers.IO) {
        val bitmap = loadAndRotateBitmap(context, imageUri)
            ?: return@withContext ""

        val inputImage = InputImage.fromBitmap(bitmap, 0)
        var extractedText = ""

        try {
            val task = recognizer.process(inputImage)
            val textObj = Tasks.await(task, 15, TimeUnit.SECONDS)
            extractedText = textObj.text.trim()
        } catch (e: Exception) {
            Log.e(TAG, "OCR recognition error: ${e.message}", e)
        } finally {
            bitmap.recycle()
        }

        Log.i(TAG, "OCR extracted ${extractedText.length} characters.")
        extractedText
    }

    private fun loadAndRotateBitmap(context: Context, uri: Uri): Bitmap? {
        return try {
            var inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream?.close()

            val maxDim = 1600
            var sampleSize = 1
            while (options.outWidth / sampleSize > maxDim || options.outHeight / sampleSize > maxDim) {
                sampleSize *= 2
            }

            inputStream = context.contentResolver.openInputStream(uri)
            val decodeOptions = BitmapFactory.Options().apply { inSampleSize = sampleSize }
            val sampledBitmap = BitmapFactory.decodeStream(inputStream, null, decodeOptions)
            inputStream?.close()

            if (sampledBitmap == null) return null

            val exifInputStream = context.contentResolver.openInputStream(uri)
            val exif = exifInputStream?.let { ExifInterface(it) }
            val orientation = exif?.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
                ?: ExifInterface.ORIENTATION_NORMAL
            exifInputStream?.close()

            val matrix = Matrix()
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            }

            if (!matrix.isIdentity) {
                Bitmap.createBitmap(sampledBitmap, 0, 0, sampledBitmap.width, sampledBitmap.height, matrix, true)
            } else {
                sampledBitmap
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading/rotating bitmap: ${e.message}", e)
            null
        }
    }

    companion object {
        private const val TAG = "MLKitOCR"
    }
}
