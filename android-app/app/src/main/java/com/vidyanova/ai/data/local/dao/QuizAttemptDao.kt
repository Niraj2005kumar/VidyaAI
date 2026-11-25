package com.vidyanova.ai.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.vidyanova.ai.data.local.entities.QuizAttemptEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QuizAttemptDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(attempt: QuizAttemptEntity)

    @Query("SELECT * FROM quiz_attempts ORDER BY completedAt DESC")
    fun getAllAttemptsFlow(): Flow<List<QuizAttemptEntity>>

    @Query("SELECT AVG(percentage) FROM quiz_attempts")
    fun getAverageQuizScoreFlow(): Flow<Double?>

    @Query("SELECT * FROM quiz_attempts WHERE synced = 0 LIMIT :limit")
    suspend fun getUnsyncedAttempts(limit: Int = 100): List<QuizAttemptEntity>

    @Query("UPDATE quiz_attempts SET synced = 1 WHERE clientId IN (:clientIds)")
    suspend fun markAsSynced(clientIds: List<String>)

    @Query("DELETE FROM quiz_attempts")
    suspend fun deleteAll()
}
