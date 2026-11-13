
package com.vidyanova.ai.ui.screens.tutor

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.vidyanova.ai.ai.instruction.TeachingMode
import com.vidyanova.ai.data.repository.ProgressRepository
import com.vidyanova.ai.data.repository.TutorRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

enum class UiStatus {
    IDLE, LOADING, SUCCESS, ERROR
}

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val isGenerating: Boolean = false,
    val status: UiStatus = UiStatus.IDLE,
    val errorMessage: String? = null
)

class TutorViewModel(application: Application) :
    AndroidViewModel(application) {

    private val tutorRepository = TutorRepository(application)

    private val generationMutex = Mutex()

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

    val uiState = _uiState.asStateFlow()

    fun sendMessage(
        userText: String,
        classLevel: Int = 10,
        subject: String = "Mathematics",
        chapter: String? = null,
        topic: String? = null,
        preferredMode: TeachingMode? = null,
        preferredLanguage: String? = null
    ) {
        if (userText.isBlank()) return

        if (!generationMutex.tryLock()) {
            Log.w(TAG, "Another generation is already running.")
            return
        }

        val requestId = UUID.randomUUID().toString().take(8)

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

        _uiState.update { current ->
            current.copy(
                messages = current.messages + userMsg + pendingMsg,
                isGenerating = true,
                status = UiStatus.LOADING,
                errorMessage = null
            )
        }

        viewModelScope.launch {
            val startTime = System.currentTimeMillis()

            try {
                Log.i(TAG, "CHAT_STARTED | RequestId: $requestId")

                val result = withContext(Dispatchers.IO) {
                    tutorRepository.askTutor(
                        question = userText,
                        classLevel = classLevel,
                        subject = subject,
                        chapter = chapter,
                        topic = topic,
                        preferredMode = preferredMode,
                        preferredLanguage = preferredLanguage
                    )
                }

                val duration = System.currentTimeMillis() - startTime

                if (result.success) {
                    updatePendingMessage(
                        pendingMsg = pendingMsg,
                        response = result.answer,
                        styleTag = result.mode.replace('_', ' '),
                        status = UiStatus.SUCCESS
                    )

                    Log.i(
                        TAG,
                        "LOCAL_MODEL_SUCCESS | RequestId: $requestId | Duration: ${duration}ms"
                    )

                    try {
                        withContext(Dispatchers.IO) {
                            ProgressRepository.recordTutorQuestion(
                                classLevel = classLevel,
                                subject = subject,
                                chapter = chapter ?: "General",
                                topic = topic ?: "Tutor questions"
                            )
                        }
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        Log.e(TAG, "Progress history update failed", e)
                    }

                } else {
                    updatePendingMessage(
                        pendingMsg = pendingMsg,
                        response = "⚠️ ${result.answer}",
                        styleTag = "Error",
                        status = UiStatus.ERROR,
                        error = result.answer
                    )

                    Log.e(TAG, "LOCAL_MODEL_FAILED: ${result.answer}")
                }

            } catch (e: CancellationException) {
                Log.w(TAG, "CHAT_CANCELLED | RequestId: $requestId")
                throw e

            } catch (e: Exception) {
                val errorText =
                    "LOCAL_MODEL_INFERENCE_FAILED: ${e.message ?: "Unknown error"}"

                Log.e(TAG, "CHAT_REQUEST_FAILED | RequestId: $requestId", e)

                updatePendingMessage(
                    pendingMsg = pendingMsg,
                    response = "⚠️ $errorText",
                    styleTag = "Error",
                    status = UiStatus.ERROR,
                    error = errorText
                )

            } finally {
                generationMutex.unlock()

                _uiState.update { current ->
                    current.copy(
                        isGenerating = false,
                        status = if (current.status == UiStatus.LOADING) {
                            UiStatus.IDLE
                        } else {
                            current.status
                        }
                    )
                }

                Log.i(TAG, "GENERATION_FINISHED | RequestId: $requestId")
            }
        }
    }

    private fun updatePendingMessage(
        pendingMsg: ChatMessage,
        response: String,
        styleTag: String,
        status: UiStatus,
        error: String? = null
    ) {
        _uiState.update { current ->
            val updatedMessages = current.messages.map { message ->
                if (message.id == pendingMsg.id) {
                    message.copy(
                        text = response,
                        styleTag = styleTag
                    )
                } else {
                    message
                }
            }

            current.copy(
                messages = updatedMessages,
                status = status,
                errorMessage = error
            )
        }
    }

    override fun onCleared() {
        super.onCleared()

        Thread({
            try {
                tutorRepository.releaseModel()
                Log.i(TAG, "Local model released")
            } catch (e: Exception) {
                Log.e(TAG, "Model release failed", e)
            }
        }, "ViyaAI-ModelRelease").apply {
            isDaemon = true
            start()
        }
    }

    companion object {
        private const val TAG = "TutorViewModel"
    }
}
