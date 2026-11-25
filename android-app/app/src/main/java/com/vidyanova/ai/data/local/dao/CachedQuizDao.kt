package com.vidyanova.ai.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.vidyanova.ai.data.local.entities.CachedQuizEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CachedQuizDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(quizzes: List<CachedQuizEntity>)

    @Query("SELECT * FROM cached_quizzes ORDER BY title ASC")
    fun getAllQuizzesFlow(): Flow<List<CachedQuizEntity>>

    @Query("SELECT * FROM cached_quizzes WHERE quizId = :quizId LIMIT 1")
    suspend fun getQuizById(quizId: String): CachedQuizEntity?

    @Query("DELETE FROM cached_quizzes")
    suspend fun deleteAll()
}
