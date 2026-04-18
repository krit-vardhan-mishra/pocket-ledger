package com.just_for_fun.pocketledger.domain.usecase

import com.just_for_fun.pocketledger.data.model.enums.TransactionType
import com.just_for_fun.pocketledger.data.model.CategoryTotal
import com.just_for_fun.pocketledger.data.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCategoryBreakdownUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    operator fun invoke(
        month: String,
        year: String,
        type: TransactionType = TransactionType.EXPENSE
    ): Flow<List<CategoryTotal>> {
        return repository.getCategoryTotals(month, year, type)
    }
}
