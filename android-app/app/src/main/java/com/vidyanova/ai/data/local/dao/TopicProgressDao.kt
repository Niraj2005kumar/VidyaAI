package com.vidyanova.ai.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.vidyanova.ai.data.local.entities.TopicProgressEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TopicProgressDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(progress: TopicProgressEntity)

    @Query("SELECT * FROM topic_progress ORDER BY lastStudiedAt DESC")
    fun getAllProgressFlow(): Flow<List<TopicProgressEntity>>

    @Query("SELECT COUNT(DISTINCT topic) FROM topic_progress WHERE questionsAsked > 0 OR quizzesAttempted > 0")
    fun getTopicsLearnedCountFlow(): Flow<Int>

    @Query("SELECT * FROM topic_progress WHERE synced = 0 LIMIT :limit")
    suspend fun getUnsyncedProgress(limit: Int = 100): List<TopicProgressEntity>

    @Query("UPDATE topic_progress SET synced = 1 WHERE clientId IN (:clientIds)")
    suspend fun markAsSynced(clientIds: List<String>)

    @Query("DELETE FROM topic_progress")
    suspend fun deleteAll()
}
