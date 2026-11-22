package com.example.hostelmanager.ui.residents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    personId: String,
    onBackClick: () -> Unit,
    onCheckoutComplete: () -> Unit,
    viewModel: CheckoutViewModel = viewModel()
) {
    LaunchedEffect(personId) { viewModel.setPersonId(personId) }

    val person by viewModel.person.collectAsState()
    val activeStay by viewModel.activeStay.collectAsState()
    val currentRoom by viewModel.currentRoom.collectAsState()
    val checkoutError by viewModel.checkoutError.collectAsState()

    val scope = rememberCoroutineScope()
    val today = remember { LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE) }

    var checkoutDate by remember { mutableStateOf(today) }
    var refundAmount by remember { mutableStateOf("") }
    var refundDate by remember { mutableStateOf(today) }

    val refundModes = listOf("Cash", "UPI", "Bank Transfer")
    var refundMode by remember { mutableStateOf("Cash") }
    var expandedMode by remember { mutableStateOf(false) }

    val checkoutPickerState = rememberDatePickerState()
    var showCheckoutPicker by remember { mutableStateOf(false) }
    val refundPickerState = rememberDatePickerState()
    var showRefundPicker by remember { mutableStateOf(false) }

    var showConfirmDialog by remember { mutableStateOf(false) }
    var prefilled by remember { mutableStateOf(false) }

    // Default the refund to the full deposit held, once
    LaunchedEffect(activeStay) {
        val stay = activeStay
        if (stay != null && !prefilled) {
            refundAmount = (stay.depositCollected / 100).toString()
            prefilled = true
        }
    }

    val stay = activeStay
    val depositPaise = stay?.depositCollected ?: 0L
    val refundRupees = refundAmount.toLongOrNull() ?: -1L
    val refundPaise = if (refundRupees >= 0) refundRupees * 100 else -1L
    val retainedPaise = (depositPaise - refundPaise).coerceAtLeast(0L)

    val isDateValid = checkoutDate.isNotBlank() && (stay == null || checkoutDate >= stay.joinDate)
    val isRefundValid = refundPaise >= 0 && refundPaise <= depositPaise
    val needsRefundDetails = refundPaise > 0
    val isFormValid = stay != null && isDateValid && isRefundValid &&
            (!needsRefundDetails || (refundMode.isNotBlank() && refundDate.isNotBlank()))

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Check Out") },
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
            Text(person?.name ?: "Resident", style = MaterialTheme.typography.headlineSmall)

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Current Stay", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    Text("Room: ${currentRoom?.roomNumber ?: "—"}")
                    Text("Monthly Rent: ₹${(stay?.monthlyRent ?: 0L) / 100}")
                    Text("Joined: ${stay?.joinDate ?: "—"}")
                    Text("Deposit held: ₹${depositPaise / 100}")
                }
            }

            if (checkoutError != null) {
                Text(
                    text = checkoutError!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text("Check-out Details", style = MaterialTheme.typography.titleMedium)

            OutlinedTextField(
                value = checkoutDate,
                onValueChange = { checkoutDate = it },
                label = { Text("Check-out Date (YYYY-MM-DD) *") },
                modifier = Modifier.fillMaxWidth(),
                isError = checkoutDate.isNotBlank() && !isDateValid,
                supportingText = {
                    if (checkoutDate.isNotBlank() && !isDateValid) {
                        Text("Cannot be before the join date (${stay?.joinDate ?: ""})")
                    }
                },
                trailingIcon = {
                    IconButton(onClick = { showCheckoutPicker = true }) {
                        Icon(Icons.Default.CalendarToday, contentDescription = "Pick check-out date")
                    }
                }
            )

            Spacer(modifier = Modifier.height(8.dp))
            Text("Deposit Settlement", style = MaterialTheme.typography.titleMedium)

            OutlinedTextField(
                value = refundAmount,
                onValueChange = { input -> refundAmount = input.filter { it.isDigit() } },
                label = { Text("Refund Amount (₹) *") },
                modifier = Modifier.fillMaxWidth(),
                isError = refundAmount.isNotBlank() && !isRefundValid,
                supportingText = {
                    if (refundAmount.isNotBlank() && !isRefundValid) {
                        Text("Cannot exceed the deposit held (₹${depositPaise / 100})")
                    } else {
                        Text("Enter 0 if nothing is being refunded.")
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            if (isRefundValid && retainedPaise > 0) {
                Text(
                    "₹${retainedPaise / 100} will be retained as deductions.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }

            if (needsRefundDetails) {
                ExposedDropdownMenuBox(
                    expanded = expandedMode,
                    onExpandedChange = { expandedMode = !expandedMode }
                ) {
                    OutlinedTextField(
                        value = refundMode,
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Refund Mode *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedMode) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedMode,
                        onDismissRequest = { expandedMode = false }
                    ) {
                        refundModes.forEach { mode ->
                            DropdownMenuItem(
                                text = { Text(mode) },
                                onClick = { refundMode = mode; expandedMode = false }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = refundDate,
                    onValueChange = { refundDate = it },
                    label = { Text("Refund Date (YYYY-MM-DD) *") },
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(onClick = { showRefundPicker = true }) {
                            Icon(Icons.Default.CalendarToday, contentDescription = "Pick refund date")
                        }
                    }
                )
            }

            Text(
                "The stay will be closed and the bed becomes free. All records are kept.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Button(
                onClick = { showConfirmDialog = true },
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                enabled = isFormValid,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                )
            ) {
                Text("Check Out Resident")
            }
        }
    }

    if (showCheckoutPicker) {
        DatePickerDialog(
            onDismissRequest = { showCheckoutPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    checkoutPickerState.selectedDateMillis?.let { millis ->
                        val date = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                        checkoutDate = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
                    }
                    showCheckoutPicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showCheckoutPicker = false }) { Text("Cancel") }
            }
        ) { DatePicker(state = checkoutPickerState) }
    }

    if (showRefundPicker) {
        DatePickerDialog(
            onDismissRequest = { showRefundPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    refundPickerState.selectedDateMillis?.let { millis ->
                        val date = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                        refundDate = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
                    }
                    showRefundPicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showRefundPicker = false }) { Text("Cancel") }
            }
        ) { DatePicker(state = refundPickerState) }
    }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Confirm check-out?") },
            text = {
                Text(
                    "Check out ${person?.name ?: "this resident"} from Room " +
                            "${currentRoom?.roomNumber ?: "—"} on $checkoutDate?\n\n" +
                            "Deposit held: ₹${depositPaise / 100}\n" +
                            "Refunding: ₹${refundPaise.coerceAtLeast(0L) / 100}" +
                            (if (needsRefundDetails) " via $refundMode" else "") +
                            (if (retainedPaise > 0) "\nRetained: ₹${retainedPaise / 100}" else "") +
                            "\n\nThe bed becomes free. Records are kept."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showConfirmDialog = false
                    scope.launch {
                        val success = viewModel.checkoutResident(
                            checkoutDate,
                            refundPaise,
                            if (needsRefundDetails) refundMode else null,
                            if (needsRefundDetails) refundDate else null
                        )
                        if (success) onCheckoutComplete()
                    }
                }) { Text("Yes, check out") }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) { Text("Cancel") }
            }
        )
    }

}
