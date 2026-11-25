package com.vidyanova.ai.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.vidyanova.ai.data.local.entities.StudySessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StudySessionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: StudySessionEntity)

    @Query("SELECT * FROM study_sessions ORDER BY startTime DESC")
    fun getAllSessionsFlow(): Flow<List<StudySessionEntity>>

    @Query("SELECT SUM(durationMinutes) FROM study_sessions")
    fun getTotalStudyTimeMinutesFlow(): Flow<Int?>

    @Query("SELECT * FROM study_sessions WHERE synced = 0 LIMIT :limit")
    suspend fun getUnsyncedSessions(limit: Int = 100): List<StudySessionEntity>

    @Query("UPDATE study_sessions SET synced = 1 WHERE clientId IN (:clientIds)")
    suspend fun markAsSynced(clientIds: List<String>)

    @Query("DELETE FROM study_sessions")
    suspend fun deleteAll()
}
