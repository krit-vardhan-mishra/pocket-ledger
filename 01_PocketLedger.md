# 1. POCKETLEDGER
## Personal Finance Tracker — Android Native (Kotlin)

---

## 1.1 Product Overview

PocketLedger is an offline-first personal finance tracker built natively for Android using Kotlin and Jetpack libraries. It allows users to log income and expenses, categorize transactions, visualize spending patterns through charts, and receive daily budget summary notifications — all without requiring an internet connection or account creation.

**One-line pitch for resume:**  
*"Offline-first Android finance tracker with MVVM architecture, Room DB, Jetpack Compose UI, WorkManager notifications, and JUnit-tested ViewModel/Repository layers with GitHub Actions CI."*

---

## 1.2 Goals

**Primary goal:** Demonstrate production-level Android architecture knowledge — the kind that gets asked in interviews at Flipkart, Swiggy, and PhonePe.

**Secondary goals:**
- Show understanding of Jetpack component ecosystem
- Demonstrate ability to write testable, separated code
- Prove knowledge of background processing on Android
- Show UI craft through smooth Compose animations and charts

---

## 1.3 Target Users (for context — helps you explain the app in interviews)

- College students managing monthly pocket money
- Young professionals tracking salary spend
- Anyone who wants a simple, no-login, no-cloud expense tracker

---

## 1.4 Tech Stack (Non-negotiable — these are the resume keywords)

| Layer | Technology | Why it matters to HR |
|---|---|---|
| Language | Kotlin | Standard for Android roles |
| UI | Jetpack Compose | Modern Android — replaces XML |
| Architecture | MVVM | Industry standard pattern |
| Local DB | Room Database | Android-native ORM |
| Async | Kotlin Coroutines + Flow | Required for any Kotlin job |
| DI | Hilt | Dependency injection — senior signal |
| Background | WorkManager | Battery-safe background tasks |
| Charts | MPAndroidChart or Compose Canvas | Visual data skills |
| Navigation | Navigation Component | Jetpack standard |
| Testing | JUnit4 + MockK + Coroutines Test | Differentiator from freshers |
| CI | GitHub Actions | DevOps awareness |

---

## 1.5 Screens & Features

### Screen 1 — Dashboard (Home)

**What it shows:**
- Current month's total income (green) and total expense (red) at the top as two metric cards
- Net balance = Income minus Expense, shown large in the center with color coding (green if positive, red if negative)
- A horizontal scrollable row of category chips — Food, Transport, Shopping, Health, Entertainment, Other — tapping one filters the list below
- A lazy scrollable list of today's transactions with amount, category icon, and note
- A floating action button (FAB) at bottom right — "+" — opens the Add Transaction sheet

**Interactions:**
- Swipe left on any transaction row to reveal a Delete button
- Tap any transaction to open Edit Transaction sheet
- Tap the balance card to navigate to the Statistics screen

---

### Screen 2 — Add / Edit Transaction (Bottom Sheet)

**Fields:**
- Amount input — numeric keyboard, required
- Type toggle — Income / Expense (segmented control, not a dropdown)
- Category selector — horizontal scrollable icon row: Food, Transport, Shopping, Health, Entertainment, Salary, Other
- Date picker — defaults to today, tappable to open a date picker dialog
- Note field — optional free text, max 100 characters
- Save button

**Validation:**
- Amount cannot be zero or empty
- Category must be selected
- Show inline error messages, not Toasts

---

### Screen 3 — Statistics / Analytics

**What it shows:**
- Month selector at the top — left/right arrows to go back/forward months, shows "April 2026" style label
- A donut/pie chart showing expense breakdown by category (MPAndroidChart or Vico)
- Below the chart: a legend list showing each category, its color, amount, and percentage of total
- A bar chart below that shows daily spending for the selected month — X axis = days 1–31, Y axis = amount spent
- Two summary cards at the bottom: "Highest spend day" and "Most spent category"

