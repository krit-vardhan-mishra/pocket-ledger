package com.just_for_fun.pocketledger.worker

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.just_for_fun.pocketledger.R
import com.just_for_fun.pocketledger.data.repository.BudgetRepository
import com.just_for_fun.pocketledger.data.repository.SettingsRepository
import com.just_for_fun.pocketledger.data.repository.TransactionRepository
import com.just_for_fun.pocketledger.data.model.enums.displayName
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.Calendar

@HiltWorker
class DailyBudgetWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val transactionRepository: TransactionRepository,
    private val budgetRepository: BudgetRepository,
    private val settingsRepository: SettingsRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val settings = settingsRepository.getSettingsSnapshot()
        if (!settings.notificationsEnabled) {
            return Result.success()
        }

        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfDay = calendar.timeInMillis

        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val endOfDay = calendar.timeInMillis

        val totalSpentToday = transactionRepository.getTodayExpenseTotal(startOfDay, endOfDay)

        val month = (calendar.get(Calendar.MONTH) + 1).toString().padStart(2, '0')
        val year = calendar.get(Calendar.YEAR).toString()
        val exceeded = if (settings.exceedAlertsEnabled) {
            budgetRepository.getExceededCategoryDetailsSnapshot(month, year)
        } else {
            emptyList()
        }

        ensureNotificationChannel()
        postNotification(totalSpentToday, exceeded.firstOrNull()?.let {
            "${it.category.displayName()} budget exceeded by ₹${"%.0f".format(it.exceededBy)}"
        })

        return Result.success()
    }

    private fun ensureNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return
        }

        val channel = NotificationChannel(
            CHANNEL_ID,
            "Daily Budget Summary",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Daily spending summaries and budget exceed alerts"
        }

        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    private fun postNotification(totalSpentToday: Double, exceedMessage: String?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val content = buildString {
            append("Today you spent ₹${"%.0f".format(totalSpentToday)}")
            if (!exceedMessage.isNullOrBlank()) {
                append(". ")
                append(exceedMessage)
                append('.')
            }
        }

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("PocketLedger Daily Summary")
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(applicationContext).notify(NOTIFICATION_ID, notification)
    }

    private companion object {
        const val CHANNEL_ID = "pocketledger_daily_budget"
        const val NOTIFICATION_ID = 1001
    }
}