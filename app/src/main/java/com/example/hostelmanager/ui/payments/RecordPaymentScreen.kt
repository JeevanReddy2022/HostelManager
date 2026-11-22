package com.example.hostelmanager.ui.payments

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import com.example.hostelmanager.data.ResidentWithRoom
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private const val PENDING_BUFFER_RUPEES = 3000L

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
fun RecordPaymentScreen(
    hostelId: String,
    prefilledPersonId: String = "",
    onBackClick: () -> Unit,
    viewModel: RecordPaymentViewModel = viewModel()
) {
    LaunchedEffect(hostelId) {
        viewModel.setHostelId(hostelId)
    }

    val allResidents by viewModel.residents.collectAsState()
    val focusManager = LocalFocusManager.current

    var searchQuery by remember { mutableStateOf("") }
    var selectedResident by remember { mutableStateOf<ResidentWithRoom?>(null) }
    var amount by remember { mutableStateOf("") }

    var paymentDate by remember { mutableStateOf("") }
    var recordedDate by remember {
        mutableStateOf(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE))
    }
    var note by remember { mutableStateOf("") }

    val modes = listOf("Cash", "UPI", "Bank Transfer")
    var selectedMode by remember { mutableStateOf("Cash") }
    var expandedMode by remember { mutableStateOf(false) }

    val paymentDatePickerState = rememberDatePickerState()
    var showPaymentDatePicker by remember { mutableStateOf(false) }

    val recordedDatePickerState = rememberDatePickerState()
    var showRecordedDatePicker by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    LaunchedEffect(prefilledPersonId, allResidents) {
        if (prefilledPersonId.isNotEmpty() && prefilledPersonId != "none" && selectedResident == null) {
            selectedResident = allResidents.find { it.person.id == prefilledPersonId }
        }
    }

    val filteredResidents = if (searchQuery.length < 2) {
        emptyList()
    } else {
        allResidents.filter { resident ->
            resident.person.name.contains(searchQuery, ignoreCase = true) ||
                    resident.person.phone.contains(searchQuery) ||
                    resident.roomNumber.contains(searchQuery, ignoreCase = true)
        }.take(5)
    }

    LaunchedEffect(selectedResident) {
        val resident = selectedResident
        if (resident != null && amount.isEmpty()) {
            amount = (resident.monthlyRent / 100).toString()
        }
    }

    val amountLong = amount.toLongOrNull() ?: 0L
    val rentRupees = selectedResident?.monthlyRent?.div(100) ?: 0L
    val maxAllowed = rentRupees + PENDING_BUFFER_RUPEES

    val isAmountValid = amountLong > 0 && amountLong <= maxAllowed
    val isAmountZeroOrNegative = amount.isNotBlank() && amountLong <= 0
    val isAmountExceeding = amount.isNotBlank() && amountLong > maxAllowed

    // NEW: Date validations
    val isPaymentDateValid = isValidPastOrTodayDate(paymentDate)
    val isPaymentDateFuture = paymentDate.isNotBlank() && try {
        LocalDate.parse(paymentDate, DateTimeFormatter.ISO_LOCAL_DATE).isAfter(LocalDate.now())
    } catch (e: Exception) { false }
    val isRecordedDateValid = isValidPastOrTodayDate(recordedDate)

    val isFormValid = selectedResident != null && amount.isNotBlank() && isAmountValid &&
            isPaymentDateValid && isRecordedDateValid

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Record Payment") },
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
            // Resident picker
            if (selectedResident == null) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search resident (name, phone, or room)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() })
                )

                if (filteredResidents.isNotEmpty()) {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column {
                            filteredResidents.forEach { resident ->
                                ResidentSearchResult(
                                    resident = resident,
                                    onClick = {
                                        selectedResident = resident
                                        searchQuery = ""
                                    }
                                )
                            }
                        }
                    }
                }
            } else {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            selectedResident = null
                            amount = ""
                        }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(selectedResident!!.person.name, style = MaterialTheme.typography.titleMedium)
                            Text("Room ${selectedResident!!.roomNumber}", style = MaterialTheme.typography.bodySmall)
                            Text("Rent: ₹${selectedResident!!.monthlyRent / 100}", style = MaterialTheme.typography.bodyMedium)
                        }
                        IconButton(onClick = {
                            selectedResident = null
                            amount = ""
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                }
            }

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
                    if (selectedResident != null) {
                        if (isAmountZeroOrNegative) {
                            Text("Amount must be greater than 0", color = MaterialTheme.colorScheme.error)
                        } else if (isAmountExceeding) {
                            Text(
                                "Cannot exceed ₹$maxAllowed (rent ₹$rentRupees + ₹$PENDING_BUFFER_RUPEES buffer)",
                                color = MaterialTheme.colorScheme.error
                            )
                        } else {
                            Text("Rent: ₹$rentRupees · Max allowed: ₹$maxAllowed (includes ₹$PENDING_BUFFER_RUPEES buffer)")
                        }
                    }
                }
            )

            // Payment Date (when money was received) — with validation
            OutlinedTextField(
                value = paymentDate,
                onValueChange = { paymentDate = it },
                label = { Text("Payment Date (when received) *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                isError = paymentDate.isNotBlank() && !isPaymentDateValid,
                supportingText = {
                    if (paymentDate.isNotBlank()) {
                        if (isPaymentDateFuture) {
                            Text("Payment date cannot be in the future", color = MaterialTheme.colorScheme.error)
                        } else if (!isPaymentDateValid) {
                            Text("Invalid date. Use YYYY-MM-DD format", color = MaterialTheme.colorScheme.error)
                        }
                    }
                },
                trailingIcon = {
                    IconButton(onClick = { showPaymentDatePicker = true }) {
                        Icon(Icons.Default.CalendarToday, contentDescription = "Pick payment date")
                    }
                }
            )

            // Recorded Date (when added to app) — with validation
            OutlinedTextField(
                value = recordedDate,
                onValueChange = { recordedDate = it },
                label = { Text("Recorded Date (when added) *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                isError = recordedDate.isNotBlank() && !isRecordedDateValid,
                supportingText = {
                    if (recordedDate.isNotBlank() && !isRecordedDateValid) {
                        Text("Invalid date. Use YYYY-MM-DD format", color = MaterialTheme.colorScheme.error)
                    }
                },
                trailingIcon = {
                    IconButton(onClick = { showRecordedDatePicker = true }) {
                        Icon(Icons.Default.CalendarToday, contentDescription = "Pick recorded date")
                    }
                }
            )

            ExposedDropdownMenuBox(
                expanded = expandedMode,
                onExpandedChange = { expandedMode = !expandedMode }
            ) {
                OutlinedTextField(
                    value = selectedMode,
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Payment Mode *") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedMode) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    singleLine = true
                )
                ExposedDropdownMenu(
                    expanded = expandedMode,
                    onDismissRequest = { expandedMode = false }
                ) {
                    modes.forEach { mode ->
                        DropdownMenuItem(
                            text = { Text(mode) },
                            onClick = {
                                selectedMode = mode
                                expandedMode = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Note (optional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val amountPaise = amount.toLongOrNull()?.times(100) ?: 0L
                    scope.launch {
                        val success = viewModel.recordPayment(
                            selectedResident!!.person.id,
                            amountPaise,
                            selectedMode,
                            paymentDate,
                            recordedDate,
                            note
                        )
                        if (success) onBackClick()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = isFormValid
            ) {
                Text("Save Payment")
            }
        }
    }

    // Payment Date Picker
    if (showPaymentDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showPaymentDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    paymentDatePickerState.selectedDateMillis?.let { millis ->
                        val pickedDate = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                        paymentDate = pickedDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
                    }
                    showPaymentDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showPaymentDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = paymentDatePickerState)
        }
    }

    // Recorded Date Picker
    if (showRecordedDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showRecordedDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    recordedDatePickerState.selectedDateMillis?.let { millis ->
                        val pickedDate = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                        recordedDate = pickedDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
                    }
                    showRecordedDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showRecordedDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = recordedDatePickerState)
        }
    }
}

@Composable
fun ResidentSearchResult(resident: ResidentWithRoom, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(resident.person.name, style = MaterialTheme.typography.bodyLarge)
            Text(resident.person.phone, style = MaterialTheme.typography.bodySmall)
        }
        Column {
            Text("Room ${resident.roomNumber}", style = MaterialTheme.typography.bodySmall)
            Text("₹${resident.monthlyRent / 100}", style = MaterialTheme.typography.bodyMedium)
        }
    }
}