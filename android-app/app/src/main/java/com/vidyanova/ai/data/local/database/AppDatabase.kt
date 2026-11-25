package com.vidyanova.ai.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.vidyanova.ai.data.local.dao.CachedOpportunityDao
import com.vidyanova.ai.data.local.dao.CachedQuizDao
import com.vidyanova.ai.data.local.dao.ChatMessageDao
import com.vidyanova.ai.data.local.dao.QuizAttemptDao
import com.vidyanova.ai.data.local.dao.ScanRecordDao
import com.vidyanova.ai.data.local.dao.StudySessionDao
import com.vidyanova.ai.data.local.dao.TopicProgressDao
import com.vidyanova.ai.data.local.entities.CachedOpportunityEntity
import com.vidyanova.ai.data.local.entities.CachedQuizEntity
import com.vidyanova.ai.data.local.entities.ChatMessageEntity
import com.vidyanova.ai.data.local.entities.QuizAttemptEntity
import com.vidyanova.ai.data.local.entities.ScanRecordEntity
import com.vidyanova.ai.data.local.entities.StudySessionEntity
import com.vidyanova.ai.data.local.entities.TopicProgressEntity

@Database(
    entities = [
        ChatMessageEntity::class,
        StudySessionEntity::class,
        TopicProgressEntity::class,
        QuizAttemptEntity::class,
        ScanRecordEntity::class,
        CachedQuizEntity::class,
        CachedOpportunityEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun chatMessageDao(): ChatMessageDao
    abstract fun studySessionDao(): StudySessionDao
    abstract fun topicProgressDao(): TopicProgressDao
    abstract fun quizAttemptDao(): QuizAttemptDao
    abstract fun scanRecordDao(): ScanRecordDao
    abstract fun cachedQuizDao(): CachedQuizDao
    abstract fun cachedOpportunityDao(): CachedOpportunityDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "vidyanova_local_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
