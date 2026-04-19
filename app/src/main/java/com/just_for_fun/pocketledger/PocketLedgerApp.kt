package com.just_for_fun.pocketledger

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import com.just_for_fun.pocketledger.di.AppCoroutineDispatchers
import com.just_for_fun.pocketledger.worker.DailyBudgetWorkScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class PocketLedgerApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var dailyBudgetWorkScheduler: DailyBudgetWorkScheduler

    @Inject
    lateinit var dispatchers: AppCoroutineDispatchers

    private val appScope by lazy { CoroutineScope(SupervisorJob() + dispatchers.default) }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        appScope.launch {
            dailyBudgetWorkScheduler.syncWithCurrentSettings()
        }
    }
}
