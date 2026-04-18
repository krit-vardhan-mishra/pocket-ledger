package com.just_for_fun.pocketledger.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.just_for_fun.pocketledger.data.model.Transaction
import com.just_for_fun.pocketledger.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val repository: TransactionRepository
) : ViewModel() {

    private val calendar = Calendar.getInstance()
    private val _selectedMonth = MutableStateFlow((calendar.get(Calendar.MONTH) + 1).toString().padStart(2, '0'))
    private val _selectedYear = MutableStateFlow(calendar.get(Calendar.YEAR).toString())

    val transactions: StateFlow<List<Transaction>> = _selectedMonth.flatMapLatest { month ->
        repository.getTransactionsByMonth(month, _selectedYear.value)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun updateFilter(month: String, year: String) {
        _selectedMonth.value = month
        _selectedYear.value = year
    }
}
