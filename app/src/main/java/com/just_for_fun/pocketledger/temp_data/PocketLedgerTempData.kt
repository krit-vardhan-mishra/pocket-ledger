package com.just_for_fun.pocketledger.temp_data

import com.just_for_fun.pocketledger.data.model.Budget
import com.just_for_fun.pocketledger.data.model.CategoryTotal
import com.just_for_fun.pocketledger.data.model.DailyTotal
import com.just_for_fun.pocketledger.data.model.Transaction
import com.just_for_fun.pocketledger.data.model.enums.Category
import com.just_for_fun.pocketledger.data.model.enums.TransactionType
import com.just_for_fun.pocketledger.domain.usecase.MonthlySummary
import java.util.Calendar

object PocketLedgerTempData {

    private const val DEMO_YEAR = 2026
    private const val DEMO_MONTH = Calendar.MARCH

    val transactions: List<Transaction> = listOf(
        Transaction(
            id = 90001L,
            amount = 85000.0,
            type = TransactionType.INCOME,
            category = Category.SALARY,
            note = "Monthly salary credit",
            date = demoDate(1, 10, 0)
        ),
        Transaction(
            id = 90002L,
            amount = 12000.0,
            type = TransactionType.INCOME,
            category = Category.OTHER,
            note = "Freelance payment",
            date = demoDate(9, 19, 10)
        ),
        Transaction(
            id = 90003L,
            amount = 2650.0,
            type = TransactionType.EXPENSE,
            category = Category.BILLS,
            note = "Electricity and internet bill",
            date = demoDate(3, 8, 30)
        ),
        Transaction(
            id = 90004L,
            amount = 3200.0,
            type = TransactionType.EXPENSE,
            category = Category.SHOPPING,
            note = "Sneakers and gym tee",
            date = demoDate(6, 18, 15)
        ),
        Transaction(
            id = 90005L,
            amount = 780.0,
            type = TransactionType.EXPENSE,
            category = Category.HEALTH,
            note = "Medicines",
            date = demoDate(7, 21, 0)
        ),
        Transaction(
            id = 90006L,
            amount = 920.0,
            type = TransactionType.EXPENSE,
            category = Category.ENTERTAINMENT,
            note = "Movie and snacks",
            date = demoDate(9, 22, 45)
        ),
        Transaction(
            id = 90007L,
            amount = 1450.0,
            type = TransactionType.EXPENSE,
            category = Category.EDUCATION,
            note = "UI design course",
            date = demoDate(11, 14, 30)
        ),
        Transaction(
            id = 90008L,
            amount = 1250.0,
            type = TransactionType.EXPENSE,
            category = Category.FOOD,
            note = "Groceries",
            date = demoDate(13, 11, 15)
        ),
        Transaction(
            id = 90009L,
            amount = 350.0,
            type = TransactionType.EXPENSE,
            category = Category.TRANSPORT,
            note = "Metro recharge",
            date = demoDate(14, 10, 10)
        ),
        Transaction(
            id = 90010L,
            amount = 640.0,
            type = TransactionType.EXPENSE,
            category = Category.OTHER,
            note = "Gift wrap and stationery",
            date = demoDate(16, 17, 20)
        ),
        Transaction(
            id = 90011L,
            amount = 430.0,
            type = TransactionType.EXPENSE,
            category = Category.FOOD,
            note = "Cafe brunch",
            date = demoDate(17, 12, 35)
        ),
        Transaction(
            id = 90012L,
            amount = 180.0,
            type = TransactionType.EXPENSE,
            category = Category.TRANSPORT,
            note = "Auto ride",
            date = demoDate(17, 20, 15)
        ),
        Transaction(
            id = 90013L,
            amount = 520.0,
            type = TransactionType.EXPENSE,
            category = Category.FOOD,
            note = "Lunch bowl",
            date = todayDate(13, 10)
        ),
        Transaction(
            id = 90014L,
            amount = 220.0,
            type = TransactionType.EXPENSE,
            category = Category.TRANSPORT,
            note = "Ride share",
            date = todayDate(19, 25)
        )
    )

