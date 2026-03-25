package com.lweiss01.paydirt.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.lweiss01.paydirt.domain.usecase.RefreshLinkedAccountsUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

/**
 * BackgroundRefreshWorker — silently refreshes Plaid balances.
 *
 * Scheduled: weekly, requires network.
 * Silent: no notification unless re-auth is needed.
 * On re-auth needed: posts a single persistent notification.
 *
 * This is how "set it and forget it" works in practice —
 * balances stay current without the user doing anything.
 */
@HiltWorker
class BackgroundRefreshWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val refreshLinkedAccountsUseCase: RefreshLinkedAccountsUseCase,
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val result = refreshLinkedAccountsUseCase(forceRefresh = false)

            when (result) {
                is RefreshLinkedAccountsUseCase.RefreshResult.Success -> {
                    if (result.reauthRequired) {
                        // Post a gentle reauth notification
                        showReauthNotification()
                    }
                    Result.success()
                }
                is RefreshLinkedAccountsUseCase.RefreshResult.Failure -> {
                    // Retry up to 3 times with exponential backoff
                    if (runAttemptCount < 3) Result.retry() else Result.failure()
                }
            }
        } catch (e: Exception) {
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }

    private fun showReauthNotification() {
        // In production: use NotificationCompat to post a single nudge
        // "One of your cards needs a quick re-login to stay current."
        // Tapping opens the app directly to the re-auth screen.
    }

    companion object {
        const val WORK_NAME = "paydirt_balance_refresh"

        /**
         * Schedule weekly background refresh.
         * Call this from Application.onCreate() or after first link.
         */
        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = PeriodicWorkRequestBuilder<BackgroundRefreshWorker>(
                repeatInterval = 7,
                repeatIntervalTimeUnit = TimeUnit.DAYS,
            )
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    WorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS,
                )
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,  // Don't reset timer if already scheduled
                request,
            )
        }

        /**
         * Trigger an immediate refresh (e.g. on app open after 7+ days).
         */
        fun runNow(context: Context) {
            val request = OneTimeWorkRequestBuilder<BackgroundRefreshWorker>()
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .build()

            WorkManager.getInstance(context).enqueue(request)
        }
    }
}
