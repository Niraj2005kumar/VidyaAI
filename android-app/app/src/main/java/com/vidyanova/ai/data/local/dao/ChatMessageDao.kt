package com.vidyanova.ai.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.vidyanova.ai.data.local.entities.ChatMessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatMessageDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: ChatMessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(messages: List<ChatMessageEntity>)

    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getAllMessagesFlow(): Flow<List<ChatMessageEntity>>

    @Query("SELECT * FROM chat_messages WHERE synced = 0 LIMIT :limit")
    suspend fun getUnsyncedMessages(limit: Int = 100): List<ChatMessageEntity>

    @Query("UPDATE chat_messages SET synced = 1 WHERE clientId IN (:clientIds)")
    suspend fun markAsSynced(clientIds: List<String>)

    @Query("DELETE FROM chat_messages")
    suspend fun deleteAll()
}
