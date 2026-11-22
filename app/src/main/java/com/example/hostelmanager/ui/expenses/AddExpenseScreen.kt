package com.example.hostelmanager.ui.expenses

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hostelmanager.ui.components.ImagePickerField
import com.example.hostelmanager.ui.theme.AppBackground
import com.example.hostelmanager.ui.theme.AppBorder
import com.example.hostelmanager.ui.theme.AppMuted
import com.example.hostelmanager.ui.theme.AppNavy
import com.example.hostelmanager.ui.theme.AppRed
import com.example.hostelmanager.ui.theme.AppText
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

// Dark yellow for non-blocking warnings
private val WarningYellow = Color(0xFFB8860B)

// Amounts above this show a warning (does not block saving)
private const val HIGH_AMOUNT_WARNING = 100_000L

// Helper: validate date is correct format and not in future
private fun isValidPastOrTodayDate(dateStr: String): Boolean {
    if (dateStr.isBlank()) return false
    return try {
        val date = LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE)
        !date.isAfter(LocalDate.now())
    } catch (e: Exception) {
        false
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen(
    hostelId: String,
    onBackClick: () -> Unit,
    viewModel: AddExpenseViewModel = viewModel()
) {
    val focusManager = LocalFocusManager.current

    var expenseDate by remember { mutableStateOf("") }
    var enteredDate by remember {
        mutableStateOf(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE))
    }

    val categories = listOf("Electricity", "Water", "Maintenance", "Repairs", "Supplies", "Salary", "Other")
    var selectedCategory by remember { mutableStateOf("Electricity") }
    var expandedCategory by remember { mutableStateOf(false) }

    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var receiptImagePath by remember { mutableStateOf<String?>(null) }

    // Date pickers: future dates are not selectable.
    // DatePicker works in UTC-midnight millis, so compare against today's UTC midnight.
    val todayUtcMillis = remember {
        LocalDate.now().atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
    }
    val noFutureDates = remember {
        object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean =
                utcTimeMillis <= todayUtcMillis
        }
    }

    val expenseDatePickerState = rememberDatePickerState(selectableDates = noFutureDates)
    var showExpenseDatePicker by remember { mutableStateOf(false) }

    val enteredDatePickerState = rememberDatePickerState(selectableDates = noFutureDates)
    var showEnteredDatePicker by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    val amountLong = amount.toLongOrNull() ?: 0L
    val isAmountValid = amountLong > 0
    val showAmountWarning = isAmountValid && amountLong > HIGH_AMOUNT_WARNING

    // Date validations
    val isExpenseDateValid = isValidPastOrTodayDate(expenseDate)
    val isExpenseDateFuture = expenseDate.isNotBlank() && try {
        LocalDate.parse(expenseDate, DateTimeFormatter.ISO_LOCAL_DATE).isAfter(LocalDate.now())
    } catch (e: Exception) { false }
    val isEnteredDateValid = isValidPastOrTodayDate(enteredDate)

    val isFormValid = isExpenseDateValid && isEnteredDateValid &&
            selectedCategory.isNotBlank() && isAmountValid

    // High-contrast, warm-colored text field styling
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

    Scaffold(
        containerColor = AppBackground,
        topBar = {
            TopAppBar(
                title = { Text("Add Expense", fontWeight = FontWeight.Bold, color = AppText) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = AppText)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppBackground,
                    titleContentColor = AppText,
                    navigationIconContentColor = AppText
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Expense Date (when it actually occurred) — cannot be in the future
            OutlinedTextField(
                value = expenseDate,
                onValueChange = { expenseDate = it.trim().take(10) },
                label = { Text("Expense Date (when occurred) *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = textFieldColors,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                isError = expenseDate.isNotBlank() && !isExpenseDateValid,
                supportingText = {
                    if (expenseDate.isNotBlank()) {
                        if (isExpenseDateFuture) {
                            Text("Expense date cannot be in the future", color = AppRed)
                        } else if (!isExpenseDateValid) {
                            Text("Invalid date. Use YYYY-MM-DD format", color = AppRed)
                        }
                    }
                },
                trailingIcon = {
                    IconButton(onClick = { showExpenseDatePicker = true }) {
                        Icon(Icons.Default.CalendarToday, contentDescription = "Pick expense date", tint = AppNavy)
                    }
                }
            )

            // Entered Date (when recorded in app) — with validation
            OutlinedTextField(
                value = enteredDate,
                onValueChange = { enteredDate = it.trim().take(10) },
                label = { Text("Recorded Date (when added) *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = textFieldColors,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                isError = enteredDate.isNotBlank() && !isEnteredDateValid,
                supportingText = {
                    if (enteredDate.isNotBlank() && !isEnteredDateValid) {
                        Text("Invalid date. Use YYYY-MM-DD format", color = AppRed)
                    }
                },
                trailingIcon = {
                    IconButton(onClick = { showEnteredDatePicker = true }) {
                        Icon(Icons.Default.CalendarToday, contentDescription = "Pick recorded date", tint = AppNavy)
                    }
                }
            )

            // Category dropdown
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
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = textFieldColors
                )
                ExposedDropdownMenu(
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

            // Amount — digits only, max 9 digits, warning above ₹1,00,000
            OutlinedTextField(
                value = amount,
                onValueChange = { input -> amount = input.filter { it.isDigit() }.take(9) },
                label = { Text("Amount (₹) *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = textFieldColors,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                isError = amount.isNotBlank() && !isAmountValid,
                supportingText = {
                    when {
                        amount.isNotBlank() && !isAmountValid ->
                            Text("Amount must be greater than 0", color = AppRed)
                        showAmountWarning ->
                            Text(
                                "Warning: Amount is above ₹1,00,000. Please double-check.",
                                color = WarningYellow
                            )
                    }
                }
            )

            // Note — last field, uses Done action
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Note (optional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = textFieldColors,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Receipt image
            ImagePickerField(
                label = "Invoice/Receipt Image (optional)",
                imagePath = receiptImagePath,
                isRequired = false,
                onImageCaptured = { receiptImagePath = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val amountPaise = amount.toLongOrNull()?.times(100) ?: 0L
                    scope.launch {
                        val success = viewModel.addExpense(
                            hostelId,
                            expenseDate,
                            enteredDate,
                            selectedCategory,
                            amountPaise,
                            note,
                            receiptImagePath
                        )
                        if (success) onBackClick()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppNavy,
                    contentColor = Color.White,
                    disabledContainerColor = AppBorder,
                    disabledContentColor = AppMuted
                ),
                enabled = isFormValid
            ) {
                Text(
                    "Save Expense",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }

    // Expense Date Picker
    if (showExpenseDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showExpenseDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    expenseDatePickerState.selectedDateMillis?.let { millis ->
                        val pickedDate = Instant.ofEpochMilli(millis)
                            .atZone(ZoneOffset.UTC)
                            .toLocalDate()
                        expenseDate = pickedDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
                    }
                    showExpenseDatePicker = false
                }) { Text("OK", color = AppNavy, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showExpenseDatePicker = false }) { Text("Cancel", color = AppMuted) }
            }
        ) {
            DatePicker(state = expenseDatePickerState)
        }
    }

    // Entered Date Picker
    if (showEnteredDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showEnteredDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    enteredDatePickerState.selectedDateMillis?.let { millis ->
                        val pickedDate = Instant.ofEpochMilli(millis)
                            .atZone(ZoneOffset.UTC)
                            .toLocalDate()
                        enteredDate = pickedDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
                    }
                    showEnteredDatePicker = false
                }) { Text("OK", color = AppNavy, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showEnteredDatePicker = false }) { Text("Cancel", color = AppMuted) }
            }
        ) {
            DatePicker(state = enteredDatePickerState)
        }
    }
}