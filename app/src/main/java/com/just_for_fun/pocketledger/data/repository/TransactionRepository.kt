package com.just_for_fun.pocketledger.data.repository

import com.just_for_fun.pocketledger.data.db.dao.TransactionDao
import com.just_for_fun.pocketledger.data.model.Transaction
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionRepository @Inject constructor(
    private val transactionDao: TransactionDao
) {

    suspend fun insertTransaction(transaction: Transaction) =
        transactionDao.insert(transaction)

    suspend fun updateTransaction(transaction: Transaction) =
        transactionDao.update(transaction)

    suspend fun deleteTransaction(id: Long) =
        transactionDao.deleteById(id)

    fun getAllTransactions(): Flow<List<Transaction>> =
        transactionDao.getAllTransactions()

    fun getRecentTransactions(): Flow<List<Transaction>> =
        transactionDao.getRecentTransactions()

    fun getTransactionsByMonth(month: String, year: String): Flow<List<Transaction>> =
        transactionDao.getTransactionsByMonth(month, year)

    fun getTransactionsByDateRange(startMillis: Long, endMillis: Long): Flow<List<Transaction>> =
        transactionDao.getTransactionsByDateRange(startMillis, endMillis)

    suspend fun getTodayExpenseTotal(startOfDay: Long, endOfDay: Long): Double =
        transactionDao.getTodayExpenseTotal(startOfDay, endOfDay)

    fun getMonthlyIncomeTotal(month: String, year: String): Flow<Double> =
        transactionDao.getMonthlyIncomeTotal(month, year)

    fun getMonthlyExpenseTotal(month: String, year: String): Flow<Double> =
        transactionDao.getMonthlyExpenseTotal(month, year)
}