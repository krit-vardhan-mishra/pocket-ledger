package com.just_for_fun.pocketledger.ui.transaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.just_for_fun.pocketledger.data.model.Transaction
import com.just_for_fun.pocketledger.data.model.enums.TransactionType as DomainTransactionType
import com.just_for_fun.pocketledger.data.model.enums.Category
import com.just_for_fun.pocketledger.data.repository.TransactionRepository
import com.just_for_fun.pocketledger.di.AppCoroutineDispatchers
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TransactionViewModel @Inject constructor(
    private val repository: TransactionRepository,
    private val dispatchers: AppCoroutineDispatchers = AppCoroutineDispatchers()
) : ViewModel() {

    private val _uiState = MutableStateFlow<TransactionUiState>(TransactionUiState.Idle)
    val uiState = _uiState.asStateFlow()

    fun addTransaction(
        note: String,
        amount: Double,
        category: Category,
        type: DomainTransactionType,
        date: Long = System.currentTimeMillis()
    ) {
        if (note.isBlank() || amount <= 0) {
            _uiState.value = TransactionUiState.Error("Invalid input")
            return
        }

        viewModelScope.launch(dispatchers.io) {
            _uiState.value = TransactionUiState.Loading
            try {
                val transaction = Transaction(
                    note = note,
                    amount = amount,
                    category = category,
                    type = type,
                    date = date
                )
                repository.insertTransaction(transaction)
                _uiState.value = TransactionUiState.Success
            } catch (e: Exception) {
                _uiState.value = TransactionUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun resetState() {
        _uiState.value = TransactionUiState.Idle
    }
}

sealed class TransactionUiState {
    object Idle : TransactionUiState()
    object Loading : TransactionUiState()
    object Success : TransactionUiState()
    data class Error(val message: String) : TransactionUiState()
}
