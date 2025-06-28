package com.srabbijan.sheetdb.worker

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.srabbijan.sheetdb.data.local.AppDatabase
import com.srabbijan.sheetdb.repository.DataRepository
import com.srabbijan.sheetdb.service.GoogleSheetsService
import java.util.concurrent.TimeUnit

class SyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val database = AppDatabase.getDatabase(applicationContext)
            val sheetsService = GoogleSheetsService(applicationContext)
            val repository = DataRepository(database.dataItemDao(), sheetsService, applicationContext)

            val success = repository.syncToSheet()
            if (success) Result.success() else Result.retry()
        } catch (e: Exception) {
            Log.e("SyncWorker", "Sync failed", e)
            Result.failure()
        }
    }
}

// Schedule periodic sync
class SyncScheduler(private val context: Context) {
    fun schedulePeriodicSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(15, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "sync_work",
            ExistingPeriodicWorkPolicy.KEEP,
            syncRequest
        )
    }
}