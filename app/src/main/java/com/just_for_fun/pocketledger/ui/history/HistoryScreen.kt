package com.just_for_fun.pocketledger.ui.history

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.just_for_fun.pocketledger.data.model.enums.Category
import com.just_for_fun.pocketledger.ui.components.TransactionCard
import com.just_for_fun.pocketledger.data.model.enums.TransactionType as DomainTransactionType
import com.just_for_fun.pocketledger.data.model.enums.TransactionType as UiTransactionType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val transactions by viewModel.transactions.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    
    val filteredTransactions = if (searchQuery.isBlank()) {
        transactions
    } else {
        transactions.filter { it.note.contains(searchQuery, ignoreCase = true) }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Transaction History") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Search transactions...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true
            )
            
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (filteredTransactions.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No transactions found", style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                } else {
                    items(filteredTransactions) { transaction ->
                        TransactionCard(
                            amount = transaction.amount,
                            type = if (transaction.type == DomainTransactionType.INCOME) UiTransactionType.INCOME else UiTransactionType.EXPENSE,
                            categoryName = transaction.category.name,
                            note = transaction.note,
                            icon = getIconForCategory(transaction.category),
                            onDelete = { /* TODO */ },
                            onClick = { /* TODO */ }
                        )
                    }
                }
            }
        }
    }
}

private fun getIconForCategory(category: Category) = when (category) {
    Category.FOOD -> Icons.Default.Restaurant
    Category.TRANSPORT -> Icons.Default.DirectionsCar
    Category.SHOPPING -> Icons.Default.ShoppingCart
    Category.BILLS -> Icons.Default.Receipt
    Category.ENTERTAINMENT -> Icons.Default.Movie
    Category.HEALTH -> Icons.Default.MedicalServices
    Category.EDUCATION -> Icons.Default.School
    Category.SALARY -> Icons.Default.Payments
    Category.OTHER -> Icons.Default.Category
}
