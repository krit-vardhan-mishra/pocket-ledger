package com.just_for_fun.pocketledger.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.just_for_fun.pocketledger.data.model.enums.Category
import com.just_for_fun.pocketledger.data.model.enums.TransactionType
import com.just_for_fun.pocketledger.ui.components.CategoryChip
import com.just_for_fun.pocketledger.ui.components.MetricCard
import com.just_for_fun.pocketledger.ui.components.TransactionCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToStatistics: () -> Unit,
    onAddTransactionClick: () -> Unit
) {
    // Mock Data for the UI
    val monthlyIncome = 25000.0
    val monthlyExpense = 8450.0
    val netBalance = monthlyIncome - monthlyExpense
    
    var selectedCategory by remember { mutableStateOf<Category?>(null) }
    
    val mockTransactions = listOf(
        MockTransaction(1200.0, TransactionType.EXPENSE, Category.FOOD, "Dinner at restaurant", Icons.Default.Restaurant),
        MockTransaction(500.0, TransactionType.EXPENSE, Category.TRANSPORT, "Uber to office", Icons.Default.DirectionsCar),
        MockTransaction(25000.0, TransactionType.INCOME, Category.SALARY, "April Salary", Icons.Default.AttachMoney),
        MockTransaction(1500.0, TransactionType.EXPENSE, Category.SHOPPING, "Groceries", Icons.Default.ShoppingCart)
    ).filter { selectedCategory == null || it.category == selectedCategory }

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
                onClick = onAddTransactionClick,
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
                        CategoryChip(
                            category = Category.OTHER, // Using OTHER as placeholder for "All" in UI
                            isSelected = selectedCategory == null,
                            onCategorySelected = { selectedCategory = null }
                        )
                    }
                    items(Category.entries.toTypedArray()) { category ->
                        CategoryChip(
                            category = category,
                            isSelected = selectedCategory == category,
                            onCategorySelected = { selectedCategory = category }
                        )
                    }
                }
            }
            
            item {
                Text(
                    text = "Recent Transactions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )
            }
            
            items(mockTransactions) { transaction ->
                TransactionCard(
                    amount = transaction.amount,
                    type = transaction.type,
                    categoryName = transaction.category.name,
                    note = transaction.note,
                    icon = transaction.icon,
                    onDelete = { /* TODO */ },
                    onClick = { /* TODO */ }
                )
            }
            
            item {
                Spacer(modifier = Modifier.height(80.dp)) // Padding for FAB
            }
        }
    }
}

// Temporary data class for UI scaffolding
data class MockTransaction(
    val amount: Double,
    val type: TransactionType,
    val category: Category,
    val note: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)
