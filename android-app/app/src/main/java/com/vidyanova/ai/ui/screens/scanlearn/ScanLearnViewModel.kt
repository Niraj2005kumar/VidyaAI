package com.vidyanova.ai.ui.screens.scanlearn

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.vidyanova.ai.data.local.database.AppDatabase
import com.vidyanova.ai.data.local.entities.ScanRecordEntity
import com.vidyanova.ai.data.local.sync.SyncWorker
import com.vidyanova.ai.ocr.MLKitOCR
import com.vidyanova.ai.ocr.OCRProcessor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

data class ScanLearnUiState(
    val imageUri: Uri? = null,
    val extractedText: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class ScanLearnViewModel(application: Application) : AndroidViewModel(application) {

    private val ocrEngine = MLKitOCR()
    private val db = AppDatabase.getInstance(application)

    private val _uiState = MutableStateFlow(ScanLearnUiState())
    val uiState: StateFlow<ScanLearnUiState> = _uiState.asStateFlow()

    fun createTempImageUri(): Uri {
        val imagesDir = File(getApplication<Application>().cacheDir, "scan_images")
        if (!imagesDir.exists()) {
            imagesDir.mkdirs()
        }
        val file = File(imagesDir, "scan_${System.currentTimeMillis()}.jpg")
        return androidx.core.content.FileProvider.getUriForFile(
            getApplication(),
            "${getApplication<Application>().packageName}.fileprovider",
            file
        )
    }

    fun processImage(imageUri: Uri) {
        _uiState.value = _uiState.value.copy(
            imageUri = imageUri,
            isLoading = true,
            errorMessage = null
        )

        viewModelScope.launch(Dispatchers.IO) {
            try {
                Log.i(TAG, "Starting OCR for image: $imageUri")
                val rawText = ocrEngine.extractText(getApplication(), imageUri)
                val cleanedText = OCRProcessor.cleanText(rawText)

                if (cleanedText.isNotBlank()) {
                    _uiState.value = _uiState.value.copy(
                        extractedText = cleanedText,
                        isLoading = false,
                        errorMessage = null
                    )
                    Log.i(TAG, "OCR success: ${cleanedText.length} chars extracted")

                    try {
                        db.scanRecordDao().insert(
                            ScanRecordEntity(
                                text = cleanedText,
                                synced = false
                            )
                        )
                        SyncWorker.scheduleOneTimeSync(getApplication())
                    } catch (e: Exception) {
                        Log.e(TAG, "Error saving scan record: ${e.message}")
                    }
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = OCRProcessor.getEmptyTextErrorMessage()
                    )
                    Log.w(TAG, "OCR result empty")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error performing OCR: ${e.message}", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to extract text from photo: ${e.message}"
                )
            }
        }
    }

    fun updateExtractedText(newText: String) {
        _uiState.value = _uiState.value.copy(
            extractedText = newText,
            errorMessage = null
        )
    }

    fun clearImageAndText() {
        _uiState.value = ScanLearnUiState()
    }

    companion object {
        private const val TAG = "ScanLearnViewModel"
    }
}
