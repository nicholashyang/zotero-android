package org.zotero.android.appupdate

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import org.zotero.android.BuildConfig
import java.util.concurrent.TimeUnit

@EntryPoint
@InstallIn(SingletonComponent::class)
interface UpdateEntryPoint {
    fun updateRepository(): UpdateRepository
}

internal fun Context.updateRepository(): UpdateRepository =
    EntryPointAccessors.fromApplication(this, UpdateEntryPoint::class.java).updateRepository()

class UpdateCheckWorker(context: Context, parameters: WorkerParameters) : CoroutineWorker(context, parameters) {
    override suspend fun doWork(): Result {
        if (!BuildConfig.SELF_UPDATE_ENABLED) return Result.success()
        return if (applicationContext.updateRepository().check(automatic = true)) Result.success() else Result.retry()
    }
}

class UpdateReconcileWorker(context: Context, parameters: WorkerParameters) : CoroutineWorker(context, parameters) {
    override suspend fun doWork(): Result {
        if (!BuildConfig.SELF_UPDATE_ENABLED) return Result.success()
        val repository = applicationContext.updateRepository()
        repository.refresh()
        return when (repository.state.value.status) {
            UpdateStatus.DOWNLOADING, UpdateStatus.WAITING_FOR_NETWORK -> Result.retry()
            else -> Result.success()
        }
    }
}

object UpdateScheduler {
    private const val CHECK = "app-update-check"
    private const val RECONCILE = "app-update-download"

    fun schedule(context: Context, enabled: Boolean) {
        if (!BuildConfig.SELF_UPDATE_ENABLED) return
        val manager = WorkManager.getInstance(context)
        if (enabled) {
            manager.enqueueUniquePeriodicWork(CHECK, ExistingPeriodicWorkPolicy.KEEP,
                PeriodicWorkRequestBuilder<UpdateCheckWorker>(24, TimeUnit.HOURS)
                    .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                    .build())
        } else {
            manager.cancelUniqueWork(CHECK)
        }
    }

    fun reconcileLater(context: Context) {
        if (!BuildConfig.SELF_UPDATE_ENABLED) return
        WorkManager.getInstance(context).enqueueUniqueWork(RECONCILE, ExistingWorkPolicy.REPLACE,
            OneTimeWorkRequestBuilder<UpdateReconcileWorker>().build())
    }
}

/** Broadcasts only wake reconciliation; persisted download IDs are the source of truth. */
class UpdateDownloadReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == android.app.DownloadManager.ACTION_DOWNLOAD_COMPLETE ||
            intent.action == Intent.ACTION_MY_PACKAGE_REPLACED) UpdateScheduler.reconcileLater(context)
    }
}
