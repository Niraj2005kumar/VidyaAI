package com.vidyanova.ai.ui.screens.tutor

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.vidyanova.ai.ai.instruction.TeachingMode
import com.vidyanova.ai.data.repository.ProgressRepository
import com.vidyanova.ai.data.repository.TutorRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

enum class UiStatus { IDLE, LOADING, SUCCESS, ERROR }

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val isGenerating: Boolean = false,
    val status: UiStatus = UiStatus.IDLE,
    val errorMessage: String? = null
)

class TutorViewModel(application: Application) : AndroidViewModel(application) {

    private val tutorRepository = TutorRepository(application)

    private val _uiState = MutableStateFlow(
        ChatUiState(
            messages = listOf(
                ChatMessage(
                    text = "Namaste! Main hoon ViyaAI, aapka offline study companion. 📚\nAap kisi bhi concept ya question ke baare mein pooch sakte hain, bina internet ke!",
                    isFromUser = false,
                    timestamp = "Just now",
                    styleTag = "Step-by-Step"
                )
            )
        )
    )
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    fun sendMessage(
        userText: String,
        classLevel: Int = 10,
        subject: String = "Mathematics",
        chapter: String? = null,
        topic: String? = null,
        preferredMode: TeachingMode? = null,
        preferredLanguage: String? = null
    ) {
        val requestId = UUID.randomUUID().toString().subSequence(0, 8).toString()
        Log.i(TAG, "CHAT_SEND_STARTED | RequestId: $requestId | Question: $userText")

        if (_uiState.value.isGenerating) {
            Log.w(TAG, "CHAT_REQUEST_BLOCKED | RequestId: $requestId | Model is currently generating another response.")
            return
        }

        if (userText.isBlank()) {
            val emptyMsg = ChatMessage(
                text = "Please apna question likho.",
                isFromUser = false
            )
            _uiState.value = _uiState.value.copy(
                messages = _uiState.value.messages + emptyMsg
            )
            return
        }

        val userMsg = ChatMessage(
            text = userText,
            isFromUser = true,
            timestamp = "Just now"
        )

        val pendingMsg = ChatMessage(
            text = "ViyaAI is preparing an on-device answer…",
            isFromUser = false,
            styleTag = preferredMode?.name ?: "Step-by-Step"
        )

        val updatedMessages = _uiState.value.messages + userMsg + pendingMsg

        _uiState.value = _uiState.value.copy(
            messages = updatedMessages,
            isGenerating = true,
            status = UiStatus.LOADING,
            errorMessage = null
        )

        Log.i(TAG, "VIEWMODEL_REQUEST_STARTED | RequestId: $requestId")

        viewModelScope.launch(Dispatchers.IO) {
            val startTime = System.currentTimeMillis()
            try {
                Log.i(TAG, "REPOSITORY_REQUEST_STARTED | RequestId: $requestId")
                val result = tutorRepository.askTutor(
                    question = userText,
                    classLevel = classLevel,
                    subject = subject,
                    chapter = chapter,
                    topic = topic,
                    preferredMode = preferredMode,
                    preferredLanguage = preferredLanguage
                )
                val durationMs = System.currentTimeMillis() - startTime

                withContext(Dispatchers.Main) {
                    val currentList = _uiState.value.messages.toMutableList()
                    val pendingIndex = currentList.indexOfFirst { it.id == pendingMsg.id }

                    if (result.success) {
                        Log.i(TAG, "LOCAL_MODEL_GENERATION_COMPLETED | RequestId: $requestId | Duration: ${durationMs}ms")
                        if (pendingIndex >= 0) {
                            currentList[pendingIndex] = pendingMsg.copy(
                                text = result.answer,
                                styleTag = result.mode.replace('_', ' ')
                            )
                        }
                        _uiState.value = _uiState.value.copy(
                            messages = currentList,
                            isGenerating = false,
                            status = UiStatus.SUCCESS,
                            errorMessage = null
                        )
                        Log.i(TAG, "VIEWMODEL_RESPONSE_UPDATED | RequestId: $requestId | Success | Answer Length: ${result.answer.length}")
                        Log.i(TAG, "CHAT_UI_RESPONSE_RENDERED | RequestId: $requestId")
                    } else {
                        Log.e(TAG, "LOCAL_MODEL_GENERATION_FAILED | RequestId: $requestId | Error: ${result.answer}")
                        if (pendingIndex >= 0) {
                            currentList[pendingIndex] = pendingMsg.copy(
                                text = "⚠️ ${result.answer}",
                                styleTag = "Error"
                            )
                        }
                        _uiState.value = _uiState.value.copy(
                            messages = currentList,
                            isGenerating = false,
                            status = UiStatus.ERROR,
                            errorMessage = result.answer
                        )
                        Log.i(TAG, "VIEWMODEL_RESPONSE_UPDATED | RequestId: $requestId | Error")
                        Log.i(TAG, "CHAT_UI_RESPONSE_RENDERED | RequestId: $requestId")
                    }
                }

                if (result.success) {
                    try {
                        ProgressRepository.recordTutorQuestion(
                            classLevel = classLevel,
                            subject = subject,
                            chapter = chapter ?: "General",
                            topic = topic ?: "Tutor questions"
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "Error updating progress history: ${e.message}")
                    }
                }

            } catch (throwable: Throwable) {
                val durationMs = System.currentTimeMillis() - startTime
                Log.e(TAG, "CHAT_REQUEST_FAILED | RequestId: $requestId | Duration: ${durationMs}ms | Cause: ${throwable.message}", throwable)

                withContext(Dispatchers.Main) {
                    val currentList = _uiState.value.messages.toMutableList()
                    val pendingIndex = currentList.indexOfFirst { it.id == pendingMsg.id }
                    val errorText = "LOCAL_MODEL_INFERENCE_FAILED: ${throwable.message ?: "Unknown error"}"

                    if (pendingIndex >= 0) {
                        currentList[pendingIndex] = pendingMsg.copy(
                            text = "⚠️ $errorText",
                            styleTag = "Error"
                        )
                    }

                    _uiState.value = _uiState.value.copy(
                        messages = currentList,
                        isGenerating = false,
                        status = UiStatus.ERROR,
                        errorMessage = errorText
                    )
                    Log.i(TAG, "CHAT_UI_RESPONSE_RENDERED | RequestId: $requestId")
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        try {
            tutorRepository.releaseModel()
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing model on ViewModel cleared", e)
        }
    }

    companion object {
        private const val TAG = "TutorViewModel"
    }
}
