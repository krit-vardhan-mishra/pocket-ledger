package com.just_for_fun.pocketledger.ui.budget

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.just_for_fun.pocketledger.data.model.enums.Category

import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetScreen(
    viewModel: BudgetViewModel = hiltViewModel()
) {
    var notificationsEnabled by remember { mutableStateOf(true) }
    var exceedAlertsEnabled by remember { mutableStateOf(true) }
    
    val budgets by viewModel.budgets.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Budget Settings") })
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            item {
                Text(
                    text = "Notification Preferences",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }
            
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Daily Summary", fontWeight = FontWeight.SemiBold)
                        Text("Receive a daily digest of your spending", style = MaterialTheme.typography.bodySmall)
                    }
                    Switch(checked = notificationsEnabled, onCheckedChange = { notificationsEnabled = it })
                }
            }
            
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Exceed Alerts", fontWeight = FontWeight.SemiBold)
                        Text("Notify when a category limit is breached", style = MaterialTheme.typography.bodySmall)
                    }
                    Switch(checked = exceedAlertsEnabled, onCheckedChange = { exceedAlertsEnabled = it })
                }
            }

            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                Text(
                    text = "Monthly Built Limits",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            items(Category.entries.toTypedArray()) { category ->
                val budget = budgets.find { it.category == category }
                var limitText by remember(budget) { mutableStateOf(budget?.monthlyLimit?.toString() ?: "0") }
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = category.name, modifier = Modifier.weight(1f))
                    OutlinedTextField(
                        value = limitText,
                        onValueChange = { 
                            limitText = it
                            val newLimit = it.toDoubleOrNull() ?: 0.0
                            viewModel.setBudget(category, newLimit)
                        },
                        modifier = Modifier.width(150.dp),
                        leadingIcon = { Text("₹") },
                        singleLine = true
                    )
                }
            }
            
            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}
