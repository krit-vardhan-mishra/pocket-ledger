package com.just_for_fun.pocketledger.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.just_for_fun.pocketledger.data.model.enums.Category

@Entity(tableName = "budgets")
data class Budget(
    @PrimaryKey val category: Category,
    val monthlyLimit: Double
)