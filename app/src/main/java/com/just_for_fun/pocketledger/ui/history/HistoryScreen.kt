package com.just_for_fun.pocketledger.ui.history

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import com.just_for_fun.pocketledger.data.model.Transaction
import com.just_for_fun.pocketledger.data.model.enums.Category
import com.just_for_fun.pocketledger.data.model.enums.TransactionType
import com.just_for_fun.pocketledger.data.model.enums.displayName
import com.just_for_fun.pocketledger.temp_data.PocketLedgerTempData
import com.just_for_fun.pocketledger.ui.components.TransactionCard
import com.just_for_fun.pocketledger.ui.components.iconForCategory
import com.just_for_fun.pocketledger.ui.dashboard.AddTransactionSheet
import com.just_for_fun.pocketledger.ui.settings.SettingsViewModel
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val settingsViewModel: SettingsViewModel = hiltViewModel()
    val appSettings by settingsViewModel.settings.collectAsState()

    val context = androidx.compose.ui.platform.LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val transactions        by viewModel.transactions.collectAsState()
    val groupedTransactions by viewModel.groupedTransactions.collectAsState()
    val filteredTransactions by viewModel.filteredTransactions.collectAsState()
    val searchQuery         by viewModel.searchQuery.collectAsState()
    val startDateMillis     by viewModel.startDateMillis.collectAsState()
    val endDateMillis       by viewModel.endDateMillis.collectAsState()
    val typeFilter          by viewModel.typeFilter.collectAsState()
    val selectedCategories  by viewModel.selectedCategories.collectAsState()

    val useTempData = appSettings.useTempData
    val displayedTransactions = if (useTempData) PocketLedgerTempData.historyTransactions else transactions
    val displayedFilteredTransactions = if (useTempData) {
        applyHistoryFilters(
            transactions = displayedTransactions,
            query = searchQuery,
            startMillis = startDateMillis,
            endMillis = endDateMillis,
            typeFilter = typeFilter,
            categories = selectedCategories
        )
    } else {
        filteredTransactions
    }
    val displayedGroupedTransactions = if (useTempData) {
        groupTransactionsByDay(displayedFilteredTransactions)
    } else {
        groupedTransactions
    }

    var showFilterSheet by remember { mutableStateOf(false) }
    var showEditSheet   by remember { mutableStateOf(false) }
    var editingTransactionId by remember { mutableStateOf<Long?>(null) }

    val editingTransaction = remember(displayedTransactions, editingTransactionId) {
        displayedTransactions.firstOrNull { it.id == editingTransactionId }
    }

    val hasActiveFilters = startDateMillis != null || endDateMillis != null ||
            typeFilter != HistoryTypeFilter.ALL || selectedCategories.isNotEmpty()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Transaction History",
                        fontWeight = FontWeight.ExtraBold
                    )
                },
                actions = {
                    // Filter button with active-state badge
                    BadgedBox(
                        badge = {
                            if (hasActiveFilters) {
                                Badge(containerColor = MaterialTheme.colorScheme.primary)
                            }
                        }
                    ) {
                        IconButton(onClick = { showFilterSheet = true }) {
                            Icon(Icons.Default.FilterList, contentDescription = "Filters")
                        }
                    }
                    IconButton(
                        onClick = {
                            val result = exportTransactionsCsv(context, displayedFilteredTransactions)
                            coroutineScope.launch {
                                if (result.isFailure) {
                                    snackbarHostState.showSnackbar(
                                        result.exceptionOrNull()?.message ?: "Failed to export CSV"
                                    )
                                } else {
                                    snackbarHostState.showSnackbar("CSV export is ready to share")
                                }
                            }
                        }
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Export CSV")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // ── Search bar ────────────────────────────────────────────────────
            OutlinedTextField(
                value = searchQuery,
                onValueChange = viewModel::updateSearchQuery,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = {
                    Text(
                        "Search transactions…",
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp)
            )

            // ── Transaction list ──────────────────────────────────────────────
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                // contentPadding = PaddingValues(horizontal = 16.dp, bottom = 24.dp),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (displayedFilteredTransactions.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillParentMaxSize()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.SearchOff,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    "No transactions found",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    "Try adjusting your filters",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                } else {
                    displayedGroupedTransactions.forEach { group ->
                        item(key = "header_${group.dayStartMillis}") {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 16.dp, bottom = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = humanReadableDayLabel(group.dayStartMillis),
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "${group.transactions.size} item${if (group.transactions.size != 1) "s" else ""}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            )
                        }

                        items(group.transactions, key = { it.id }) { transaction ->
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
                                        showEditSheet = true
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // ── Filter bottom sheet ───────────────────────────────────────────────────
    if (showFilterSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFilterSheet = false },
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            tonalElevation = 3.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp)
            ) {
                Text(
                    "Filters",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(20.dp))

                // Date range
                Text(
                    "Date Range",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        onClick = {
                            showDatePicker(context, startDateMillis) { picked ->
                                viewModel.updateStartDateMillis(startOfDay(picked))
                            }
                        }
                    ) {
                        Icon(
                            Icons.Default.CalendarMonth,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(startDateMillis?.let { "From: ${formatDate(it)}" } ?: "From Date")
                    }
                    OutlinedButton(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        onClick = {
                            showDatePicker(context, endDateMillis) { picked ->
                                viewModel.updateEndDateMillis(endOfDay(picked))
                            }
                        }
                    ) {
                        Icon(
                            Icons.Default.CalendarMonth,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(endDateMillis?.let { "To: ${formatDate(it)}" } ?: "To Date")
                    }
                }
                if (startDateMillis != null || endDateMillis != null) {
                    TextButton(onClick = { viewModel.clearDateRange() }) {
                        Icon(
                            Icons.Default.Clear,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Clear Date Range")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Type filter
                Text(
                    "Transaction Type",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    SegmentedButton(
                        selected = typeFilter == HistoryTypeFilter.ALL,
                        onClick  = { viewModel.updateTypeFilter(HistoryTypeFilter.ALL) },
                        shape    = SegmentedButtonDefaults.itemShape(index = 0, count = 3)
                    ) { Text("Both") }
                    SegmentedButton(
                        selected = typeFilter == HistoryTypeFilter.INCOME,
                        onClick  = { viewModel.updateTypeFilter(HistoryTypeFilter.INCOME) },
                        shape    = SegmentedButtonDefaults.itemShape(index = 1, count = 3)
                    ) { Text("Income") }
                    SegmentedButton(
                        selected = typeFilter == HistoryTypeFilter.EXPENSE,
                        onClick  = { viewModel.updateTypeFilter(HistoryTypeFilter.EXPENSE) },
                        shape    = SegmentedButtonDefaults.itemShape(index = 2, count = 3)
                    ) { Text("Expense") }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Category filter
                Text(
                    "Categories",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(Category.entries.toTypedArray()) { category ->
                        FilterChip(
                            selected = category in selectedCategories,
                            onClick  = { viewModel.toggleCategory(category) },
                            label    = { Text(category.displayName()) },
                            shape    = RoundedCornerShape(12.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        onClick = {
                            viewModel.clearFilters()
                            showFilterSheet = false
                        }
                    ) { Text("Clear All") }
                    Button(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        onClick = { showFilterSheet = false }
                    ) { Text("Apply Filters", fontWeight = FontWeight.SemiBold) }
                }
            }
        }
    }

    // ── Edit sheet ────────────────────────────────────────────────────────────
    if (showEditSheet && !useTempData) {
        AddTransactionSheet(
            initialTransaction = editingTransaction,
            onDismissRequest   = {
                showEditSheet = false
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
                showEditSheet = false
                editingTransactionId = null
            }
        )
    }
}

private fun applyHistoryFilters(
    transactions: List<Transaction>,
    query: String,
    startMillis: Long?,
    endMillis: Long?,
    typeFilter: HistoryTypeFilter,
    categories: Set<Category>
): List<Transaction> {
    return transactions.filter { transaction ->
        val matchesSearch = query.isBlank() || transaction.note.contains(query, ignoreCase = true)
        val matchesType = when (typeFilter) {
            HistoryTypeFilter.ALL -> true
            HistoryTypeFilter.INCOME -> transaction.type == TransactionType.INCOME
            HistoryTypeFilter.EXPENSE -> transaction.type == TransactionType.EXPENSE
        }
        val matchesCategory = categories.isEmpty() || transaction.category in categories
        val matchesStart = startMillis == null || transaction.date >= startMillis
        val matchesEnd = endMillis == null || transaction.date <= endMillis
        matchesSearch && matchesType && matchesCategory && matchesStart && matchesEnd
    }
}

private fun groupTransactionsByDay(transactions: List<Transaction>): List<HistoryDayGroup> {
    return transactions
        .sortedByDescending { it.date }
        .groupBy { startOfDay(it.date) }
        .toSortedMap(compareByDescending { it })
        .map { (dayStartMillis, items) ->
            HistoryDayGroup(
                dayStartMillis = dayStartMillis,
                transactions = items
            )
        }
}

private fun exportTransactionsCsv(context: Context, transactions: List<Transaction>): Result<Unit> {
    return runCatching {
        val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val fileName = "transactions_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.csv"
        val file = File(exportDir, fileName)

        file.bufferedWriter().use { writer ->
            writer.appendLine("id,date,type,category,amount,note")
            transactions.sortedByDescending { it.date }.forEach { transaction ->
                val formattedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(transaction.date))
                val escapedNote = transaction.note.replace("\"", "\"\"")
                writer.appendLine(
                    "${transaction.id},$formattedDate,${transaction.type},${transaction.category},${"%.2f".format(transaction.amount)},\"$escapedNote\""
                )
            }
        }

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Export CSV"))
    }
}

private fun showDatePicker(context: Context, initialMillis: Long?, onSelected: (Long) -> Unit) {
    val calendar = Calendar.getInstance().apply {
        if (initialMillis != null) timeInMillis = initialMillis
    }
    DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val picked = Calendar.getInstance().apply {
                set(year, month, dayOfMonth, 12, 0, 0)
                set(Calendar.MILLISECOND, 0)
            }
            onSelected(picked.timeInMillis)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    ).show()
}

private fun startOfDay(millis: Long): Long {
    return Calendar.getInstance().apply {
        timeInMillis = millis
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0);     set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}

private fun endOfDay(millis: Long): Long {
    return Calendar.getInstance().apply {
        timeInMillis = millis
        set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59)
        set(Calendar.SECOND, 59);      set(Calendar.MILLISECOND, 999)
    }.timeInMillis
}

private fun formatDate(millis: Long): String =
    SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(millis))

private fun humanReadableDayLabel(dayStartMillis: Long): String {
    val today     = startOfDay(System.currentTimeMillis())
    val yesterday = today - 24 * 60 * 60 * 1000L
    return when (dayStartMillis) {
        today     -> "Today"
        yesterday -> "Yesterday"
        else      -> formatDate(dayStartMillis)
    }
}