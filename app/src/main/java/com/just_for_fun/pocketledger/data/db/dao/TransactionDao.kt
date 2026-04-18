package com.just_for_fun.pocketledger.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.just_for_fun.pocketledger.data.model.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: Transaction)

    @Update
    suspend fun update(transaction: Transaction)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM transactions ORDER BY date DESC LIMIT 5")
    fun getRecentTransactions(): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<Transaction>>

    @Query("""
        SELECT * FROM transactions 
        WHERE strftime('%m', date / 1000, 'unixepoch') = :month 
          AND strftime('%Y', date / 1000, 'unixepoch') = :year 
        ORDER BY date DESC
    """)
    fun getTransactionsByMonth(month: String, year: String): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE date BETWEEN :startMillis AND :endMillis ORDER BY date DESC")
    fun getTransactionsByDateRange(startMillis: Long, endMillis: Long): Flow<List<Transaction>>

    @Query("""
        SELECT COALESCE(SUM(amount), 0.0) FROM transactions 
        WHERE type = 'EXPENSE' 
          AND date BETWEEN :startOfDay AND :endOfDay
    """)
    suspend fun getTodayExpenseTotal(startOfDay: Long, endOfDay: Long): Double

    @Query("""
        SELECT COALESCE(SUM(amount), 0.0) FROM transactions 
        WHERE type = 'INCOME' 
          AND strftime('%m', date / 1000, 'unixepoch') = :month 
          AND strftime('%Y', date / 1000, 'unixepoch') = :year
    """)
    fun getMonthlyIncomeTotal(month: String, year: String): Flow<Double>

    @Query("""
        SELECT COALESCE(SUM(amount), 0.0) FROM transactions 
        WHERE type = 'EXPENSE' 
          AND strftime('%m', date / 1000, 'unixepoch') = :month 
          AND strftime('%Y', date / 1000, 'unixepoch') = :year
    """)
    fun getMonthlyExpenseTotal(month: String, year: String): Flow<Double>
}