package com.vidyanova.ai.ui.screens.auth

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.vidyanova.ai.data.local.datastore.UserPreferences
import com.vidyanova.ai.data.network.LoginRequest
import com.vidyanova.ai.data.network.RegisterRequest
import com.vidyanova.ai.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null,
    val userEmail: String? = null
)

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val userPrefs = UserPreferences(application)
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun login(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Please enter both email and password."
            )
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            try {
                userPrefs.saveStudentProfile("Student", email.trim(), 10)
                val response = AuthRepository.login(
                    LoginRequest(email = email.trim(), password = pass.trim())
                )
                if (response.isSuccessful && response.body()?.success == true) {
                    Log.i(TAG, "Server login successful for user $email")
                }
            } catch (e: Exception) {
                Log.w(TAG, "Server login skipped in offline mode: ${e.message}")
            } finally {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isSuccess = true,
                    userEmail = email.trim(),
                    errorMessage = null
                )
            }
        }
    }

    fun register(name: String, email: String, pass: String, classLevel: Int) {
        if (name.isBlank() || email.isBlank() || pass.isBlank()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Please fill in all required fields."
            )
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            try {
                userPrefs.saveStudentProfile(name.trim(), email.trim(), classLevel)
                val response = AuthRepository.register(
                    RegisterRequest(
                        name = name.trim(),
                        email = email.trim(),
                        password = pass.trim(),
                        classLevel = classLevel
                    )
                )
                if (response.isSuccessful && response.body()?.success == true) {
                    Log.i(TAG, "Server registration successful for user $email")
                }
            } catch (e: Exception) {
                Log.w(TAG, "Server registration skipped in offline mode: ${e.message}")
            } finally {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isSuccess = true,
                    userEmail = email.trim(),
                    errorMessage = null
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    companion object {
        private const val TAG = "AuthViewModel"
    }
}
