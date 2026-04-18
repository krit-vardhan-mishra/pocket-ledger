package com.just_for_fun.pocketledger.ui.dashboard

import com.just_for_fun.pocketledger.MainDispatcherRule
import com.just_for_fun.pocketledger.data.model.Transaction
import com.just_for_fun.pocketledger.data.model.enums.Category
import com.just_for_fun.pocketledger.data.model.enums.TransactionType
import com.just_for_fun.pocketledger.data.repository.BudgetRepository
import com.just_for_fun.pocketledger.data.repository.TransactionRepository
import com.just_for_fun.pocketledger.domain.usecase.GetMonthlySummaryUseCase
import com.just_for_fun.pocketledger.domain.usecase.MonthlySummary
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private val transactionRepository: TransactionRepository = mockk(relaxed = true)
    private val budgetRepository: BudgetRepository = mockk(relaxed = true)
    private val getMonthlySummaryUseCase: GetMonthlySummaryUseCase = mockk()

    @Test
    fun filterByCategory_returnsOnlyMatchingTransactions() = runTest {
        val transactions = listOf(
            Transaction(
                id = 1,
                amount = 120.0,
                type = TransactionType.EXPENSE,
                category = Category.FOOD,
                note = "Lunch",
                date = System.currentTimeMillis()
            ),
            Transaction(
                id = 2,
                amount = 80.0,
                type = TransactionType.EXPENSE,
                category = Category.TRANSPORT,
                note = "Cab",
                date = System.currentTimeMillis()
            )
        )

        every { transactionRepository.getTransactionsByDateRange(any(), any()) } returns MutableStateFlow(transactions)
        every { budgetRepository.getExceededCategories(any(), any()) } returns flowOf(emptyList())
        every { getMonthlySummaryUseCase.invoke(any(), any()) } returns flowOf(
            MonthlySummary(totalIncome = 0.0, totalExpense = 200.0, balance = -200.0)
        )

        val viewModel = DashboardViewModel(transactionRepository, budgetRepository, getMonthlySummaryUseCase)
        val collectJob = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.filteredTransactions.collect {}
        }

        advanceUntilIdle()
        assertEquals(2, viewModel.filteredTransactions.value.size)

        viewModel.filterByCategory(Category.FOOD)
        advanceUntilIdle()

        val filtered = viewModel.filteredTransactions.value
        assertEquals(1, filtered.size)
        assertEquals(Category.FOOD, filtered.first().category)

        collectJob.cancel()
    }

    @Test
    fun saveTransaction_withNullId_insertsTransaction() = runTest {
        every { transactionRepository.getTransactionsByDateRange(any(), any()) } returns MutableStateFlow(emptyList())
        every { budgetRepository.getExceededCategories(any(), any()) } returns flowOf(emptyList())
        every { getMonthlySummaryUseCase.invoke(any(), any()) } returns flowOf(
            MonthlySummary(totalIncome = 0.0, totalExpense = 0.0, balance = 0.0)
        )

        val viewModel = DashboardViewModel(transactionRepository, budgetRepository, getMonthlySummaryUseCase)

        viewModel.saveTransaction(
            id = null,
            amount = 250.0,
            type = TransactionType.INCOME,
            category = Category.SALARY,
            note = "Freelance",
            date = 1_700_000_000_000L
        )

        advanceUntilIdle()

        coVerify(exactly = 1) {
            transactionRepository.insertTransaction(
                withArg {
                    assertEquals(0L, it.id)
                    assertEquals(250.0, it.amount, 0.0)
                    assertEquals(TransactionType.INCOME, it.type)
                    assertEquals(Category.SALARY, it.category)
                }
            )
        }
        coVerify(exactly = 0) { transactionRepository.updateTransaction(any()) }
    }

    @Test
    fun saveTransaction_withExistingId_updatesTransaction() = runTest {
        every { transactionRepository.getTransactionsByDateRange(any(), any()) } returns MutableStateFlow(emptyList())
        every { budgetRepository.getExceededCategories(any(), any()) } returns flowOf(emptyList())
        every { getMonthlySummaryUseCase.invoke(any(), any()) } returns flowOf(
            MonthlySummary(totalIncome = 0.0, totalExpense = 0.0, balance = 0.0)
        )

        val viewModel = DashboardViewModel(transactionRepository, budgetRepository, getMonthlySummaryUseCase)

        viewModel.saveTransaction(
            id = 99L,
            amount = 45.0,
            type = TransactionType.EXPENSE,
            category = Category.FOOD,
            note = "Dinner",
            date = 1_700_000_000_000L
        )

        advanceUntilIdle()

        coVerify(exactly = 1) {
            transactionRepository.updateTransaction(
                withArg {
                    assertEquals(99L, it.id)
                    assertEquals(45.0, it.amount, 0.0)
                    assertEquals(TransactionType.EXPENSE, it.type)
                }
            )
        }
        coVerify(exactly = 0) { transactionRepository.insertTransaction(any()) }
        assertTrue(viewModel.filteredTransactions.value.isEmpty())
    }
}
