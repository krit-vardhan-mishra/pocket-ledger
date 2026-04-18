package com.just_for_fun.pocketledger.ui.statistics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    viewModel: StatisticsViewModel = hiltViewModel()
) {
    val summary by viewModel.monthlySummary.collectAsState()
    val breakdown by viewModel.categoryBreakdown.collectAsState()

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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Month Selector
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
                IconButton(onClick = { viewModel.nextPage() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Month")
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Placeholder for Donut Chart
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Total Expense", style = MaterialTheme.typography.labelLarge)
                    Text(
                        text = "₹${"%.2f".format(summary?.totalExpense ?: 0.0)}", 
                        style = MaterialTheme.typography.headlineSmall, 
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Column(modifier = Modifier.fillMaxWidth()) {
                if (breakdown.isEmpty()) {
                    Text(
                        "No data for this month", 
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    breakdown.take(5).forEachIndexed { index, item ->
                        val color = when (index % 4) {
                            0 -> Color(0xFFE91E63)
                            1 -> Color(0xFF2196F3)
                            2 -> Color(0xFF4CAF50)
                            else -> Color(0xFFFFC107)
                        }
                        LegendItem(item.category, "₹${"%.2f".format(item.amount)}", "${item.percentage.toInt()}%", color)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Summary Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Card(modifier = Modifier.weight(1f)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Income", style = MaterialTheme.typography.labelMedium)
                        Text(
                            text = "₹${"%.0f".format(summary?.totalIncome ?: 0.0)}", 
                            style = MaterialTheme.typography.titleMedium, 
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF4CAF50)
                        )
                    }
                }
                Card(modifier = Modifier.weight(1f)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Top Category", style = MaterialTheme.typography.labelMedium)
                        Text(
                            text = breakdown.firstOrNull()?.category ?: "N/A", 
                            style = MaterialTheme.typography.titleMedium, 
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "₹${"%.0f".format(breakdown.firstOrNull()?.amount ?: 0.0)}", 
                            color = Color(0xFFF44336), 
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
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
