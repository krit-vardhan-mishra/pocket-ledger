package com.just_for_fun.pocketledger.ui.dashboard

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.just_for_fun.pocketledger.data.model.Transaction
import com.just_for_fun.pocketledger.data.model.enums.Category
import com.just_for_fun.pocketledger.data.model.enums.TransactionType
import com.just_for_fun.pocketledger.data.model.enums.displayName
import com.just_for_fun.pocketledger.ui.components.iconForCategory
import com.just_for_fun.pocketledger.ui.theme.ExpenseRed
import com.just_for_fun.pocketledger.ui.theme.IncomeGreen
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class TransactionFormData(
    val id: Long?,
    val amount: Double,
    val type: TransactionType,
    val category: Category,
    val note: String,
    val dateMillis: Long
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionSheet(
    initialTransaction: Transaction? = null,
    onDismissRequest: () -> Unit,
    onSaveTransaction: (TransactionFormData) -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    var amountText by remember(initialTransaction?.id) {
        mutableStateOf(initialTransaction?.amount?.toString().orEmpty())
    }
    var transactionType by remember(initialTransaction?.id) {
        mutableStateOf(initialTransaction?.type ?: TransactionType.EXPENSE)
    }
    var selectedCategory by remember(initialTransaction?.id) {
        mutableStateOf(initialTransaction?.category)
    }
    var note by remember(initialTransaction?.id) {
        mutableStateOf(initialTransaction?.note.orEmpty())
    }
    var selectedDateMillis by remember(initialTransaction?.id) {
        mutableLongStateOf(initialTransaction?.date ?: System.currentTimeMillis())
    }
    var amountError by remember { mutableStateOf<String?>(null) }
    var categoryError by remember { mutableStateOf<String?>(null) }

    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }

    val typeColor = if (transactionType == TransactionType.INCOME) IncomeGreen else ExpenseRed

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
                .windowInsetsPadding(WindowInsets.ime),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Sheet title
            Text(
                text = if (initialTransaction == null) "New Transaction" else "Edit Transaction",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 20.dp)
            )

            // ── Type toggle ───────────────────────────────────────────────────
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = transactionType == TransactionType.EXPENSE,
                    onClick = { transactionType = TransactionType.EXPENSE },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                ) { Text("Expense", fontWeight = FontWeight.Medium) }
                SegmentedButton(
                    selected = transactionType == TransactionType.INCOME,
                    onClick = { transactionType = TransactionType.INCOME },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                ) { Text("Income", fontWeight = FontWeight.Medium) }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── Amount ────────────────────────────────────────────────────────
            OutlinedTextField(
                value = amountText,
                onValueChange = {
                    amountText = it
                    amountError = null
                },
                label = { Text("Amount") },
                leadingIcon = {
                    Text(
                        "₹",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = typeColor
                    )
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                isError = amountError != null,
                supportingText = { amountError?.let { Text(it) } },
                textStyle = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = typeColor
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = typeColor,
                    focusedLabelColor = typeColor,
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

            // ── Category ──────────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Category",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (categoryError != null) {
                    Text(
                        text = categoryError ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(Category.entries.toTypedArray()) { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = {
                            selectedCategory = category
                            categoryError = null
                        },
                        label = {
                            Text(
                                category.displayName(),
                                fontWeight = if (selectedCategory == category)
                                    FontWeight.SemiBold else FontWeight.Normal
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = iconForCategory(category),
                                contentDescription = category.displayName(),
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── Date ──────────────────────────────────────────────────────────
            OutlinedButton(
                onClick = {
                    val calendar = Calendar.getInstance().apply { timeInMillis = selectedDateMillis }
                    DatePickerDialog(
                        context,
                        { _, year, month, dayOfMonth ->
                            val selectedCalendar = Calendar.getInstance().apply {
                                set(year, month, dayOfMonth, 12, 0, 0)
                                set(Calendar.MILLISECOND, 0)
                            }
                            selectedDateMillis = selectedCalendar.timeInMillis
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                    ).show()
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = dateFormatter.format(Date(selectedDateMillis)),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── Note ──────────────────────────────────────────────────────────
            OutlinedTextField(
                value = note,
                onValueChange = { if (it.length <= 100) note = it },
                label = { Text("Note (Optional)") },
                supportingText = {
                    Text(
                        "${note.length}/100",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(28.dp))

            // ── Save button ───────────────────────────────────────────────────
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull() ?: 0.0
                    if (amount <= 0.0) {
                        amountError = "Amount cannot be zero or empty"
                        return@Button
                    }
                    val category = selectedCategory
                    if (category == null) {
                        categoryError = "Please select a category"
                        return@Button
                    }
                    onSaveTransaction(
                        TransactionFormData(
                            id           = initialTransaction?.id,
                            amount       = amount,
                            type         = transactionType,
                            category     = category,
                            note         = note.trim(),
                            dateMillis   = selectedDateMillis
                        )
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor   = MaterialTheme.colorScheme.onPrimary
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
            ) {
                Text(
                    text = if (initialTransaction == null) "Save Transaction" else "Update Transaction",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}