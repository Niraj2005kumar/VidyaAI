package com.vidyanova.ai.data.repository

import android.util.Log
import com.google.gson.JsonObject
import com.vidyanova.ai.data.network.AuthTokenStore
import com.vidyanova.ai.data.network.RetrofitClient
import java.io.IOException

object ProgressRepository {

    suspend fun recordTutorQuestion(
        classLevel: Int,
        subject: String,
        chapter: String,
        topic: String
    ) {
        if (AuthTokenStore.getToken().isNullOrBlank()) {
            Log.i(TAG, "Skipped tutor progress sync because the user is not signed in.")
            return
        }

        try {
            val currentResponse = RetrofitClient.apiService.getProgress(
                classLevel = classLevel,
                subject = subject
            )
            if (!currentResponse.isSuccessful) {
                Log.w(TAG, "Progress lookup failed with HTTP ${currentResponse.code()}.")
                return
            }

            val existingProgress = currentResponse.body()
                ?.getAsJsonArray("progress")
                ?.firstOrNull { item ->
                    val entry = item.asJsonObject
                    entry.stringValue("chapter") == chapter &&
                        entry.stringValue("topic") == topic
                }
                ?.asJsonObject

            val update = JsonObject().apply {
                addProperty("classLevel", classLevel)
                addProperty("subject", subject)
                addProperty("chapter", chapter)
                addProperty("topic", topic)
                addProperty(
                    "questionsAsked",
                    (existingProgress?.get("questionsAsked")?.asInt ?: 0) + 1
                )
            }

            val updateResponse = RetrofitClient.apiService.updateProgress(update)
            if (!updateResponse.isSuccessful) {
                Log.w(TAG, "Progress update failed with HTTP ${updateResponse.code()}.")
            }
        } catch (error: IOException) {
            Log.w(TAG, "Could not sync tutor progress; local AI remains available.", error)
        }
    }

    private fun JsonObject.stringValue(name: String): String =
        get(name)?.takeUnless { it.isJsonNull }?.asString.orEmpty()

    private const val TAG = "ProgressRepository"
}