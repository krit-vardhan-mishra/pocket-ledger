package com.just_for_fun.pocketledger.data.repository

import com.just_for_fun.pocketledger.data.db.dao.BudgetDao
import com.just_for_fun.pocketledger.data.db.dao.TransactionDao
import com.just_for_fun.pocketledger.data.model.Budget
import com.just_for_fun.pocketledger.data.model.ExceededCategory
import com.just_for_fun.pocketledger.data.model.enums.Category
import com.just_for_fun.pocketledger.data.model.enums.TransactionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BudgetRepository @Inject constructor(
    private val budgetDao: BudgetDao,
    private val transactionDao: TransactionDao
) {

    fun getAllBudgets(): Flow<List<Budget>> = budgetDao.getAllBudgets()

    suspend fun setBudget(category: Category, limit: Double) {
        budgetDao.insertOrUpdate(Budget(category, limit))
    }

    suspend fun getBudgetForCategory(category: Category): Budget? =
        budgetDao.getBudgetForCategory(category)

    suspend fun deleteBudget(category: Category) =
        budgetDao.deleteByCategory(category)

    fun getExceededCategoryDetails(month: String, year: String): Flow<List<ExceededCategory>> {
        return combine(
            budgetDao.getAllBudgets(),
            transactionDao.getTransactionsByMonth(month, year)
        ) { budgets, transactions ->
            val expensesByCategory = transactions
                .filter { it.type == TransactionType.EXPENSE }
                .groupBy { it.category }
                .mapValues { (_, items) -> items.sumOf { it.amount } }

            budgets.mapNotNull { budget ->
                val spent = expensesByCategory[budget.category] ?: 0.0
                val exceededBy = spent - budget.monthlyLimit
                if (exceededBy > 0) {
                    ExceededCategory(budget.category, exceededBy)
                } else {
                    null
                }
            }.sortedByDescending { it.exceededBy }
        }
    }

    fun getExceededCategories(month: String, year: String): Flow<List<Category>> {
        return getExceededCategoryDetails(month, year).combine(budgetDao.getAllBudgets()) { details, _ ->
            details.map { it.category }
        }
    }

    suspend fun getExceededCategoryDetailsSnapshot(month: String, year: String): List<ExceededCategory> {
        return getExceededCategoryDetails(month, year).first()
    }
}