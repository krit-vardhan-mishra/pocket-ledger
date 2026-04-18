package com.just_for_fun.pocketledger.data.repository

import com.just_for_fun.pocketledger.data.db.dao.BudgetDao
import com.just_for_fun.pocketledger.data.model.Budget
import com.just_for_fun.pocketledger.data.model.enums.Category
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BudgetRepository @Inject constructor(
    private val budgetDao: BudgetDao
) {

    fun getAllBudgets(): Flow<List<Budget>> = budgetDao.getAllBudgets()

    suspend fun setBudget(category: Category, limit: Double) {
        budgetDao.insertOrUpdate(Budget(category, limit))
    }

    suspend fun getBudgetForCategory(category: Category): Budget? =
        budgetDao.getBudgetForCategory(category)

    suspend fun deleteBudget(category: Category) =
        budgetDao.deleteByCategory(category)
}