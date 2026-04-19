package com.just_for_fun.pocketledger.ui.budget

import android.app.TimePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.just_for_fun.pocketledger.data.model.AppThemeMode
import com.just_for_fun.pocketledger.data.model.enums.Category
import com.just_for_fun.pocketledger.data.model.enums.displayName
import com.just_for_fun.pocketledger.temp_data.PocketLedgerTempData
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetScreen(
    viewModel: BudgetViewModel = hiltViewModel()
) {
    val context  = LocalContext.current
    val budgets  by viewModel.budgets.collectAsState()
    val settings by viewModel.settings.collectAsState()

    val formattedTime = remember(settings.notificationHour, settings.notificationMinute) {
        String.format(Locale.getDefault(), "%02d:%02d", settings.notificationHour, settings.notificationMinute)
    }
    val visibleBudgets = remember(budgets, settings.useTempData) {
        if (settings.useTempData) PocketLedgerTempData.budgets else budgets
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Budget Settings",
                        fontWeight = FontWeight.ExtraBold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ── Notification settings card ────────────────────────────────────
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Notification Preferences",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            item {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(modifier = Modifier.padding(4.dp)) {

                        // Daily Summary toggle
                        ListItem(
                            headlineContent = {
                                Text(
                                    "Daily Summary",
                                    fontWeight = FontWeight.Medium
                                )
                            },
                            supportingContent = {
                                Text(
                                    "Receive a daily digest of your spending",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            },
                            leadingContent = {
                                Icon(
                                    imageVector = if (settings.notificationsEnabled)
                                        Icons.Default.Notifications
                                    else
                                        Icons.Default.NotificationsOff,
                                    contentDescription = null,
                                    tint = if (settings.notificationsEnabled)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            trailingContent = {
                                Switch(
                                    checked       = settings.notificationsEnabled,
                                    onCheckedChange = viewModel::updateNotificationsEnabled
                                )
                            }
                        )

                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color    = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )

                        // Notification time row
                        ListItem(
                            headlineContent = {
                                Text(
                                    "Notification Time",
                                    fontWeight = FontWeight.Medium
                                )
                            },
                            supportingContent = {
                                Text(
                                    "Currently set to $formattedTime",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            },
                            leadingContent = {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            trailingContent = {
                                FilledTonalButton(
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
                                    },
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Set Time")
                                }
                            }
                        )

                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color    = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )

                        // Exceed alerts toggle
                        ListItem(
                            headlineContent = {
                                Text(
                                    "Exceed Alerts",
                                    fontWeight = FontWeight.Medium
                                )
                            },
                            supportingContent = {
                                Text(
                                    "Notify when a category limit is breached",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            },
                            leadingContent = {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = if (settings.exceedAlertsEnabled)
                                        MaterialTheme.colorScheme.error
                                    else
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            trailingContent = {
                                Switch(
                                    checked       = settings.exceedAlertsEnabled,
                                    onCheckedChange = viewModel::updateExceedAlertsEnabled
                                )
                            }
                        )

                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )

                        ListItem(
                            headlineContent = {
                                Text(
                                    "Use Temp Data",
                                    fontWeight = FontWeight.Medium
                                )
                            },
                            supportingContent = {
                                Text(
                                    "Show curated demo data for app screenshots",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            },
                            leadingContent = {
                                Icon(
                                    imageVector = if (settings.useTempData)
                                        Icons.Default.Notifications
                                    else
                                        Icons.Default.NotificationsOff,
                                    contentDescription = null,
                                    tint = if (settings.useTempData)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            trailingContent = {
                                Switch(
                                    checked = settings.useTempData,
                                    onCheckedChange = viewModel::updateUseTempData
                                )
                            }
                        )

                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )

                        ListItem(
                            headlineContent = {
                                Text(
                                    "Theme Mode",
                                    fontWeight = FontWeight.Medium
                                )
                            },
                            supportingContent = {
                                Text(
                                    "Choose how the app theme is applied",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        )

                        SingleChoiceSegmentedButtonRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            SegmentedButton(
                                selected = settings.themeMode == AppThemeMode.DEFAULT,
                                onClick = { viewModel.updateThemeMode(AppThemeMode.DEFAULT) },
                                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3)
                            ) {
                                Text("Default")
                            }
                            SegmentedButton(
                                selected = settings.themeMode == AppThemeMode.DARK,
                                onClick = { viewModel.updateThemeMode(AppThemeMode.DARK) },
                                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3)
                            ) {
                                Text("Dark")
                            }
                            SegmentedButton(
                                selected = settings.themeMode == AppThemeMode.LIGHT,
                                onClick = { viewModel.updateThemeMode(AppThemeMode.LIGHT) },
                                shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3)
                            ) {
                                Text("Light")
                            }
                        }
                    }
                }
            }

            // ── Budget limits section ─────────────────────────────────────────
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Monthly Budget Limits",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = "Set ₹0 to disable the limit for that category",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            item {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(modifier = Modifier.padding(4.dp)) {
                        Category.entries.toTypedArray().forEachIndexed { index, category ->
                            val budget = visibleBudgets.find { it.category == category }
                            var limitText by remember(budget) {
                                mutableStateOf(budget?.monthlyLimit?.toString() ?: "0")
                            }

                            ListItem(
                                headlineContent = {
                                    Text(
                                        category.displayName(),
                                        fontWeight = FontWeight.Medium
                                    )
                                },
                                trailingContent = {
                                    OutlinedTextField(
                                        value = limitText,
                                        onValueChange = {
                                            limitText = it
                                            if (!settings.useTempData) {
                                                viewModel.setBudget(category, it.toDoubleOrNull() ?: 0.0)
                                            }
                                        },
                                        modifier = Modifier.width(130.dp),
                                        enabled = !settings.useTempData,
                                        leadingIcon = {
                                            Text(
                                                "₹",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Medium
                                            )
                                        },
                                        keyboardOptions = KeyboardOptions(
                                            keyboardType = KeyboardType.Decimal
                                        ),
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp),
                                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    )
                                }
                            )

                            if (index < Category.entries.size - 1) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    color    = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                                )
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}