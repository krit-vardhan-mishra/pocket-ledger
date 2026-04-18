package com.just_for_fun.pocketledger.ui.history

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
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
import com.just_for_fun.pocketledger.data.model.Transaction
import com.just_for_fun.pocketledger.data.model.enums.Category
import com.just_for_fun.pocketledger.data.model.enums.displayName
import com.just_for_fun.pocketledger.ui.components.TransactionCard
import com.just_for_fun.pocketledger.ui.components.iconForCategory
import com.just_for_fun.pocketledger.ui.dashboard.AddTransactionSheet
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch
import com.just_for_fun.pocketledger.data.model.enums.TransactionType as DomainTransactionType
import com.just_for_fun.pocketledger.data.model.enums.TransactionType as UiTransactionType

private enum class HistoryTypeFilter {
    ALL,
    INCOME,
    EXPENSE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val transactions by viewModel.transactions.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var showFilterSheet by remember { mutableStateOf(false) }
    var showEditSheet by remember { mutableStateOf(false) }
    var editingTransactionId by remember { mutableStateOf<Long?>(null) }

    var startDateMillis by remember { mutableStateOf<Long?>(null) }
    var endDateMillis by remember { mutableStateOf<Long?>(null) }
    var typeFilter by remember { mutableStateOf(HistoryTypeFilter.ALL) }
    val selectedCategories = remember { mutableStateListOf<Category>() }

    val filteredTransactions = remember(
        transactions,
        searchQuery,
        startDateMillis,
        endDateMillis,
        typeFilter,
        selectedCategories.toList()
    ) {
        transactions.filter { transaction ->
            val matchesSearch = searchQuery.isBlank() ||
                transaction.note.contains(searchQuery, ignoreCase = true)

            val matchesType = when (typeFilter) {
                HistoryTypeFilter.ALL -> true
                HistoryTypeFilter.INCOME -> transaction.type == DomainTransactionType.INCOME
                HistoryTypeFilter.EXPENSE -> transaction.type == DomainTransactionType.EXPENSE
            }

            val matchesCategory = selectedCategories.isEmpty() ||
                transaction.category in selectedCategories

            val matchesStart = startDateMillis == null || transaction.date >= startDateMillis!!
            val matchesEnd = endDateMillis == null || transaction.date <= endDateMillis!!

            matchesSearch && matchesType && matchesCategory && matchesStart && matchesEnd
        }
    }

    val groupedTransactions = remember(filteredTransactions) {
        filteredTransactions
            .sortedByDescending { it.date }
            .groupBy { startOfDay(it.date) }
            .toSortedMap(compareByDescending { it })
    }

    val editingTransaction = remember(transactions, editingTransactionId) {
        transactions.firstOrNull { it.id == editingTransactionId }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Transaction History") },
                actions = {
                    IconButton(onClick = { showFilterSheet = true }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filters")
                    }
                    IconButton(
                        onClick = {
                            val result = exportTransactionsCsv(context, filteredTransactions)
                            if (result.isFailure) {
                                val error = result.exceptionOrNull()?.message ?: "Failed to export CSV"
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(error)
                                }
                            } else {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("CSV export is ready to share")
                                }
                            }
                        }
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Export CSV")
                    }
                },
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
                    groupedTransactions.forEach { (dayMillis, dayTransactions) ->
                        item(key = "header_$dayMillis") {
                            Text(
                                text = humanReadableDayLabel(dayMillis),
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                            )
                        }

                        items(dayTransactions, key = { it.id }) { transaction ->
                            TransactionCard(
                                amount = transaction.amount,
                                type = if (transaction.type == DomainTransactionType.INCOME) UiTransactionType.INCOME else UiTransactionType.EXPENSE,
                                categoryName = transaction.category.displayName(),
                                note = transaction.note,
                                icon = iconForCategory(transaction.category),
                                onDelete = { viewModel.deleteTransaction(transaction.id) },
                                onClick = {
                                    editingTransactionId = transaction.id
                                    showEditSheet = true
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showFilterSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFilterSheet = false }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Text("Filters", style = MaterialTheme.typography.titleLarge, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))

                Text("Date Range", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            showDatePicker(context, startDateMillis) { picked ->
                                startDateMillis = startOfDay(picked)
                            }
                        }
                    ) {
                        Text(startDateMillis?.let { "From: ${formatDate(it)}" } ?: "From")
                    }
                    OutlinedButton(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            showDatePicker(context, endDateMillis) { picked ->
                                endDateMillis = endOfDay(picked)
                            }
                        }
                    ) {
                        Text(endDateMillis?.let { "To: ${formatDate(it)}" } ?: "To")
                    }
                }

                TextButton(
                    onClick = {
                        startDateMillis = null
                        endDateMillis = null
                    }
                ) {
                    Text("Clear Date Range")
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text("Type", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))

                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    SegmentedButton(
                        selected = typeFilter == HistoryTypeFilter.ALL,
                        onClick = { typeFilter = HistoryTypeFilter.ALL },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3)
                    ) { Text("Both") }
                    SegmentedButton(
                        selected = typeFilter == HistoryTypeFilter.INCOME,
                        onClick = { typeFilter = HistoryTypeFilter.INCOME },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3)
                    ) { Text("Income") }
                    SegmentedButton(
                        selected = typeFilter == HistoryTypeFilter.EXPENSE,
                        onClick = { typeFilter = HistoryTypeFilter.EXPENSE },
                        shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3)
                    ) { Text("Expense") }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text("Categories", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))

                androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(Category.entries.toTypedArray()) { category ->
                        FilterChip(
                            selected = category in selectedCategories,
                            onClick = {
                                if (category in selectedCategories) {
                                    selectedCategories.remove(category)
                                } else {
                                    selectedCategories.add(category)
                                }
                            },
                            label = { Text(category.displayName()) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            typeFilter = HistoryTypeFilter.ALL
                            selectedCategories.clear()
                            startDateMillis = null
                            endDateMillis = null
                            showFilterSheet = false
                        }
                    ) {
                        Text("Clear All")
                    }
                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = { showFilterSheet = false }
                    ) {
                        Text("Apply")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    if (showEditSheet) {
        AddTransactionSheet(
            initialTransaction = editingTransaction,
            onDismissRequest = {
                showEditSheet = false
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
                showEditSheet = false
                editingTransactionId = null
            }
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
        if (initialMillis != null) {
            timeInMillis = initialMillis
        }
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
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}

private fun endOfDay(millis: Long): Long {
    return Calendar.getInstance().apply {
        timeInMillis = millis
        set(Calendar.HOUR_OF_DAY, 23)
        set(Calendar.MINUTE, 59)
        set(Calendar.SECOND, 59)
        set(Calendar.MILLISECOND, 999)
    }.timeInMillis
}

private fun formatDate(millis: Long): String {
    return SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(millis))
}

private fun humanReadableDayLabel(dayStartMillis: Long): String {
    val today = startOfDay(System.currentTimeMillis())
    val yesterday = today - 24 * 60 * 60 * 1000L
    return when (dayStartMillis) {
        today -> "Today"
        yesterday -> "Yesterday"
        else -> formatDate(dayStartMillis)
    }
}

