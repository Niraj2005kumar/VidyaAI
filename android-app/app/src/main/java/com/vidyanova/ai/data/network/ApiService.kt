package com.vidyanova.ai.data.network

import com.vidyanova.ai.utils.Constants
import com.google.gson.JsonObject
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.POST
import retrofit2.http.Query

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
    val learningTopics: Int,
    val weakAreas: Int,
    val totalQuestionsAsked: Int,
    val totalQuizzesAttempted: Int,
    val totalStudyTimeMinutes: Int,
    val averageScore: Double
)

data class ProgressSummaryResponse(
    val success: Boolean,
    val summary: ProgressSummary?
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

    @GET(Constants.USER_PROFILE_ENDPOINT)
    suspend fun getUserProfile(): Response<JsonObject>

    @PUT(Constants.USER_PROFILE_ENDPOINT)
    suspend fun updateUserProfile(@Body updates: JsonObject): Response<JsonObject>

    @POST(Constants.STUDY_START_ENDPOINT)
    suspend fun startStudySession(@Body session: JsonObject): Response<JsonObject>

    @PUT(Constants.STUDY_END_ENDPOINT)
    suspend fun endStudySession(@Path("id") sessionId: String): Response<JsonObject>

    @GET(Constants.STUDY_HISTORY_ENDPOINT)
    suspend fun getStudyHistory(): Response<JsonObject>

    @GET(Constants.STUDY_TOTAL_TIME_ENDPOINT)
    suspend fun getStudyTotalTime(): Response<JsonObject>

    @GET(Constants.PROGRESS_ENDPOINT)
    suspend fun getProgress(
        @Query("classLevel") classLevel: Int? = null,
        @Query("subject") subject: String? = null
    ): Response<JsonObject>

    @POST(Constants.PROGRESS_UPDATE_ENDPOINT)
    suspend fun updateProgress(@Body progress: JsonObject): Response<JsonObject>

    @GET(Constants.WEAK_AREAS_ENDPOINT)
    suspend fun getWeakAreas(): Response<JsonObject>

    @GET(Constants.PROGRESS_SUMMARY_ENDPOINT)
    suspend fun getProgressSummary(): Response<ProgressSummaryResponse>

    @GET(Constants.QUIZ_ENDPOINT)
    suspend fun getQuizzes(
        @Query("classLevel") classLevel: Int? = null,
        @Query("subject") subject: String? = null,
        @Query("chapter") chapter: String? = null
    ): Response<JsonObject>

    @GET("${Constants.QUIZ_ENDPOINT}/{id}")
    suspend fun getQuiz(@Path("id") quizId: String): Response<JsonObject>

    @POST("${Constants.QUIZ_ENDPOINT}/{id}/submit")
    suspend fun submitQuiz(
        @Path("id") quizId: String,
        @Body answers: JsonObject
    ): Response<JsonObject>

    @GET(Constants.QUIZ_ATTEMPTS_ENDPOINT)
    suspend fun getMyQuizAttempts(): Response<JsonObject>

    @GET(Constants.OPPORTUNITIES_ENDPOINT)
    suspend fun getOpportunities(
        @Query("type") type: String? = null,
        @Query("classLevel") classLevel: Int? = null
    ): Response<JsonObject>

    @GET("${Constants.OPPORTUNITIES_ENDPOINT}/{id}")
    suspend fun getOpportunity(@Path("id") opportunityId: String): Response<JsonObject>
}
