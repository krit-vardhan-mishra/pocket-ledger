package com.just_for_fun.pocketledger

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.ExistingPeriodicWorkPolicy
import com.just_for_fun.pocketledger.worker.DailyBudgetWorker
import java.util.concurrent.TimeUnit

@HiltAndroidApp
class PocketLedgerApp : Application() {
    override fun onCreate() {
        super.onCreate()
        scheduleDailyWorker()
    }

    private fun scheduleDailyWorker() {
        val workRequest = PeriodicWorkRequestBuilder<DailyBudgetWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(1, TimeUnit.HOURS)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "DailyBudgetCheck",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
}
