package com.just_for_fun.pocketledger.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.just_for_fun.pocketledger.data.repository.TransactionRepository
import com.just_for_fun.pocketledger.domain.usecase.GetMonthlySummaryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val getMonthlySummaryUseCase: GetMonthlySummaryUseCase
) : ViewModel() {

    private val calendar = Calendar.getInstance()
    private val currentMonth = (calendar.get(Calendar.MONTH) + 1).toString().padStart(2, '0')
    private val currentYear = calendar.get(Calendar.YEAR).toString()

    val monthlySummary = getMonthlySummaryUseCase(currentMonth, currentYear)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val recentTransactions = transactionRepository.getRecentTransactions()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}
