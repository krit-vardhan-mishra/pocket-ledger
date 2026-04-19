package com.just_for_fun.pocketledger.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.just_for_fun.pocketledger.data.model.Transaction
import com.just_for_fun.pocketledger.data.model.enums.Category
import com.just_for_fun.pocketledger.data.model.enums.TransactionType
import com.just_for_fun.pocketledger.data.repository.TransactionRepository
import com.just_for_fun.pocketledger.di.AppCoroutineDispatchers
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

enum class HistoryTypeFilter {
    ALL,
    INCOME,
    EXPENSE
}

data class HistoryDayGroup(
    val dayStartMillis: Long,
    val transactions: List<Transaction>
)

private data class HistoryFilters(
    val query: String,
    val startMillis: Long?,
    val endMillis: Long?,
    val typeFilter: HistoryTypeFilter,
    val categories: Set<Category>
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val repository: TransactionRepository,
    private val dispatchers: AppCoroutineDispatchers = AppCoroutineDispatchers()
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _startDateMillis = MutableStateFlow<Long?>(null)
    val startDateMillis: StateFlow<Long?> = _startDateMillis.asStateFlow()

    private val _endDateMillis = MutableStateFlow<Long?>(null)
    val endDateMillis: StateFlow<Long?> = _endDateMillis.asStateFlow()

    private val _typeFilter = MutableStateFlow(HistoryTypeFilter.ALL)
    val typeFilter: StateFlow<HistoryTypeFilter> = _typeFilter.asStateFlow()

    private val _selectedCategories = MutableStateFlow<Set<Category>>(emptySet())
    val selectedCategories: StateFlow<Set<Category>> = _selectedCategories.asStateFlow()

    val transactions: StateFlow<List<Transaction>> = repository.getAllTransactions().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val activeFilters: StateFlow<HistoryFilters> = combine(
        searchQuery,
        startDateMillis,
        endDateMillis,
        typeFilter,
        selectedCategories
    ) { query, startMillis, endMillis, typeFilter, categories ->
        HistoryFilters(
            query = query,
            startMillis = startMillis,
            endMillis = endMillis,
            typeFilter = typeFilter,
            categories = categories
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = HistoryFilters(
                query = "",
                startMillis = null,
                endMillis = null,
                typeFilter = HistoryTypeFilter.ALL,
                categories = emptySet()
            )
        )

    val filteredTransactions: StateFlow<List<Transaction>> = combine(
        transactions,
        activeFilters
    ) { transactions, filters ->
        transactions.filter { transaction ->
            val matchesSearch = filters.query.isBlank() ||
                transaction.note.contains(filters.query, ignoreCase = true)

            val matchesType = when (filters.typeFilter) {
                HistoryTypeFilter.ALL -> true
                HistoryTypeFilter.INCOME -> transaction.type == TransactionType.INCOME
                HistoryTypeFilter.EXPENSE -> transaction.type == TransactionType.EXPENSE
            }

            val matchesCategory = filters.categories.isEmpty() || transaction.category in filters.categories
            val matchesStart = filters.startMillis == null || transaction.date >= filters.startMillis
            val matchesEnd = filters.endMillis == null || transaction.date <= filters.endMillis

            matchesSearch && matchesType && matchesCategory && matchesStart && matchesEnd
        }
    }
        .flowOn(dispatchers.default)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val groupedTransactions: StateFlow<List<HistoryDayGroup>> = filteredTransactions
        .map { filtered ->
            filtered
                .sortedByDescending { it.date }
                .groupBy { startOfDay(it.date) }
                .toSortedMap(compareByDescending { it })
                .map { (dayStartMillis, items) ->
                    HistoryDayGroup(
                        dayStartMillis = dayStartMillis,
                        transactions = items
                    )
                }
        }
        .flowOn(dispatchers.default)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateStartDateMillis(millis: Long?) {
        _startDateMillis.value = millis
    }

    fun updateEndDateMillis(millis: Long?) {
        _endDateMillis.value = millis
    }

    fun updateTypeFilter(filter: HistoryTypeFilter) {
        _typeFilter.value = filter
    }

    fun toggleCategory(category: Category) {
        _selectedCategories.update { selected ->
            if (category in selected) selected - category else selected + category
        }
    }

    fun clearDateRange() {
        _startDateMillis.value = null
        _endDateMillis.value = null
    }

    fun clearFilters() {
        _searchQuery.value = ""
        _startDateMillis.value = null
        _endDateMillis.value = null
        _typeFilter.value = HistoryTypeFilter.ALL
        _selectedCategories.value = emptySet()
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
                repository.insertTransaction(transaction)
            } else {
                repository.updateTransaction(transaction)
            }
        }
    }

    fun deleteTransaction(id: Long) {
        viewModelScope.launch(dispatchers.io) {
            repository.deleteTransaction(id)
        }
    }

    private fun startOfDay(millis: Long): Long {
        return Calendar.getInstance().apply {
            timeInMillis = millis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
}
