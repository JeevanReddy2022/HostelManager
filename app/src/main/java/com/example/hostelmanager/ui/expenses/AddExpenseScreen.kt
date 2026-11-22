package com.example.hostelmanager.ui.expenses

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.Button
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hostelmanager.ui.components.ImagePickerField
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

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

    val expenseDatePickerState = rememberDatePickerState()
    var showExpenseDatePicker by remember { mutableStateOf(false) }

    val enteredDatePickerState = rememberDatePickerState()
    var showEnteredDatePicker by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    val amountLong = amount.toLongOrNull() ?: 0L
    val isAmountValid = amountLong > 0

    // NEW: Date validations
    val isExpenseDateValid = isValidPastOrTodayDate(expenseDate)
    val isExpenseDateFuture = expenseDate.isNotBlank() && try {
        LocalDate.parse(expenseDate, DateTimeFormatter.ISO_LOCAL_DATE).isAfter(LocalDate.now())
    } catch (e: Exception) { false }
    val isEnteredDateValid = isValidPastOrTodayDate(enteredDate)

    val isFormValid = isExpenseDateValid && isEnteredDateValid &&
            selectedCategory.isNotBlank() && isAmountValid

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Expense") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Expense Date (when it actually occurred) — with validation
            OutlinedTextField(
                value = expenseDate,
                onValueChange = { expenseDate = it },
                label = { Text("Expense Date (when occurred) *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                isError = expenseDate.isNotBlank() && !isExpenseDateValid,
                supportingText = {
                    if (expenseDate.isNotBlank()) {
                        if (isExpenseDateFuture) {
                            Text("Expense date cannot be in the future", color = MaterialTheme.colorScheme.error)
                        } else if (!isExpenseDateValid) {
                            Text("Invalid date. Use YYYY-MM-DD format", color = MaterialTheme.colorScheme.error)
                        }
                    }
                },
                trailingIcon = {
                    IconButton(onClick = { showExpenseDatePicker = true }) {
                        Icon(Icons.Default.CalendarToday, contentDescription = "Pick expense date")
                    }
                }
            )

            // Entered Date (when recorded in app) — with validation
            OutlinedTextField(
                value = enteredDate,
                onValueChange = { enteredDate = it },
                label = { Text("Recorded Date (when added) *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                isError = enteredDate.isNotBlank() && !isEnteredDateValid,
                supportingText = {
                    if (enteredDate.isNotBlank() && !isEnteredDateValid) {
                        Text("Invalid date. Use YYYY-MM-DD format", color = MaterialTheme.colorScheme.error)
                    }
                },
                trailingIcon = {
                    IconButton(onClick = { showEnteredDatePicker = true }) {
                        Icon(Icons.Default.CalendarToday, contentDescription = "Pick recorded date")
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
                    singleLine = true
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

            // Amount — with IME action
            OutlinedTextField(
                value = amount,
                onValueChange = { input -> amount = input.filter { it.isDigit() } },
                label = { Text("Amount (₹) *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                isError = amount.isNotBlank() && !isAmountValid,
                supportingText = {
                    if (amount.isNotBlank() && !isAmountValid) {
                        Text("Amount must be greater than 0", color = MaterialTheme.colorScheme.error)
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
                modifier = Modifier.fillMaxWidth(),
                enabled = isFormValid
            ) {
                Text("Save Expense")
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
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                        expenseDate = pickedDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
                    }
                    showExpenseDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showExpenseDatePicker = false }) { Text("Cancel") }
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
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                        enteredDate = pickedDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
                    }
                    showEnteredDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showEnteredDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = enteredDatePickerState)
        }
    }
}