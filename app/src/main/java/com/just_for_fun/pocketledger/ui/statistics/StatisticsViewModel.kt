package com.just_for_fun.pocketledger.ui.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.just_for_fun.pocketledger.data.model.CategoryTotal
import com.just_for_fun.pocketledger.data.model.DailyTotal
import com.just_for_fun.pocketledger.data.model.enums.TransactionType
import com.just_for_fun.pocketledger.data.repository.TransactionRepository
import com.just_for_fun.pocketledger.domain.usecase.GetCategoryBreakdownUseCase
import com.just_for_fun.pocketledger.domain.usecase.GetMonthlySummaryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val getMonthlySummaryUseCase: GetMonthlySummaryUseCase,
    private val getCategoryBreakdownUseCase: GetCategoryBreakdownUseCase
) : ViewModel() {

    private val now = Calendar.getInstance()
    private val selectedCalendar = Calendar.getInstance()
    private val _selectedMonth = MutableStateFlow((selectedCalendar.get(Calendar.MONTH) + 1).toString().padStart(2, '0'))
    private val _selectedYear = MutableStateFlow(selectedCalendar.get(Calendar.YEAR).toString())

    val canNavigateNext: StateFlow<Boolean> = combine(_selectedMonth, _selectedYear) { month, year ->
            val selectedMonthIndex = month.toIntOrNull() ?: 1
            val selectedYearInt = year.toIntOrNull() ?: now.get(Calendar.YEAR)
            selectedYearInt < now.get(Calendar.YEAR) ||
                (selectedYearInt == now.get(Calendar.YEAR) && selectedMonthIndex < now.get(Calendar.MONTH) + 1)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    val monthlySummary = _selectedMonth.flatMapLatest { month ->
        getMonthlySummaryUseCase(month, _selectedYear.value)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val categoryBreakdown = _selectedMonth.flatMapLatest { month ->
        getCategoryBreakdownUseCase(month, _selectedYear.value, TransactionType.EXPENSE)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val dailyTotals: StateFlow<List<DailyTotal>> = _selectedMonth.flatMapLatest { month ->
        transactionRepository.getDailyTotals(month, _selectedYear.value, TransactionType.EXPENSE)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val highestSpendDay: StateFlow<DailyTotal?> = dailyTotals
        .map { totals -> totals.maxByOrNull { it.amount } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val mostSpentCategory: StateFlow<CategoryTotal?> = categoryBreakdown
        .map { totals -> totals.maxByOrNull { it.amount } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    fun nextPage() {
        if (!canNavigateNext.value) {
            return
        }
        selectedCalendar.add(Calendar.MONTH, 1)
        syncSelectedDate()
    }

    fun previousPage() {
        selectedCalendar.add(Calendar.MONTH, -1)
        syncSelectedDate()
    }

    fun getDaysInSelectedMonth(): Int {
        return selectedCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    }

    fun getFormattedDate(): String {
        val monthNames = arrayOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")
        return "${monthNames[selectedCalendar.get(Calendar.MONTH)]} ${selectedCalendar.get(Calendar.YEAR)}"
    }

    private fun syncSelectedDate() {
        _selectedMonth.value = (selectedCalendar.get(Calendar.MONTH) + 1).toString().padStart(2, '0')
        _selectedYear.value = selectedCalendar.get(Calendar.YEAR).toString()
    }
}
