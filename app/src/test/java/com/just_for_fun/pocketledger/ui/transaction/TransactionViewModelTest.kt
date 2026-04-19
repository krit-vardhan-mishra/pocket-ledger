package com.just_for_fun.pocketledger.ui.transaction

import com.just_for_fun.pocketledger.MainDispatcherRule
import com.just_for_fun.pocketledger.data.model.enums.Category
import com.just_for_fun.pocketledger.data.model.enums.TransactionType
import com.just_for_fun.pocketledger.data.repository.TransactionRepository
import com.just_for_fun.pocketledger.di.AppCoroutineDispatchers
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TransactionViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private val repository: TransactionRepository = mockk(relaxed = true)
    private val testDispatchers by lazy {
        AppCoroutineDispatchers(
            io = dispatcherRule.dispatcher,
            default = dispatcherRule.dispatcher,
            main = dispatcherRule.dispatcher
        )
    }

    @Test
    fun addTransaction_invalidInput_setsErrorState() = runTest {
        val viewModel = TransactionViewModel(repository, testDispatchers)

        viewModel.addTransaction(
            note = "",
            amount = 0.0,
            category = Category.FOOD,
            type = TransactionType.EXPENSE
        )

        assertTrue(viewModel.uiState.value is TransactionUiState.Error)
    }

    @Test
    fun addTransaction_validInput_callsInsertAndSetsSuccess() = runTest {
        val viewModel = TransactionViewModel(repository, testDispatchers)

        viewModel.addTransaction(
            note = "Lunch",
            amount = 120.0,
            category = Category.FOOD,
            type = TransactionType.EXPENSE
        )

        advanceUntilIdle()

        coVerify(exactly = 1) { repository.insertTransaction(any()) }
        assertTrue(viewModel.uiState.value is TransactionUiState.Success)
    }
}
