package com.vidyanova.ai.data.repository

import com.vidyanova.ai.data.network.AuthResponse
import com.vidyanova.ai.data.network.AuthTokenStore
import com.vidyanova.ai.data.network.LoginRequest
import com.vidyanova.ai.data.network.RegisterRequest
import com.vidyanova.ai.data.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.io.IOException

object AuthRepository {
    suspend fun login(request: LoginRequest): Response<AuthResponse> =
        withContext(Dispatchers.IO) {
            saveTokenFromResponse(RetrofitClient.apiService.login(request))
        }

    suspend fun register(request: RegisterRequest): Response<AuthResponse> =
        withContext(Dispatchers.IO) {
            saveTokenFromResponse(RetrofitClient.apiService.register(request))
        }

    fun logout() {
        AuthTokenStore.clearToken()
    }

    private fun saveTokenFromResponse(response: Response<AuthResponse>): Response<AuthResponse> {
        if (response.isSuccessful) {
            val token = response.body()?.token
                ?: throw IOException("Authentication response did not contain a token")
            AuthTokenStore.saveToken(token)
        }
        return response
    }
}
