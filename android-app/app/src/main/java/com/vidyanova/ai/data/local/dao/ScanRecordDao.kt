package com.vidyanova.ai.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.vidyanova.ai.data.local.entities.ScanRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ScanRecordDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(scan: ScanRecordEntity)

    @Query("SELECT * FROM scan_records ORDER BY timestamp DESC")
    fun getAllScansFlow(): Flow<List<ScanRecordEntity>>

    @Query("SELECT * FROM scan_records WHERE synced = 0 LIMIT :limit")
    suspend fun getUnsyncedScans(limit: Int = 100): List<ScanRecordEntity>

    @Query("UPDATE scan_records SET synced = 1 WHERE clientId IN (:clientIds)")
    suspend fun markAsSynced(clientIds: List<String>)

    @Query("DELETE FROM scan_records")
    suspend fun deleteAll()
}
