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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hostelmanager.data.Payment
import com.example.hostelmanager.data.ResidentWithRoom
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentHistoryScreen(
    hostelId: String,
    onBackClick: () -> Unit,
    viewModel: PaymentHistoryViewModel = viewModel()
) {
    LaunchedEffect(hostelId) {
        viewModel.setHostelId(hostelId)
    }

    val payments by viewModel.payments.collectAsState()
    val residents by viewModel.residents.collectAsState()

    var selectedMonth by remember { mutableStateOf<String?>(null) }
    var selectedPersonId by remember { mutableStateOf<String?>(null) }

    var expandedMonth by remember { mutableStateOf(false) }
    var personSearchQuery by remember { mutableStateOf("") }

    // NEW: delete confirmation dialog state
    var paymentToDelete by remember { mutableStateOf<Payment?>(null) }

    val months = remember {
        (0 until 12).map { offset ->
            LocalDate.now().minusMonths(offset.toLong()).format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM"))
        }
    }

    val filteredResidents = if (personSearchQuery.length < 2) {
        emptyList()
    } else {
        residents.filter { resident ->
            resident.person.name.contains(personSearchQuery, ignoreCase = true) ||
                    resident.person.phone.contains(personSearchQuery) ||
                    resident.roomNumber.contains(personSearchQuery, ignoreCase = true)
        }.take(5)
    }

    val selectedPersonName = residents.firstOrNull { it.person.id == selectedPersonId }?.person?.name

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Payment History") },
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
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ExposedDropdownMenuBox(
                    expanded = expandedMonth,
                    onExpandedChange = { expandedMonth = !expandedMonth },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = selectedMonth?.let { formatMonth(it) } ?: "All time",
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Month") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedMonth) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedMonth,
                        onDismissRequest = { expandedMonth = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("All time") },
                            onClick = {
                                selectedMonth = null
                                viewModel.setMonth(null)
                                expandedMonth = false
                            }
                        )
                        months.forEach { month ->
                            DropdownMenuItem(
                                text = { Text(formatMonth(month)) },
                                onClick = {
                                    selectedMonth = month
                                    viewModel.setMonth(month)
                                    expandedMonth = false
                                }
                            )
                        }
                    }
                }

                if (selectedPersonId == null) {
                    OutlinedTextField(
                        value = personSearchQuery,
                        onValueChange = { personSearchQuery = it },
                        label = { Text("Person") },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Search...") }
                    )
                } else {
                    OutlinedTextField(
                        value = selectedPersonName ?: "",
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Person") },
                        trailingIcon = {
                            IconButton(onClick = {
                                selectedPersonId = null
                                viewModel.setPerson(null)
                            }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear")
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            if (selectedPersonId == null && filteredResidents.isNotEmpty()) {
                Card(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                    Column {
                        filteredResidents.forEach { resident ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedPersonId = resident.person.id
                                        viewModel.setPerson(resident.person.id)
                                        personSearchQuery = ""
                                    }
                                    .padding(16.dp)
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(resident.person.name, style = MaterialTheme.typography.bodyLarge)
                                    Text(resident.person.phone, style = MaterialTheme.typography.bodySmall)
                                }
                                Text("Room ${resident.roomNumber}", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }

            if (payments.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("No payments found.", style = MaterialTheme.typography.titleMedium)
                    Text("Adjust filters or record a payment.")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(payments) { payment ->
                        PaymentCard(
                            payment = payment,
                            residents = residents,
                            onDeleteClick = { paymentToDelete = payment }
                        )
                    }
                }
            }
        }
    }

    // NEW: Delete confirmation dialog
    paymentToDelete?.let { payment ->
        AlertDialog(
            onDismissRequest = { paymentToDelete = null },
            title = { Text("Delete Payment?") },
            text = {
                Text(
                    "Delete payment of ₹${payment.amountPaise / 100} from ${payment.paymentDate}? " +
                            "This can't be undone."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deletePayment(payment.id)
                        paymentToDelete = null
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { paymentToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun PaymentCard(
    payment: Payment,
    residents: List<ResidentWithRoom>,
    onDeleteClick: () -> Unit
) {
    val resident = residents.firstOrNull { it.person.id == payment.personId }
    val personName = resident?.person?.name ?: "Unknown"
    val roomNumber = resident?.roomNumber ?: "—"

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(personName, style = MaterialTheme.typography.titleMedium)
                Text("Room $roomNumber", style = MaterialTheme.typography.bodySmall)
                // NEW: shows both dates
                Text("Received: ${payment.paymentDate}", style = MaterialTheme.typography.bodySmall)
                if (payment.date != payment.paymentDate) {
                    Text("Recorded: ${payment.date}", style = MaterialTheme.typography.bodySmall)
                }
                Text(payment.mode, style = MaterialTheme.typography.bodySmall)
                if (payment.note.isNotBlank()) {
                    Text(payment.note, style = MaterialTheme.typography.bodySmall)
                }
            }
            Column(horizontalAlignment = androidx.compose.ui.Alignment.End) {
                Text(
                    "₹${payment.amountPaise / 100}",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                // NEW: delete button
                IconButton(onClick = onDeleteClick) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

private fun formatMonth(yyyyMm: String): String {
    val parts = yyyyMm.split("-")
    if (parts.size != 2) return yyyyMm
    val year = parts[0]
    val monthNum = parts[1].toIntOrNull() ?: return yyyyMm
    val monthName = java.time.Month.of(monthNum).getDisplayName(TextStyle.FULL, Locale.getDefault())
    return "$monthName $year"
}