    val budgets: List<Budget> = listOf(
        Budget(Category.FOOD, 1400.0),
        Budget(Category.TRANSPORT, 400.0),
        Budget(Category.SHOPPING, 2500.0),
        Budget(Category.HEALTH, 1500.0),
        Budget(Category.ENTERTAINMENT, 800.0),
        Budget(Category.SALARY, 0.0),
        Budget(Category.BILLS, 3000.0),
        Budget(Category.EDUCATION, 2000.0),
        Budget(Category.OTHER, 1000.0)
    )

    val monthlySummary: MonthlySummary by lazy {
        val monthTransactions = getTransactionsForMonth(DEMO_MONTH, DEMO_YEAR)
        val income = monthTransactions
            .filter { it.type == TransactionType.INCOME }
            .sumOf { it.amount }
        val expense = monthTransactions
            .filter { it.type == TransactionType.EXPENSE }
            .sumOf { it.amount }
        MonthlySummary(
            totalIncome = income,
            totalExpense = expense,
            balance = income - expense
        )
    }

    val categoryBreakdown: List<CategoryTotal> by lazy {
        val expenses = getTransactionsForMonth(DEMO_MONTH, DEMO_YEAR)
            .filter { it.type == TransactionType.EXPENSE }

        val total = expenses.sumOf { it.amount }
        if (total <= 0.0) {
            emptyList()
        } else {
            expenses.groupBy { it.category }
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
    }

    val dailyTotals: List<DailyTotal> by lazy {
        getTransactionsForMonth(DEMO_MONTH, DEMO_YEAR)
            .filter { it.type == TransactionType.EXPENSE }
            .groupBy { dayOfMonth(it.date) }
            .map { (day, items) ->
                DailyTotal(
                    dayOfMonth = day,
                    amount = items.sumOf { it.amount }
                )
            }
            .sortedBy { it.dayOfMonth }
    }

    val highestSpendDay: DailyTotal?
        get() = dailyTotals.maxByOrNull { it.amount }

    val mostSpentCategory: CategoryTotal?
        get() = categoryBreakdown.maxByOrNull { it.amount }

    val exceededCategories: List<Category> by lazy {
        val expensesByCategory = getTransactionsForMonth(DEMO_MONTH, DEMO_YEAR)
            .filter { it.type == TransactionType.EXPENSE }
            .groupBy { it.category }
            .mapValues { (_, items) -> items.sumOf { it.amount } }

        budgets.mapNotNull { budget ->
            val spent = expensesByCategory[budget.category] ?: 0.0
            if (budget.monthlyLimit > 0.0 && spent > budget.monthlyLimit) {
                budget.category
            } else {
                null
            }
        }
    }

    val historyTransactions: List<Transaction>
        get() = transactions.sortedByDescending { it.date }

    fun todayTransactions(selectedCategory: Category?): List<Transaction> {
        val todayItems = transactions.filter { isToday(it.date) }
        return if (selectedCategory == null) {
            todayItems.sortedByDescending { it.date }
        } else {
            todayItems.filter { it.category == selectedCategory }.sortedByDescending { it.date }
        }
    }

    private fun getTransactionsForMonth(month: Int, year: Int): List<Transaction> {
        return transactions.filter { millis ->
            val calendar = Calendar.getInstance().apply { timeInMillis = millis.date }
            calendar.get(Calendar.MONTH) == month && calendar.get(Calendar.YEAR) == year
        }
    }

    private fun dayOfMonth(millis: Long): Int {
        val calendar = Calendar.getInstance().apply { timeInMillis = millis }
        return calendar.get(Calendar.DAY_OF_MONTH)
    }

    private fun isToday(millis: Long): Boolean {
        val now = Calendar.getInstance()
        val date = Calendar.getInstance().apply { timeInMillis = millis }
        return now.get(Calendar.YEAR) == date.get(Calendar.YEAR) &&
            now.get(Calendar.DAY_OF_YEAR) == date.get(Calendar.DAY_OF_YEAR)
    }

    private fun demoDate(day: Int, hour: Int, minute: Int): Long {
        return Calendar.getInstance().apply {
            set(Calendar.YEAR, DEMO_YEAR)
            set(Calendar.MONTH, DEMO_MONTH)
            set(Calendar.DAY_OF_MONTH, day)
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    private fun todayDate(hour: Int, minute: Int): Long {
        return Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
}
