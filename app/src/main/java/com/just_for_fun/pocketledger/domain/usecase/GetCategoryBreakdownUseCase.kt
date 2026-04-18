package com.just_for_fun.pocketledger.domain.usecase

import com.just_for_fun.pocketledger.data.model.enums.TransactionType
import com.just_for_fun.pocketledger.data.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

data class CategoryBreakdown(
    val category: String,
    val amount: Double,
    val percentage: Float
)

class GetCategoryBreakdownUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    operator fun invoke(month: String, year: String, type: TransactionType = TransactionType.EXPENSE): Flow<List<CategoryBreakdown>> {
        return repository.getTransactionsByMonth(month, year).map { transactions ->
            val filtered = transactions.filter { it.type == type }
            val total = filtered.sumOf { it.amount }
            
            if (total == 0.0) return@map emptyList()
            
            filtered.groupBy { it.category }
                .map { (category, items) ->
                    val sum = items.sumOf { it.amount }
                    CategoryBreakdown(
                        category = category.name,
                        amount = sum,
                        percentage = (sum / total * 100).toFloat()
                    )
                }
                .sortedByDescending { it.amount }
        }
    }
}
