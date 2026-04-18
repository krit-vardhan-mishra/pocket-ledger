package com.just_for_fun.pocketledger.ui.budget

import android.app.TimePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.just_for_fun.pocketledger.data.model.enums.Category
import com.just_for_fun.pocketledger.data.model.enums.displayName
import androidx.hilt.navigation.compose.hiltViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetScreen(
    viewModel: BudgetViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val budgets by viewModel.budgets.collectAsState()
    val settings by viewModel.settings.collectAsState()

    val formattedTime = remember(settings.notificationHour, settings.notificationMinute) {
        String.format(Locale.getDefault(), "%02d:%02d", settings.notificationHour, settings.notificationMinute)
    }

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
                    Switch(
                        checked = settings.notificationsEnabled,
                        onCheckedChange = viewModel::updateNotificationsEnabled
                    )
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
                        Text("Notification Time", fontWeight = FontWeight.SemiBold)
                        Text("Current time: $formattedTime", style = MaterialTheme.typography.bodySmall)
                    }
                    OutlinedButton(
                        onClick = {
                            TimePickerDialog(
                                context,
                                { _, hour, minute ->
                                    viewModel.updateNotificationTime(hour, minute)
                                },
                                settings.notificationHour,
                                settings.notificationMinute,
                                true
                            ).show()
                        }
                    ) {
                        Text("Set Time")
                    }
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
                    Switch(
                        checked = settings.exceedAlertsEnabled,
                        onCheckedChange = viewModel::updateExceedAlertsEnabled
                    )
                }
            }

            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                Text(
                    text = "Monthly Budget Limits",
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
                    Text(text = category.displayName(), modifier = Modifier.weight(1f))
                    OutlinedTextField(
                        value = limitText,
                        onValueChange = { 
                            limitText = it
                            val newLimit = it.toDoubleOrNull() ?: 0.0
                            viewModel.setBudget(category, newLimit)
                        },
                        modifier = Modifier.width(150.dp),
                        leadingIcon = { Text("₹") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true
                    )
                }
            }
            
            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}
