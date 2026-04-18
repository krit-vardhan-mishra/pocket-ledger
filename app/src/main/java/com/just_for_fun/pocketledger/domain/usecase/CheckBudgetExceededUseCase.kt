package com.just_for_fun.pocketledger.domain.usecase

import com.just_for_fun.pocketledger.data.model.enums.Category
import com.just_for_fun.pocketledger.data.repository.BudgetRepository
import com.just_for_fun.pocketledger.data.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CheckBudgetExceededUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val budgetRepository: BudgetRepository
) {
    suspend operator fun invoke(category: Category, month: String, year: String): Boolean {
        val budget = budgetRepository.getBudgetForCategory(category) ?: return false
        val transactions = transactionRepository.getTransactionsByMonth(month, year).first()
        val totalSpent = transactions.filter { it.category == category && it.type.name == "EXPENSE" }
            .sumOf { it.amount }
        
        return totalSpent > budget.monthlyLimit
    }
}