package com.just_for_fun.pocketledger.domain.usecase

import com.just_for_fun.pocketledger.data.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

data class MonthlySummary(
    val totalIncome: Double,
    val totalExpense: Double,
    val balance: Double
)

class GetMonthlySummaryUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    operator fun invoke(month: String, year: String): Flow<MonthlySummary> {
        return combine(
            repository.getMonthlyIncomeTotal(month, year),
            repository.getMonthlyExpenseTotal(month, year)
        ) { income, expense ->
            MonthlySummary(
                totalIncome = income,
                totalExpense = expense,
                balance = income - expense
            )
        }
    }
}