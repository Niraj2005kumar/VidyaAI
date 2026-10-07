package com.vidyanova.ai.data.network

import com.vidyanova.ai.utils.Constants
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String,
    val role: String = "student",
    val classLevel: Int,
    val preferredLanguage: String = "english"
)

data class ApiResponse(
    val success: Boolean,
    val message: String?
)

data class AuthResponse(
    val success: Boolean,
    val message: String?,
    val token: String?
)

data class ProgressSummary(
    val totalTopics: Int,
    val masteredTopics: Int,
    val weakTopics: Int,
    val totalQuestions: Int,
    val totalQuizzes: Int,
    val totalStudyTimeMinutes: Int,
    val averageScore: Double
)

data class ProgressSummaryResponse(
    val success: Boolean,
    val data: ProgressSummary?
)

interface ApiService {

    @GET(Constants.HEALTH_ENDPOINT)
    suspend fun healthCheck(): Response<ApiResponse>

    @POST(Constants.LOGIN_ENDPOINT)
    suspend fun login(
        @Body request: LoginRequest
    ): Response<AuthResponse>

    @POST(Constants.REGISTER_ENDPOINT)
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<AuthResponse>

    @GET(Constants.PROGRESS_SUMMARY_ENDPOINT)
    suspend fun getProgressSummary(): Response<ProgressSummaryResponse>
}
