package com.just_for_fun.pocketledger.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.just_for_fun.pocketledger.data.model.enums.Category
import com.just_for_fun.pocketledger.data.model.enums.displayName
import com.just_for_fun.pocketledger.temp_data.PocketLedgerTempData
import com.just_for_fun.pocketledger.ui.components.CategoryChip
import com.just_for_fun.pocketledger.ui.components.MetricCard
import com.just_for_fun.pocketledger.ui.components.TransactionCard
import com.just_for_fun.pocketledger.ui.components.iconForCategory
import com.just_for_fun.pocketledger.ui.settings.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToStatistics: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val settingsViewModel: SettingsViewModel = hiltViewModel()
    val appSettings by settingsViewModel.settings.collectAsState()

    val summary by viewModel.monthlySummary.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val transactions by viewModel.filteredTransactions.collectAsState()
    val exceededCategories by viewModel.exceededCategories.collectAsState()

    val useTempData = appSettings.useTempData
    val displaySummary = if (useTempData) PocketLedgerTempData.monthlySummary else summary
    val displayTransactions = if (useTempData) {
        PocketLedgerTempData.todayTransactions(selectedCategory)
    } else {
        transactions
    }
    val displayExceededCategories = if (useTempData) {
        PocketLedgerTempData.exceededCategories
    } else {
        exceededCategories
    }

    var showSheet by remember { mutableStateOf(false) }
    var editingTransactionId by remember { mutableStateOf<Long?>(null) }
    val editingTransaction = remember(displayTransactions, editingTransactionId) {
        displayTransactions.firstOrNull { it.id == editingTransactionId }
    }

    val monthlyIncome  = displaySummary?.totalIncome ?: 0.0
    val monthlyExpense = displaySummary?.totalExpense ?: 0.0
    val netBalance     = displaySummary?.balance ?: 0.0

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "PocketLedger",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Monthly Overview",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            if (!useTempData) {
                ExtendedFloatingActionButton(
                    onClick = {
                        editingTransactionId = null
                        showSheet = true
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor   = MaterialTheme.colorScheme.onPrimary,
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("Add", fontWeight = FontWeight.SemiBold) }
                )
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ── Hero Balance Card ─────────────────────────────────────────────
            item {
                Spacer(modifier = Modifier.height(4.dp))
                ElevatedCard(
                    onClick = onNavigateToStatistics,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 20.dp)
                    ) {
                        Text(
                            text = "Net Balance",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "₹${"%.2f".format(netBalance)}",
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (netBalance >= 0)
                                MaterialTheme.colorScheme.onPrimaryContainer
                            else
                                MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tap to view analytics →",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f)
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.12f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            MetricCard(
                                title    = "Income",
                                amount   = monthlyIncome,
                                isIncome = true,
                                modifier = Modifier.weight(1f)
                            )
                            MetricCard(
                                title    = "Expense",
                                amount   = monthlyExpense,
                                isIncome = false,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // ── Category filter ───────────────────────────────────────────────
            item {
                Text(
                    text = "Filter by Category",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = selectedCategory == null,
                            onClick  = { viewModel.filterByCategory(null) },
                            label    = {
                                Text(
                                    "All",
                                    fontWeight = if (selectedCategory == null)
                                        FontWeight.SemiBold else FontWeight.Normal
                                )
                            },
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                    items(Category.entries.toTypedArray()) { category ->
                        CategoryChip(
                            category           = category,
                            isSelected         = selectedCategory == category,
                            isAlert            = category in displayExceededCategories,
                            onCategorySelected = { viewModel.filterByCategory(category) }
                        )
                    }
                }
            }

            // ── Today's Transactions header ───────────────────────────────────
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Today's Transactions",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    if (displayTransactions.isNotEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Text(
                                text = "${displayTransactions.size}",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }
            }

            // ── Empty state ───────────────────────────────────────────────────
            if (displayTransactions.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "No transactions yet",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Tap + to record your first entry",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }

            items(displayTransactions, key = { it.id }) { transaction ->
                TransactionCard(
                    amount       = transaction.amount,
                    type         = transaction.type,
                    categoryName = transaction.category.displayName(),
                    note         = transaction.note,
                    icon         = iconForCategory(transaction.category),
                    onDelete     = {
                        if (!useTempData) {
                            viewModel.deleteTransaction(transaction.id)
                        }
                    },
                    onClick      = {
                        if (!useTempData) {
                            editingTransactionId = transaction.id
                            showSheet = true
                        }
                    }
                )
            }

            item { Spacer(modifier = Modifier.height(88.dp)) }
        }
    }

    if (showSheet && !useTempData) {
        AddTransactionSheet(
            initialTransaction = editingTransaction,
            onDismissRequest   = {
                showSheet = false
                editingTransactionId = null
            },
            onSaveTransaction  = { formData ->
                viewModel.saveTransaction(
                    id       = formData.id,
                    amount   = formData.amount,
                    type     = formData.type,
                    category = formData.category,
                    note     = formData.note,
                    date     = formData.dateMillis
                )
                showSheet = false
                editingTransactionId = null
            }
        )
    }
}