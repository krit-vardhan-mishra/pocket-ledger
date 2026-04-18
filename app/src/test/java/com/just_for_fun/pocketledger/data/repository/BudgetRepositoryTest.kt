package com.just_for_fun.pocketledger.data.repository

import com.just_for_fun.pocketledger.MainDispatcherRule
import com.just_for_fun.pocketledger.data.db.dao.BudgetDao
import com.just_for_fun.pocketledger.data.db.dao.TransactionDao
import com.just_for_fun.pocketledger.data.model.Budget
import com.just_for_fun.pocketledger.data.model.Transaction
import com.just_for_fun.pocketledger.data.model.enums.Category
import com.just_for_fun.pocketledger.data.model.enums.TransactionType
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
class BudgetRepositoryTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private val budgetDao: BudgetDao = mockk()
    private val transactionDao: TransactionDao = mockk()

    @Test
    fun getExceededCategoryDetails_returnsOnlyExceededBudgets() = runTest {
        val budgetsFlow = MutableStateFlow(
            listOf(
                Budget(Category.FOOD, 500.0),
                Budget(Category.TRANSPORT, 300.0)
            )
        )
        val transactionsFlow = MutableStateFlow(
            listOf(
                Transaction(
                    id = 1,
                    amount = 650.0,
                    type = TransactionType.EXPENSE,
                    category = Category.FOOD,
                    note = "Groceries",
                    date = 1_700_000_000_000L
                ),
                Transaction(
                    id = 2,
                    amount = 180.0,
                    type = TransactionType.EXPENSE,
                    category = Category.TRANSPORT,
                    note = "Fuel",
                    date = 1_700_000_000_000L
                )
            )
        )

        every { budgetDao.getAllBudgets() } returns budgetsFlow
        every { transactionDao.getTransactionsByMonth("04", "2026") } returns transactionsFlow

        val repository = BudgetRepository(budgetDao, transactionDao)
        val exceeded = repository.getExceededCategoryDetails("04", "2026").first()

        assertEquals(1, exceeded.size)
        assertEquals(Category.FOOD, exceeded.first().category)
        assertEquals(150.0, exceeded.first().exceededBy, 0.0)

        val exceededCategories = repository.getExceededCategories("04", "2026").first()
        assertTrue(exceededCategories.contains(Category.FOOD))
        assertEquals(1, exceededCategories.size)
    }
}