**Interactions:**
- Tapping a pie slice highlights it and shows the category name + amount in the center hole
- Tapping a bar in the bar chart shows a tooltip with the date and amount

---

### Screen 4 — Budget Settings

**What it shows:**
- A list of categories, each with a monthly budget input field
- A toggle to enable/disable the daily notification
- A time picker to choose what time the daily summary notification fires
- A toggle for "Notify when I exceed budget in any category"

**Behavior:**
- Budget data is stored in DataStore Preferences (not Room — this is intentional and shows you know when to use each storage type)
- When a category's spending exceeds its budget, the category chip on Dashboard turns red

---

### Screen 5 — Transaction History

**What it shows:**
- Full list of all transactions, grouped by date (section headers like "Today", "Yesterday", "15 April 2026")
- Search bar at the top to filter by note text
- Filter icon opens a bottom sheet with: date range picker, category multi-select, type (Income/Expense/Both)
- Export button — generates a simple CSV file of filtered transactions and opens Android's share sheet

---

## 1.6 Data Models (Room Entities)

```kotlin
@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amount: Double,
    val type: TransactionType,   // enum: INCOME, EXPENSE
    val category: Category,      // enum: FOOD, TRANSPORT, etc.
    val note: String,
    val date: Long,              // epoch millis — always store dates as Long in Android
    val createdAt: Long
)

@Entity(tableName = "budgets")
data class Budget(
    @PrimaryKey val category: Category,
    val monthlyLimit: Double
)

// Not a Room entity — stored in DataStore
data class AppSettings(
    val notificationsEnabled: Boolean,
    val notificationHour: Int,
    val notificationMinute: Int
)
```

---

## 1.7 Repository Layer

```
TransactionRepository
  ├── insertTransaction(transaction: Transaction)
  ├── updateTransaction(transaction: Transaction)
  ├── deleteTransaction(id: Long)
  ├── getTransactionsByMonth(month: Int, year: Int): Flow<List<Transaction>>
  ├── getTransactionsByDateRange(start: Long, end: Long): Flow<List<Transaction>>
  ├── getCategoryTotals(month: Int, year: Int): Flow<List<CategoryTotal>>
  └── getDailyTotals(month: Int, year: Int): Flow<List<DailyTotal>>

BudgetRepository
  ├── getBudgets(): Flow<List<Budget>>
  ├── setBudget(category: Category, limit: Double)
  └── getExceededCategories(month: Int, year: Int): Flow<List<Category>>

SettingsRepository (DataStore)
  ├── getSettings(): Flow<AppSettings>
  └── updateSettings(settings: AppSettings)
```

---

## 1.8 ViewModel Layer

```
DashboardViewModel
  ├── uiState: StateFlow<DashboardUiState>
  ├── selectedCategory: StateFlow<Category?>
  ├── addTransaction(transaction: Transaction)
  ├── deleteTransaction(id: Long)
  └── filterByCategory(category: Category?)

StatisticsViewModel
  ├── selectedMonth: StateFlow<YearMonth>
  ├── categoryBreakdown: StateFlow<List<CategoryTotal>>
  ├── dailyTotals: StateFlow<List<DailyTotal>>
  └── navigateMonth(direction: Int)

BudgetViewModel
  ├── budgets: StateFlow<List<Budget>>
  ├── settings: StateFlow<AppSettings>
  ├── updateBudget(category, amount)
  └── updateSettings(settings: AppSettings)
```

---

## 1.9 WorkManager — Daily Notification

Create a `DailyBudgetWorker` that:
1. Queries today's total spending from Room
2. Checks which categories have exceeded their budget
3. Builds a notification: "Today you spent ₹847. Food budget exceeded by ₹200."
4. Posts it using NotificationCompat

