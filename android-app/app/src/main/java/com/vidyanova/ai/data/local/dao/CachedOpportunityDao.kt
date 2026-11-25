package com.vidyanova.ai.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.vidyanova.ai.data.local.entities.CachedOpportunityEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CachedOpportunityDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(opportunities: List<CachedOpportunityEntity>)

    @Query("SELECT * FROM cached_opportunities ORDER BY title ASC")
    fun getAllOpportunitiesFlow(): Flow<List<CachedOpportunityEntity>>

    @Query("DELETE FROM cached_opportunities")
    suspend fun deleteAll()
}
