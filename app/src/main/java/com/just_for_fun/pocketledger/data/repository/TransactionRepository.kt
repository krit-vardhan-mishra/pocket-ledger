package com.just_for_fun.pocketledger.data.repository

import com.just_for_fun.pocketledger.data.db.dao.TransactionDao
import com.just_for_fun.pocketledger.data.model.CategoryTotal
import com.just_for_fun.pocketledger.data.model.DailyTotal
import com.just_for_fun.pocketledger.data.model.Transaction
import com.just_for_fun.pocketledger.data.model.enums.TransactionType
import com.just_for_fun.pocketledger.di.AppCoroutineDispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionRepository @Inject constructor(
    private val transactionDao: TransactionDao,
    private val dispatchers: AppCoroutineDispatchers = AppCoroutineDispatchers()
) {

    suspend fun insertTransaction(transaction: Transaction) = withContext(dispatchers.io) {
        transactionDao.insert(transaction)
    }

    suspend fun updateTransaction(transaction: Transaction) = withContext(dispatchers.io) {
        transactionDao.update(transaction)
    }

    suspend fun deleteTransaction(id: Long) = withContext(dispatchers.io) {
        transactionDao.deleteById(id)
    }

    fun getAllTransactions(): Flow<List<Transaction>> =
        transactionDao.getAllTransactions()

    fun getRecentTransactions(): Flow<List<Transaction>> =
        transactionDao.getRecentTransactions()

    fun getTransactionsByMonth(month: String, year: String): Flow<List<Transaction>> =
        transactionDao.getTransactionsByMonth(month, year)

    fun getTransactionsByDateRange(startMillis: Long, endMillis: Long): Flow<List<Transaction>> =
        transactionDao.getTransactionsByDateRange(startMillis, endMillis)

    suspend fun getTodayExpenseTotal(startOfDay: Long, endOfDay: Long): Double =
        withContext(dispatchers.io) {
            transactionDao.getTodayExpenseTotal(startOfDay, endOfDay)
        }

    fun getMonthlyIncomeTotal(month: String, year: String): Flow<Double> =
        transactionDao.getMonthlyIncomeTotal(month, year)

    fun getMonthlyExpenseTotal(month: String, year: String): Flow<Double> =
        transactionDao.getMonthlyExpenseTotal(month, year)

    fun getCategoryTotals(
        month: String,
        year: String,
        type: TransactionType = TransactionType.EXPENSE
    ): Flow<List<CategoryTotal>> {
        return getTransactionsByMonth(month, year).map { transactions ->
            val filtered = transactions.filter { it.type == type }
            val total = filtered.sumOf { it.amount }
            if (total <= 0.0) {
                emptyList()
            } else {
                filtered.groupBy { it.category }
                    .map { (category, items) ->
                        val amount = items.sumOf { it.amount }
                        CategoryTotal(
                            category = category,
                            amount = amount,
                            percentage = ((amount / total) * 100.0).toFloat()
                        )
                    }
                    .sortedByDescending { it.amount }
            }
        }.flowOn(dispatchers.default)
    }

    fun getDailyTotals(
        month: String,
        year: String,
        type: TransactionType = TransactionType.EXPENSE
    ): Flow<List<DailyTotal>> {
        return getTransactionsByMonth(month, year).map { transactions ->
            transactions
                .filter { it.type == type }
                .groupBy { transaction ->
                    Calendar.getInstance().apply { timeInMillis = transaction.date }
                        .get(Calendar.DAY_OF_MONTH)
                }
                .map { (day, items) ->
                    DailyTotal(
                        dayOfMonth = day,
                        amount = items.sumOf { it.amount }
                    )
                }
                .sortedBy { it.dayOfMonth }
        }.flowOn(dispatchers.default)
    }
}