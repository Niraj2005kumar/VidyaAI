package com.vidyanova.ai.data.local.sync

import android.content.Context
import android.util.Log
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.vidyanova.ai.data.local.database.AppDatabase
import com.vidyanova.ai.data.local.datastore.UserPreferences
import com.vidyanova.ai.data.local.entities.CachedOpportunityEntity
import com.vidyanova.ai.data.local.entities.CachedQuizEntity
import com.vidyanova.ai.data.network.AuthTokenStore
import com.vidyanova.ai.data.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class SyncManager(private val context: Context) {

    private val db = AppDatabase.getInstance(context)
    private val userPrefs = UserPreferences(context)

    suspend fun performFullSync(): Boolean = withContext(Dispatchers.IO) {
        val consent = userPrefs.syncConsentFlow.first()
        if (consent == false) {
            Log.i(TAG, "Sync skipped: User has disabled cloud sync consent.")
            return@withContext false
        }

        try {
            ensureDeviceToken()
            pushUnsyncedData()
            pullServerData()
            Log.i(TAG, "Full offline-first sync completed successfully.")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Sync failed: ${e.message}", e)
            false
        }
    }

    private suspend fun ensureDeviceToken() {
        var token = userPrefs.authTokenFlow.first()
        if (token.isNullOrBlank() || token.startsWith("offline_local_token_")) {
            val deviceId = userPrefs.getOrCreateDeviceId()
            Log.i(TAG, "Requesting real device auth token for deviceId: $deviceId")

            val authReq = JsonObject().apply {
                addProperty("deviceId", deviceId)
                addProperty("name", userPrefs.studentNameFlow.first())
                addProperty("classLevel", userPrefs.classLevelFlow.first())
            }

            try {
                val response = RetrofitClient.apiService.authDevice(authReq)
                if (response.isSuccessful && response.body()?.success == true) {
                    val newToken = response.body()?.token
                    if (!newToken.isNullOrBlank()) {
                        userPrefs.saveAuthToken(newToken)
                        AuthTokenStore.saveToken(newToken)
                        Log.i(TAG, "Real MongoDB Device token acquired and saved.")
                    }
                } else {
                    Log.w(TAG, "Device token request failed HTTP ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Unable to reach auth server: ${e.message}")
            }
        } else {
            AuthTokenStore.saveToken(token)
        }
    }

    private suspend fun pushUnsyncedData() {
        val unsyncedChats = db.chatMessageDao().getUnsyncedMessages()
        val unsyncedSessions = db.studySessionDao().getUnsyncedSessions()
        val unsyncedProgress = db.topicProgressDao().getUnsyncedProgress()
        val unsyncedAttempts = db.quizAttemptDao().getUnsyncedAttempts()
        val unsyncedScans = db.scanRecordDao().getUnsyncedScans()

        if (unsyncedChats.isEmpty() && unsyncedSessions.isEmpty() &&
            unsyncedProgress.isEmpty() && unsyncedAttempts.isEmpty() && unsyncedScans.isEmpty()) {
            Log.i(TAG, "No unsynced local rows to push.")
            return
        }

        val pushBody = JsonObject().apply {
            // Chat messages
            val chatsArr = JsonArray()
            unsyncedChats.forEach { msg ->
                chatsArr.add(JsonObject().apply {
                    addProperty("clientId", msg.clientId)
                    addProperty("sessionId", msg.sessionId)
                    addProperty("role", msg.role)
                    addProperty("text", msg.text)
                    addProperty("mode", msg.mode)
                    addProperty("language", msg.language)
                    addProperty("timestamp", msg.timestamp)
                })
            }
            add("chatMessages", chatsArr)

            // Study sessions
            val sessionsArr = JsonArray()
            unsyncedSessions.forEach { sess ->
                sessionsArr.add(JsonObject().apply {
                    addProperty("clientId", sess.clientId)
                    addProperty("subject", sess.subject)
                    addProperty("topic", sess.topic)
                    addProperty("startTime", sess.startTime)
                    addProperty("endTime", sess.endTime ?: sess.startTime)
                    addProperty("durationMinutes", sess.durationMinutes)
                })
            }
            add("studySessions", sessionsArr)

            // Topic Progress
            val progressArr = JsonArray()
            unsyncedProgress.forEach { prog ->
                progressArr.add(JsonObject().apply {
                    addProperty("clientId", prog.clientId)
                    addProperty("classLevel", prog.classLevel)
                    addProperty("subject", prog.subject)
                    addProperty("chapter", prog.chapter)
                    addProperty("topic", prog.topic)
                    addProperty("questionsAsked", prog.questionsAsked)
                    addProperty("quizzesAttempted", prog.quizzesAttempted)
                    addProperty("averageScore", prog.averageScore)
                    addProperty("studyTimeMinutes", prog.studyTimeMinutes)
                    addProperty("masteryLevel", prog.masteryLevel)
                    addProperty("isWeakArea", prog.isWeakArea)
                    addProperty("lastStudiedAt", prog.lastStudiedAt)
                })
            }
            add("topicProgress", progressArr)

            // Quiz Attempts
            val attemptsArr = JsonArray()
            unsyncedAttempts.forEach { att ->
                attemptsArr.add(JsonObject().apply {
                    addProperty("clientId", att.clientId)
                    addProperty("quizTitle", att.quizTitle)
                    addProperty("score", att.score)
                    addProperty("totalQuestions", att.totalQuestions)
                    addProperty("correctAnswers", att.correctAnswers)
                    addProperty("percentage", att.percentage)
                    addProperty("completedAt", att.completedAt)
                })
            }
            add("quizAttempts", attemptsArr)

            // Scans
            val scansArr = JsonArray()
            unsyncedScans.forEach { scan ->
                scansArr.add(JsonObject().apply {
                    addProperty("clientId", scan.clientId)
                    addProperty("text", scan.text)
                    addProperty("timestamp", scan.timestamp)
                })
            }
            add("scans", scansArr)
        }

        val response = RetrofitClient.apiService.syncPush(pushBody)
        if (response.isSuccessful && response.body()?.get("success")?.asBoolean == true) {
            val acceptedObj = response.body()?.getAsJsonObject("accepted")
            if (acceptedObj != null) {
                // Mark accepted clientIds as synced
                parseAndMarkSynced(acceptedObj.getAsJsonArray("chatMessages")) { ids -> db.chatMessageDao().markAsSynced(ids) }
                parseAndMarkSynced(acceptedObj.getAsJsonArray("studySessions")) { ids -> db.studySessionDao().markAsSynced(ids) }
                parseAndMarkSynced(acceptedObj.getAsJsonArray("topicProgress")) { ids -> db.topicProgressDao().markAsSynced(ids) }
                parseAndMarkSynced(acceptedObj.getAsJsonArray("quizAttempts")) { ids -> db.quizAttemptDao().markAsSynced(ids) }
                parseAndMarkSynced(acceptedObj.getAsJsonArray("scans")) { ids -> db.scanRecordDao().markAsSynced(ids) }
            }
            Log.i(TAG, "Push sync to MongoDB succeeded.")
        } else {
            Log.w(TAG, "Push sync HTTP failed ${response.code()}")
        }
    }

    private suspend fun parseAndMarkSynced(
        arr: JsonArray?,
        updateCall: suspend (List<String>) -> Unit
    ) {
        if (arr != null && arr.size() > 0) {
            val list = mutableListOf<String>()
            arr.forEach { list.add(it.asString) }
            updateCall(list)
        }
    }

    private suspend fun pullServerData() {
        val lastSync = userPrefs.lastSyncTimeFlow.first()
        val sinceIso = java.time.Instant.ofEpochMilli(lastSync).toString()

        val response = RetrofitClient.apiService.syncPull(sinceIso)
        if (response.isSuccessful && response.body()?.get("success")?.asBoolean == true) {
            val body = response.body()!!

            // Cache Quizzes
            val quizzesArr = body.getAsJsonArray("quizzes")
            if (quizzesArr != null && quizzesArr.size() > 0) {
                val cachedQuizzes = mutableListOf<CachedQuizEntity>()
                quizzesArr.forEach { item ->
                    val obj = item.asJsonObject
                    cachedQuizzes.add(
                        CachedQuizEntity(
                            quizId = obj.get("_id")?.asString ?: "",
                            title = obj.get("title")?.asString ?: "Quiz",
                            subject = obj.get("subject")?.asString ?: "General",
                            classLevel = obj.get("classLevel")?.asInt ?: 10,
                            chapter = obj.get("chapter")?.asString ?: "General",
                            questionsJson = obj.get("questions")?.toString() ?: "[]"
                        )
                    )
                }
                db.cachedQuizDao().insertAll(cachedQuizzes)
            }

            // Cache Opportunities
            val oppsArr = body.getAsJsonArray("opportunities")
            if (oppsArr != null && oppsArr.size() > 0) {
                val cachedOpps = mutableListOf<CachedOpportunityEntity>()
                oppsArr.forEach { item ->
                    val obj = item.asJsonObject
                    cachedOpps.add(
                        CachedOpportunityEntity(
                            opportunityId = obj.get("_id")?.asString ?: "",
                            title = obj.get("title")?.asString ?: "Scholarship",
                            description = obj.get("description")?.asString ?: "",
                            type = obj.get("type")?.asString ?: "scholarship",
                            deadline = obj.get("deadline")?.asString ?: "",
                            applyUrl = obj.get("applyUrl")?.asString ?: ""
                        )
                    )
                }
                db.cachedOpportunityDao().insertAll(cachedOpps)
            }

            // Update Last Sync Time
            userPrefs.saveLastSyncTime(System.currentTimeMillis())
            Log.i(TAG, "Pull sync completed and cached.")
        }
    }

    companion object {
        private const val TAG = "SyncManager"
    }
}
