package com.example.hostelmanager.ui.expenses

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.hostelmanager.data.Expense
import com.example.hostelmanager.ui.components.ImagePickerField
import com.example.hostelmanager.ui.theme.AppBorder
import com.example.hostelmanager.ui.theme.AppCard
import com.example.hostelmanager.ui.theme.AppMuted
import com.example.hostelmanager.ui.theme.AppNavy
import com.example.hostelmanager.ui.theme.AppRed
import com.example.hostelmanager.ui.theme.AppText
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

private val DATE_REGEX = Regex("""^\d{4}-\d{2}-\d{2}$""")

// Dark yellow for non-blocking warnings
private val WarningYellow = Color(0xFFB8860B)

// Amounts above this show a warning (does not block saving)
private const val HIGH_AMOUNT_WARNING = 100_000L

// Strict parse: rejects wrong format and impossible dates (e.g. 2025-02-30)
private fun parseLocalDate(date: String): LocalDate? {
    if (!DATE_REGEX.matches(date)) return null
    return runCatching { LocalDate.parse(date) }.getOrNull()
}

private fun parseDateToUtcMillis(date: String): Long? =
    parseLocalDate(date)?.atStartOfDay(ZoneOffset.UTC)?.toInstant()?.toEpochMilli()

private fun formatUtcMillisToDate(millis: Long): String =
    Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate().toString()

private enum class DatePickerTarget {
    EXPENSE_DATE,
    ENTERED_DATE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditExpenseDialog(
    expense: Expense,
    hostelId: String,
    onDismiss: () -> Unit,
    onUpdate: (
        id: String,
        expenseDate: String,
        enteredDate: String,
        category: String,
        amountPaise: Long,
        note: String,
        receiptImagePath: String?
    ) -> Unit
) {
    val focusManager = LocalFocusManager.current
    val enteredDateFocus = remember { FocusRequester() }
    val amountFocus = remember { FocusRequester() }
    val noteFocus = remember { FocusRequester() }

    var expenseDate by remember { mutableStateOf(expense.expenseDate) }
    var enteredDate by remember { mutableStateOf(expense.enteredDate) }
    var activeDatePicker by remember { mutableStateOf<DatePickerTarget?>(null) }

    val categories = listOf("Electricity", "Water", "Maintenance", "Repairs", "Supplies", "Salary", "Other")
    var selectedCategory by remember { mutableStateOf(expense.category) }
    var expandedCategory by remember { mutableStateOf(false) }

    var amount by remember { mutableStateOf((expense.amountPaise / 100).toString()) }
    var note by remember { mutableStateOf(expense.note) }
    var receiptImagePath by remember { mutableStateOf<String?>(expense.receiptImagePath) }
    // Date pickers: future dates are not selectable.
    // DatePicker works in UTC-midnight millis, so compare against today's UTC midnight.
    val today = remember { LocalDate.now() }
    val todayUtcMillis = remember {
        today.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
    }
    val noFutureDates = remember {
        object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean =
                utcTimeMillis <= todayUtcMillis
        }
    }

    // Validation states
    val parsedExpenseDate = parseLocalDate(expenseDate)
    val expenseDateError = when {
        expenseDate.isBlank() -> "Expense date is required"
        parsedExpenseDate == null -> "Use valid YYYY-MM-DD format"
        parsedExpenseDate.isAfter(today) -> "Expense date cannot be in the future"
        else -> null
    }

    val parsedEnteredDate = parseLocalDate(enteredDate)
    val enteredDateError = when {
        enteredDate.isBlank() -> "Recorded date is required"
        parsedEnteredDate == null -> "Use valid YYYY-MM-DD format"
        parsedEnteredDate.isAfter(today) -> "Recorded date cannot be in the future"
        else -> null
    }

    val amountLong = amount.toLongOrNull() ?: 0L
    val amountError = when {
        amount.isBlank() -> "Amount is required"
        amountLong <= 0L -> "Amount must be greater than 0"
        else -> null
    }
    val showAmountWarning = amountError == null && amountLong > HIGH_AMOUNT_WARNING

