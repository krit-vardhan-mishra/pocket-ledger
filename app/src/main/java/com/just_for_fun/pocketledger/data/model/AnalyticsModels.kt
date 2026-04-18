package com.just_for_fun.pocketledger.data.model

import com.just_for_fun.pocketledger.data.model.enums.Category

data class CategoryTotal(
    val category: Category,
    val amount: Double,
    val percentage: Float
)

data class DailyTotal(
    val dayOfMonth: Int,
    val amount: Double
)

data class ExceededCategory(
    val category: Category,
    val exceededBy: Double
)
