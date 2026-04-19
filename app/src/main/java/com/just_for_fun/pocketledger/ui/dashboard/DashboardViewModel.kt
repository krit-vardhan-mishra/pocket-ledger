package com.just_for_fun.pocketledger.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.just_for_fun.pocketledger.data.model.Transaction
import com.just_for_fun.pocketledger.data.model.enums.Category
import com.just_for_fun.pocketledger.data.model.enums.TransactionType
import com.just_for_fun.pocketledger.data.repository.BudgetRepository
import com.just_for_fun.pocketledger.data.repository.TransactionRepository
import com.just_for_fun.pocketledger.di.AppCoroutineDispatchers
import com.just_for_fun.pocketledger.domain.usecase.GetMonthlySummaryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val budgetRepository: BudgetRepository,
    private val getMonthlySummaryUseCase: GetMonthlySummaryUseCase,
    private val dispatchers: AppCoroutineDispatchers = AppCoroutineDispatchers()
) : ViewModel() {

    private val calendar = Calendar.getInstance()
    private val currentMonth = (calendar.get(Calendar.MONTH) + 1).toString().padStart(2, '0')
    private val currentYear = calendar.get(Calendar.YEAR).toString()

    private val _selectedCategory = MutableStateFlow<Category?>(null)
    val selectedCategory: StateFlow<Category?> = _selectedCategory.asStateFlow()

    val monthlySummary = getMonthlySummaryUseCase(currentMonth, currentYear)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    private val startOfToday = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    private val endOfToday = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 23)
        set(Calendar.MINUTE, 59)
        set(Calendar.SECOND, 59)
        set(Calendar.MILLISECOND, 999)
    }.timeInMillis

    val todayTransactions = transactionRepository.getTransactionsByDateRange(startOfToday, endOfToday)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val filteredTransactions = combine(todayTransactions, selectedCategory) { transactions, category ->
        if (category == null) {
            transactions
        } else {
            transactions.filter { it.category == category }
        }
    }
        .flowOn(dispatchers.default)
        .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val exceededCategories = budgetRepository.getExceededCategories(currentMonth, currentYear)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun filterByCategory(category: Category?) {
        _selectedCategory.value = category
    }

    fun saveTransaction(
        id: Long?,
        amount: Double,
        type: TransactionType,
        category: Category,
        note: String,
        date: Long
    ) {
        viewModelScope.launch(dispatchers.io) {
            val transaction = Transaction(
                id = id ?: 0L,
                amount = amount,
                type = type,
                category = category,
                note = note,
                date = date
            )

            if (id == null) {
                transactionRepository.insertTransaction(transaction)
            } else {
                transactionRepository.updateTransaction(transaction)
            }
        }
    }

    fun deleteTransaction(id: Long) {
        viewModelScope.launch(dispatchers.io) {
            transactionRepository.deleteTransaction(id)
        }
    }
}