```kotlin
class DailyBudgetWorker(
    context: Context,
    params: WorkerParameters,
    private val repository: TransactionRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val todayTotal = repository.getTodayTotal()
        val exceeded = repository.getExceededCategories()
        // Build and post notification
        return Result.success()
    }
}
```

Schedule it using `PeriodicWorkRequest` with a daily interval, set to fire at the user's chosen time using `setInitialDelay`.

---

## 1.10 JUnit Tests to Write (Minimum — this is what you mention in interviews)

```
TransactionRepositoryTest
  ✅ insertTransaction_savesCorrectly
  ✅ deleteTransaction_removesFromDb
  ✅ getTransactionsByMonth_returnsOnlyCorrectMonth
  ✅ getCategoryTotals_sumsCorrectly

DashboardViewModelTest
  ✅ initialState_isLoading
  ✅ addTransaction_updatesUiState
  ✅ filterByCategory_filtersCorrectly
  ✅ deleteTransaction_removesFromList

StatisticsViewModelTest
  ✅ navigateMonth_incrementsCorrectly
  ✅ navigateMonth_doesNotGoFuture
  ✅ categoryBreakdown_calculatesPercentages

BudgetViewModelTest
  ✅ exceededCategories_flaggedCorrectly
  ✅ setBudget_updatesRepository
```

Use `MockK` to mock the repository in ViewModel tests. Use an in-memory Room database for Repository tests.

---

## 1.11 GitHub Actions CI File

Create `.github/workflows/android.yml`:

```yaml
name: Android CI

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'
      - name: Run Unit Tests
        run: ./gradlew test
      - name: Upload Test Results
        uses: actions/upload-artifact@v3
        with:
          name: test-results
          path: app/build/reports/tests/
```

This adds a green checkmark badge to your repo. Add this to your README:  
`![CI](https://github.com/krit-vardhan-mishra/PocketLedger/actions/workflows/android.yml/badge.svg)`

---

## 1.12 Folder Structure

```
app/
└── src/
    └── main/
        └── java/com/kritvm/pocketledger/
            ├── data/
            │   ├── db/
            │   │   ├── AppDatabase.kt
            │   │   ├── dao/TransactionDao.kt
            │   │   └── dao/BudgetDao.kt
            │   ├── model/
            │   │   ├── Transaction.kt
            │   │   ├── Budget.kt
            │   │   └── enums/Category.kt
            │   └── repository/
            │       ├── TransactionRepository.kt
            │       └── BudgetRepository.kt
            ├── domain/
            │   └── usecase/
            │       ├── GetMonthlySummaryUseCase.kt
            │       └── CheckBudgetExceededUseCase.kt
            ├── ui/
            │   ├── dashboard/
            │   │   ├── DashboardScreen.kt
            │   │   └── DashboardViewModel.kt
            │   ├── statistics/
            │   │   ├── StatisticsScreen.kt
            │   │   └── StatisticsViewModel.kt
            │   ├── budget/
            │   │   ├── BudgetScreen.kt
            │   │   └── BudgetViewModel.kt
            │   ├── history/
            │   │   ├── HistoryScreen.kt
            │   │   └── HistoryViewModel.kt
            │   └── components/
            │       ├── TransactionCard.kt
            │       ├── CategoryChip.kt
            │       └── AddTransactionSheet.kt
            ├── worker/
            │   └── DailyBudgetWorker.kt
            └── di/
                └── AppModule.kt
```

---

## 1.13 Resume Bullet Points (copy these)

- Built offline-first personal finance tracker using Kotlin, Jetpack Compose, and Room Database with full MVVM architecture and Hilt dependency injection
- Implemented WorkManager-based daily budget notification system with user-configurable schedule and per-category budget alerts
- Achieved 85%+ ViewModel and Repository test coverage using JUnit4 and MockK; configured GitHub Actions CI to run tests on every push
- Developed interactive financial analytics with donut and bar charts using MPAndroidChart showing category breakdown and daily spending patterns

---

---
