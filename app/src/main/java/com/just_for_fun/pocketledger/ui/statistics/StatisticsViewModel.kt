package com.just_for_fun.pocketledger.ui.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.just_for_fun.pocketledger.data.model.enums.TransactionType
import com.just_for_fun.pocketledger.domain.usecase.GetCategoryBreakdownUseCase
import com.just_for_fun.pocketledger.domain.usecase.GetMonthlySummaryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val getMonthlySummaryUseCase: GetMonthlySummaryUseCase,
    private val getCategoryBreakdownUseCase: GetCategoryBreakdownUseCase
) : ViewModel() {

    private val calendar = Calendar.getInstance()
    private val _selectedMonth = MutableStateFlow((calendar.get(Calendar.MONTH) + 1).toString().padStart(2, '0'))
    private val _selectedYear = MutableStateFlow(calendar.get(Calendar.YEAR).toString())

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

    fun nextPage() {
        calendar.add(Calendar.MONTH, 1)
        updateDate()
    }

    fun previousPage() {
        calendar.add(Calendar.MONTH, -1)
        updateDate()
    }

    private fun updateDate() {
        _selectedMonth.value = (calendar.get(Calendar.MONTH) + 1).toString().padStart(2, '0')
        _selectedYear.value = calendar.get(Calendar.YEAR).toString()
    }

    fun getFormattedDate(): String {
        val monthNames = arrayOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")
        return "${monthNames[calendar.get(Calendar.MONTH)]} ${calendar.get(Calendar.YEAR)}"
    }
}
