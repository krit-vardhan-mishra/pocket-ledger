package com.just_for_fun.pocketledger.ui.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.just_for_fun.pocketledger.data.model.Budget
import com.just_for_fun.pocketledger.data.model.AppSettings
import com.just_for_fun.pocketledger.data.model.enums.Category
import com.just_for_fun.pocketledger.data.repository.BudgetRepository
import com.just_for_fun.pocketledger.data.repository.SettingsRepository
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
    private val dailyBudgetWorkScheduler: DailyBudgetWorkScheduler
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
        viewModelScope.launch {
            budgetRepository.setBudget(category, limit)
        }
    }

    fun deleteBudget(category: Category) {
        viewModelScope.launch {
            budgetRepository.deleteBudget(category)
        }
    }

    fun updateNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateNotificationsEnabled(enabled)
            dailyBudgetWorkScheduler.syncWithCurrentSettings()
        }
    }

    fun updateNotificationTime(hour: Int, minute: Int) {
        viewModelScope.launch {
            settingsRepository.updateNotificationTime(hour, minute)
            dailyBudgetWorkScheduler.syncWithCurrentSettings()
        }
    }

    fun updateExceedAlertsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateExceedAlertsEnabled(enabled)
            dailyBudgetWorkScheduler.syncWithCurrentSettings()
        }
    }
}
