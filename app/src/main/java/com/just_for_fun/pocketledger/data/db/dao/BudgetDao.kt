package com.just_for_fun.pocketledger.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.just_for_fun.pocketledger.data.model.Budget
import com.just_for_fun.pocketledger.data.model.enums.Category
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(budget: Budget)

    @Query("SELECT * FROM budgets")
    fun getAllBudgets(): Flow<List<Budget>>

    @Query("SELECT * FROM budgets WHERE category = :category")
    suspend fun getBudgetForCategory(category: Category): Budget?

    @Query("DELETE FROM budgets WHERE category = :category")
    suspend fun deleteByCategory(category: Category)
}