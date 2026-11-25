package com.vidyanova.ai.data.local.sync

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import java.util.concurrent.TimeUnit

class SyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        Log.i(TAG, "SyncWorker started execution...")
        val syncManager = SyncManager(applicationContext)
        val success = syncManager.performFullSync()

        return if (success) {
            Log.i(TAG, "SyncWorker finished successfully.")
            Result.success()
        } else {
            Log.w(TAG, "SyncWorker failed. Scheduling retry...")
            Result.retry()
        }
    }

    companion object {
        private const val TAG = "SyncWorker"
        private const val ONE_TIME_WORK_NAME = "VidyaAiOneTimeSync"
        private const val PERIODIC_WORK_NAME = "VidyaAiPeriodicSync"

        fun scheduleOneTimeSync(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = OneTimeWorkRequestBuilder<SyncWorker>()
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                ONE_TIME_WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                request
            )
            Log.i(TAG, "Enqueued OneTimeSync WorkManager request.")
        }

        fun schedulePeriodicSync(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = PeriodicWorkRequestBuilder<SyncWorker>(6, TimeUnit.HOURS)
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                PERIODIC_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
            Log.i(TAG, "Enqueued 6-hour PeriodicSync WorkManager request.")
        }
    }
}
