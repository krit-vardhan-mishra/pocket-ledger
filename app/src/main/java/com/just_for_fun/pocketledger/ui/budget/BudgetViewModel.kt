package com.just_for_fun.pocketledger.ui.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.just_for_fun.pocketledger.data.model.AppSettings
import com.just_for_fun.pocketledger.data.model.AppThemeMode
import com.just_for_fun.pocketledger.data.model.enums.Category
import com.just_for_fun.pocketledger.data.repository.BudgetRepository
import com.just_for_fun.pocketledger.data.repository.SettingsRepository
import com.just_for_fun.pocketledger.di.AppCoroutineDispatchers
import com.just_for_fun.pocketledger.worker.DailyBudgetWorkScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BudgetViewModel @Inject constructor(
    private val budgetRepository: BudgetRepository,
    private val settingsRepository: SettingsRepository,
    private val dailyBudgetWorkScheduler: DailyBudgetWorkScheduler,
    private val dispatchers: AppCoroutineDispatchers = AppCoroutineDispatchers()
) : ViewModel() {

    val budgets = budgetRepository.getAllBudgets()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val settings = settingsRepository.settings
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AppSettings()
        )

    fun setBudget(category: Category, limit: Double) {
        viewModelScope.launch(dispatchers.io) {
            budgetRepository.setBudget(category, limit)
        }
    }

    fun deleteBudget(category: Category) {
        viewModelScope.launch(dispatchers.io) {
            budgetRepository.deleteBudget(category)
        }
    }

    fun updateNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch(dispatchers.io) {
            settingsRepository.updateNotificationsEnabled(enabled)
            dailyBudgetWorkScheduler.syncWithCurrentSettings()
        }
    }

    fun updateNotificationTime(hour: Int, minute: Int) {
        viewModelScope.launch(dispatchers.io) {
            settingsRepository.updateNotificationTime(hour, minute)
            dailyBudgetWorkScheduler.syncWithCurrentSettings()
        }
    }

    fun updateExceedAlertsEnabled(enabled: Boolean) {
        viewModelScope.launch(dispatchers.io) {
            settingsRepository.updateExceedAlertsEnabled(enabled)
            dailyBudgetWorkScheduler.syncWithCurrentSettings()
        }
    }

    fun updateUseTempData(enabled: Boolean) {
        viewModelScope.launch(dispatchers.io) {
            settingsRepository.updateUseTempData(enabled)
        }
    }

    fun updateThemeMode(mode: AppThemeMode) {
        viewModelScope.launch(dispatchers.io) {
            settingsRepository.updateThemeMode(mode)
        }
    }
}
