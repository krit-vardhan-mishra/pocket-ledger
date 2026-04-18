package com.just_for_fun.pocketledger.ui.statistics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.just_for_fun.pocketledger.data.model.CategoryTotal
import com.just_for_fun.pocketledger.data.model.DailyTotal
import com.just_for_fun.pocketledger.data.model.enums.displayName
import com.just_for_fun.pocketledger.ui.components.DailyBarChart
import com.just_for_fun.pocketledger.ui.components.ExpenseDonutChart
import com.just_for_fun.pocketledger.ui.components.colorForChartIndex

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    viewModel: StatisticsViewModel = hiltViewModel()
) {
    val summary by viewModel.monthlySummary.collectAsState()
    val breakdown by viewModel.categoryBreakdown.collectAsState()
    val dailyTotals by viewModel.dailyTotals.collectAsState()
    val highestSpendDay by viewModel.highestSpendDay.collectAsState()
    val mostSpentCategory by viewModel.mostSpentCategory.collectAsState()
    val canNavigateNext by viewModel.canNavigateNext.collectAsState()

    var selectedSlice by remember { mutableStateOf<CategoryTotal?>(null) }
    var selectedDay by remember { mutableStateOf<DailyTotal?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Analytics") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.previousPage() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous Month")
                }
                Text(
                    text = viewModel.getFormattedDate(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                IconButton(
                    onClick = { viewModel.nextPage() },
                    enabled = canNavigateNext
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Month")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (breakdown.isNotEmpty()) {
                ExpenseDonutChart(
                    data = breakdown,
                    selectedCategory = selectedSlice,
                    onSliceSelected = { selectedSlice = it },
                    modifier = Modifier.size(240.dp)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No expense data")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Column(modifier = Modifier.fillMaxWidth()) {
                if (breakdown.isEmpty()) {
                    Text(
                        text = "No data for this month",
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    breakdown.take(6).forEachIndexed { index, item ->
                        LegendItem(
                            name = item.category.displayName(),
                            amount = "₹${"%.2f".format(item.amount)}",
                            percentage = "${item.percentage.toInt()}%",
                            color = colorForChartIndex(index)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Daily Spending",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.fillMaxWidth()
            )

            DailyBarChart(
                totals = dailyTotals,
                daysInMonth = viewModel.getDaysInSelectedMonth(),
                selectedDay = selectedDay,
                onBarSelected = { selectedDay = it },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Card(modifier = Modifier.weight(1f)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Highest Spend Day", style = MaterialTheme.typography.labelMedium)
                        Text(
                            text = highestSpendDay?.dayOfMonth?.toString() ?: "N/A",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "₹${"%.0f".format(highestSpendDay?.amount ?: 0.0)}",
                            color = Color(0xFFF44336),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Card(modifier = Modifier.weight(1f)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Top Category", style = MaterialTheme.typography.labelMedium)
                        Text(
                            text = mostSpentCategory?.category?.displayName() ?: "N/A",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "₹${"%.0f".format(mostSpentCategory?.amount ?: 0.0)}",
                            color = Color(0xFFF44336),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Monthly Overview", style = MaterialTheme.typography.labelMedium)
                    Text(
                        text = "Income: ₹${"%.0f".format(summary?.totalIncome ?: 0.0)}",
                        color = Color(0xFF4CAF50),
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Expense: ₹${"%.0f".format(summary?.totalExpense ?: 0.0)}",
                        color = Color(0xFFF44336),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun LegendItem(name: String, amount: String, percentage: String, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(name, style = MaterialTheme.typography.bodyLarge)
        }
        Row {
            Text(percentage, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.width(16.dp))
            Text(amount, fontWeight = FontWeight.Bold)
        }
    }
}
