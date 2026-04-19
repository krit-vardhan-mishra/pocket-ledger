package com.just_for_fun.pocketledger.data.repository

import com.just_for_fun.pocketledger.MainDispatcherRule
import com.just_for_fun.pocketledger.data.db.dao.TransactionDao
import com.just_for_fun.pocketledger.data.model.Transaction
import com.just_for_fun.pocketledger.data.model.enums.Category
import com.just_for_fun.pocketledger.data.model.enums.TransactionType
import com.just_for_fun.pocketledger.di.AppCoroutineDispatchers
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TransactionRepositoryTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private val transactionDao: TransactionDao = mockk()

    @Test
    fun getCategoryTotals_andDailyTotals_aggregateExpenseTransactions() = runTest {
        val transactionsFlow = MutableStateFlow(
            listOf(
                Transaction(
                    id = 1,
                    amount = 300.0,
                    type = TransactionType.EXPENSE,
                    category = Category.FOOD,
                    note = "Lunch",
                    date = 1_711_000_000_000L
                ),
                Transaction(
                    id = 2,
                    amount = 200.0,
                    type = TransactionType.EXPENSE,
                    category = Category.TRANSPORT,
                    note = "Taxi",
                    date = 1_711_086_400_000L
                ),
                Transaction(
                    id = 3,
                    amount = 100.0,
                    type = TransactionType.EXPENSE,
                    category = Category.FOOD,
                    note = "Snacks",
                    date = 1_711_086_400_000L
                ),
                Transaction(
                    id = 4,
                    amount = 1000.0,
                    type = TransactionType.INCOME,
                    category = Category.SALARY,
                    note = "Salary",
                    date = 1_711_172_800_000L
                )
            )
        )

        every { transactionDao.getTransactionsByMonth("04", "2026") } returns transactionsFlow

        val repository = TransactionRepository(
            transactionDao,
            AppCoroutineDispatchers(
                io = dispatcherRule.dispatcher,
                default = dispatcherRule.dispatcher,
                main = dispatcherRule.dispatcher
            )
        )

        val categoryTotals = repository.getCategoryTotals("04", "2026", TransactionType.EXPENSE).first()
        assertEquals(2, categoryTotals.size)
        assertEquals(Category.FOOD, categoryTotals.first().category)
        assertEquals(400.0, categoryTotals.first().amount, 0.0)
        assertEquals(66.666f, categoryTotals.first().percentage, 0.01f)

        val dailyTotals = repository.getDailyTotals("04", "2026", TransactionType.EXPENSE).first()
        assertEquals(2, dailyTotals.size)
        assertEquals(600.0, dailyTotals.sumOf { it.amount }, 0.0)
        assertTrue(dailyTotals[0].dayOfMonth < dailyTotals[1].dayOfMonth)
        assertEquals(300.0, dailyTotals[0].amount, 0.0)
        assertEquals(300.0, dailyTotals[1].amount, 0.0)
    }
}
