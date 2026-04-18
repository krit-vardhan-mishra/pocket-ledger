package com.just_for_fun.pocketledger.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.just_for_fun.pocketledger.data.model.enums.Category
import com.just_for_fun.pocketledger.data.model.enums.TransactionType
import com.just_for_fun.pocketledger.data.model.enums.displayName
import com.just_for_fun.pocketledger.ui.components.CategoryChip
import com.just_for_fun.pocketledger.ui.components.MetricCard
import com.just_for_fun.pocketledger.ui.components.TransactionCard
import com.just_for_fun.pocketledger.ui.components.iconForCategory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToStatistics: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val summary by viewModel.monthlySummary.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val transactions by viewModel.filteredTransactions.collectAsState()
    val exceededCategories by viewModel.exceededCategories.collectAsState()

    var showSheet by remember { mutableStateOf(false) }
    var editingTransactionId by remember { mutableStateOf<Long?>(null) }
    val editingTransaction = remember(transactions, editingTransactionId) {
        transactions.firstOrNull { it.id == editingTransactionId }
    }

    val monthlyIncome = summary?.totalIncome ?: 0.0
    val monthlyExpense = summary?.totalExpense ?: 0.0
    val netBalance = summary?.balance ?: 0.0

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("PocketLedger", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingTransactionId = null
                    showSheet = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Transaction")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    MetricCard(
                        title = "Income",
                        amount = monthlyIncome,
                        isIncome = true,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Expense",
                        amount = monthlyExpense,
                        isIncome = false,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            item {
                Card(
                    onClick = onNavigateToStatistics,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Net Balance", 
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "₹${"%.2f".format(netBalance)}",
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold,
                            color = if (netBalance >= 0) Color(0xFF4CAF50) else Color(0xFFF44336)
                        )
                    }
                }
            }
            
            item {
                Text(
                    text = "Categories",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 8.dp)
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = selectedCategory == null,
                            onClick = { viewModel.filterByCategory(null) },
                            label = { Text("All Categories") }
                        )
                    }
                    items(Category.entries.toTypedArray()) { category ->
                        CategoryChip(
                            category = category,
                            isSelected = selectedCategory == category,
                            isAlert = category in exceededCategories,
                            onCategorySelected = { viewModel.filterByCategory(category) }
                        )
                    }
                }
            }
            
            item {
                Text(
                    text = "Today's Transactions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )
            }
            
            if (transactions.isEmpty()) {
                item {
                    Text(
                        text = "No transactions for today",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            items(transactions) { transaction ->
                TransactionCard(
                    amount = transaction.amount,
                    type = transaction.type,
                    categoryName = transaction.category.displayName(),
                    note = transaction.note,
                    icon = iconForCategory(transaction.category),
                    onDelete = { viewModel.deleteTransaction(transaction.id) },
                    onClick = {
                        editingTransactionId = transaction.id
                        showSheet = true
                    }
                )
            }
            
            item {
                Spacer(modifier = Modifier.height(80.dp)) // Padding for FAB
            }
        }
    }

    if (showSheet) {
        AddTransactionSheet(
            initialTransaction = editingTransaction,
            onDismissRequest = {
                showSheet = false
                editingTransactionId = null
            },
            onSaveTransaction = { formData ->
                viewModel.saveTransaction(
                    id = formData.id,
                    amount = formData.amount,
                    type = formData.type,
                    category = formData.category,
                    note = formData.note,
                    date = formData.dateMillis
                )
                showSheet = false
                editingTransactionId = null
            }
        )
    }
}
