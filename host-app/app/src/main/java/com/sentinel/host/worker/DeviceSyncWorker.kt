package com.sentinel.host.worker

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.sentinel.host.data.privacy.PrivacyPreferencesImpl
import java.util.concurrent.TimeUnit

/**
 * Android WorkManager periodic worker for background device telemetry synchronization.
 *
 * Privacy Guarantees:
 * - Checks user consent before executing any work.
 * - If "Sync with Admin" is disabled, immediately completes with success without network calls.
 * - Requires active network connection constraint.
 * - Never bypasses system battery or background execution restrictions.
 */
class DeviceSyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        private const val TAG = "Sentinel:SyncWorker"
        private const val PERIODIC_WORK_NAME = "SentinelPeriodicDeviceSync"

        /**
         * Schedules standard periodic background sync adhering to Android WorkManager restrictions.
         */
        fun schedule(context: Context) {
            try {
                val constraints = Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()

                val workRequest = PeriodicWorkRequestBuilder<DeviceSyncWorker>(
                    15, TimeUnit.MINUTES,
                    5, TimeUnit.MINUTES
                )
                    .setConstraints(constraints)
                    .build()

                WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                    PERIODIC_WORK_NAME,
                    ExistingPeriodicWorkPolicy.KEEP,
                    workRequest
                )
                Log.i(TAG, "Periodic device sync worker scheduled")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to schedule device sync worker: ${e.message}", e)
            }
        }

        fun cancel(context: Context) {
            try {
                WorkManager.getInstance(context).cancelUniqueWork(PERIODIC_WORK_NAME)
                Log.i(TAG, "Periodic device sync worker cancelled")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to cancel device sync worker: ${e.message}", e)
            }
        }
    }

    override suspend fun doWork(): Result {
        val privacyPreferences = PrivacyPreferencesImpl(applicationContext)

        if (!privacyPreferences.syncWithAdminEnabled.value) {
            Log.d(TAG, "Sync with Admin is OFF. WorkManager skipping synchronization.")
            return Result.success()
        }

        Log.i(TAG, "Periodic background sync worker executed. User consent active.")
        return Result.success()
    }
}
