package com.just_for_fun.pocketledger.ui.statistics

import com.just_for_fun.pocketledger.MainDispatcherRule
import com.just_for_fun.pocketledger.data.model.CategoryTotal
import com.just_for_fun.pocketledger.data.model.DailyTotal
import com.just_for_fun.pocketledger.data.model.enums.Category
import com.just_for_fun.pocketledger.data.model.enums.TransactionType
import com.just_for_fun.pocketledger.data.repository.TransactionRepository
import com.just_for_fun.pocketledger.domain.usecase.GetCategoryBreakdownUseCase
import com.just_for_fun.pocketledger.domain.usecase.GetMonthlySummaryUseCase
import com.just_for_fun.pocketledger.domain.usecase.MonthlySummary
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class StatisticsViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private val transactionRepository: TransactionRepository = mockk()
    private val getMonthlySummaryUseCase: GetMonthlySummaryUseCase = mockk()
    private val getCategoryBreakdownUseCase: GetCategoryBreakdownUseCase = mockk()

    @Test
    fun highestSpendDayAndTopCategory_areDerivedFromFlows() = runTest {
        every { transactionRepository.getDailyTotals(any(), any(), TransactionType.EXPENSE) } returns MutableStateFlow(
            listOf(
                DailyTotal(dayOfMonth = 3, amount = 500.0),
                DailyTotal(dayOfMonth = 10, amount = 950.0),
                DailyTotal(dayOfMonth = 17, amount = 300.0)
            )
        )
        every { getCategoryBreakdownUseCase.invoke(any(), any(), TransactionType.EXPENSE) } returns MutableStateFlow(
            listOf(
                CategoryTotal(Category.FOOD, 250.0, 20f),
                CategoryTotal(Category.SHOPPING, 600.0, 48f),
                CategoryTotal(Category.TRANSPORT, 400.0, 32f)
            )
        )
        every { getMonthlySummaryUseCase.invoke(any(), any()) } returns flowOf(
            MonthlySummary(totalIncome = 2000.0, totalExpense = 1250.0, balance = 750.0)
        )

        val viewModel = StatisticsViewModel(transactionRepository, getMonthlySummaryUseCase, getCategoryBreakdownUseCase)
        val highestSpendCollector = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.highestSpendDay.collect {}
        }
        val topCategoryCollector = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.mostSpentCategory.collect {}
        }

        advanceUntilIdle()

        assertEquals(10, viewModel.highestSpendDay.value?.dayOfMonth)
        assertEquals(950.0, viewModel.highestSpendDay.value?.amount ?: 0.0, 0.0)
        assertEquals(Category.SHOPPING, viewModel.mostSpentCategory.value?.category)
        assertEquals(600.0, viewModel.mostSpentCategory.value?.amount ?: 0.0, 0.0)

        highestSpendCollector.cancel()
        topCategoryCollector.cancel()
    }

    @Test
    fun nextPage_isBlockedOnCurrentMonth_andEnabledAfterGoingBack() = runTest {
        every { transactionRepository.getDailyTotals(any(), any(), TransactionType.EXPENSE) } returns MutableStateFlow(emptyList())
        every { getCategoryBreakdownUseCase.invoke(any(), any(), TransactionType.EXPENSE) } returns MutableStateFlow(emptyList())
        every { getMonthlySummaryUseCase.invoke(any(), any()) } returns flowOf(
            MonthlySummary(totalIncome = 0.0, totalExpense = 0.0, balance = 0.0)
        )

        val viewModel = StatisticsViewModel(transactionRepository, getMonthlySummaryUseCase, getCategoryBreakdownUseCase)
        val canNavigateCollector = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.canNavigateNext.collect {}
        }

        advanceUntilIdle()

        val initialPage = viewModel.getFormattedDate()
        assertFalse(viewModel.canNavigateNext.value)

        viewModel.nextPage()
        advanceUntilIdle()

        assertEquals(initialPage, viewModel.getFormattedDate())

        viewModel.previousPage()
        advanceUntilIdle()

        assertTrue(viewModel.canNavigateNext.value)

        canNavigateCollector.cancel()
    }
}