    val isFormValid = expenseDateError == null &&
            enteredDateError == null &&
            selectedCategory.isNotBlank() &&
            amountError == null

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = AppText,
        unfocusedTextColor = AppText,
        disabledTextColor = AppMuted,
        focusedLabelColor = AppNavy,
        unfocusedLabelColor = AppMuted,
        disabledLabelColor = AppMuted,
        focusedBorderColor = AppNavy,
        unfocusedBorderColor = AppBorder,
        disabledBorderColor = AppBorder,
        errorBorderColor = AppRed,
        errorLabelColor = AppRed,
        errorTextColor = AppText,
        errorSupportingTextColor = AppRed,
        cursorColor = AppNavy
    )

    val submitForm = {
        if (isFormValid) {
            onUpdate(
                expense.id,
                expenseDate.trim(),
                enteredDate.trim(),
                selectedCategory,
                amountLong * 100,
                note.trim(),
                receiptImagePath
            )
        }
    }

    // Calendar DatePickerDialog
    activeDatePicker?.let { target ->
        val currentText = when (target) {
            DatePickerTarget.EXPENSE_DATE -> expenseDate
            DatePickerTarget.ENTERED_DATE -> enteredDate
        }
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = parseDateToUtcMillis(currentText),
            selectableDates = noFutureDates
        )

        DatePickerDialog(
            onDismissRequest = { activeDatePicker = null },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val formatted = formatUtcMillisToDate(millis)
                            when (target) {
                                DatePickerTarget.EXPENSE_DATE -> expenseDate = formatted
                                DatePickerTarget.ENTERED_DATE -> enteredDate = formatted
                            }
                        }
                        activeDatePicker = null
                    }
                ) {
                    Text("OK", color = AppNavy, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { activeDatePicker = null }) {
                    Text("Cancel", color = AppMuted)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = AppCard,
        titleContentColor = AppText,
        textContentColor = AppText,
        title = {
            Text("Edit Expense", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(
                    value = expenseDate,
                    onValueChange = { expenseDate = it.trim().take(10) },
                    label = { Text("Expense Date (YYYY-MM-DD) *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = textFieldColors,
                    isError = expenseDateError != null,
                    supportingText = expenseDateError?.let { error ->
                        { Text(error, color = AppRed) }
                    },
                    trailingIcon = {
                        IconButton(onClick = { activeDatePicker = DatePickerTarget.EXPENSE_DATE }) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = "Select Expense Date",
                                tint = AppNavy
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { enteredDateFocus.requestFocus() }
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = enteredDate,
                    onValueChange = { enteredDate = it.trim().take(10) },
                    label = { Text("Recorded Date (YYYY-MM-DD) *") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(enteredDateFocus),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = textFieldColors,
                    isError = enteredDateError != null,
                    supportingText = enteredDateError?.let { error ->
                        { Text(error, color = AppRed) }
                    },
                    trailingIcon = {
                        IconButton(onClick = { activeDatePicker = DatePickerTarget.ENTERED_DATE }) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = "Select Recorded Date",
                                tint = AppNavy
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { amountFocus.requestFocus() }
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))

                ExposedDropdownMenuBox(
                    expanded = expandedCategory,
                    onExpandedChange = { expandedCategory = !expandedCategory }
                ) {
                    OutlinedTextField(
                        value = selectedCategory,
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Category *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategory) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = textFieldColors,
                        isError = selectedCategory.isBlank()
                    )
                    DropdownMenu(
                        expanded = expandedCategory,
                        onDismissRequest = { expandedCategory = false }
                    ) {
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category) },
                                onClick = {
                                    selectedCategory = category
                                    expandedCategory = false
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                // Amount: digits only, max 9 digits, warning above ₹1,00,000
                OutlinedTextField(
                    value = amount,
                    onValueChange = { input -> amount = input.filter { it.isDigit() }.take(9) },
                    label = { Text("Amount (₹) *") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(amountFocus),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = textFieldColors,
                    isError = amountError != null,
                    supportingText = when {
                        amountError != null -> {
                            { Text(amountError, color = AppRed) }
                        }
                        showAmountWarning -> {
                            {
                                Text(
                                    "Warning: Amount is above ₹1,00,000. Please double-check.",
                                    color = WarningYellow
                                )
                            }
                        }
                        else -> null
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { noteFocus.requestFocus() }
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Note (optional)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(noteFocus),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = textFieldColors,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                            submitForm()
                        }
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))

                ImagePickerField(
                    label = "Invoice/Receipt Image (optional)",
                    imagePath = receiptImagePath,
                    isRequired = false,
                    onImageCaptured = { receiptImagePath = it }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = submitForm,
                enabled = isFormValid
            ) {
                Text(
                    "Save",
                    color = if (isFormValid) AppNavy else AppMuted,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = AppMuted)
            }
        }
    )
}