package com.just_for_fun.pocketledger.worker

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.just_for_fun.pocketledger.data.model.AppSettings
import com.just_for_fun.pocketledger.data.repository.SettingsRepository
import com.just_for_fun.pocketledger.di.AppCoroutineDispatchers
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DailyBudgetWorkScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsRepository: SettingsRepository,
    private val dispatchers: AppCoroutineDispatchers = AppCoroutineDispatchers()
) {

    suspend fun syncWithCurrentSettings() {
        schedule(settingsRepository.getSettingsSnapshot())
    }

    suspend fun schedule(settings: AppSettings) = withContext(dispatchers.default) {
        val workManager = WorkManager.getInstance(context)
        if (!settings.notificationsEnabled) {
            workManager.cancelUniqueWork(WORK_NAME)
            return@withContext
        }

        val now = Calendar.getInstance()
        val trigger = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, settings.notificationHour)
            set(Calendar.MINUTE, settings.notificationMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (before(now)) {
                add(Calendar.DAY_OF_MONTH, 1)
            }
        }

        val initialDelayMillis = (trigger.timeInMillis - now.timeInMillis).coerceAtLeast(0L)

        val request = PeriodicWorkRequestBuilder<DailyBudgetWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(initialDelayMillis, TimeUnit.MILLISECONDS)
            .build()

        workManager.enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    companion object {
        const val WORK_NAME = "DailyBudgetCheck"
    }
}
