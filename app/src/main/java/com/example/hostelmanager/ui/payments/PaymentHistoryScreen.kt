package com.example.hostelmanager.ui.payments

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hostelmanager.data.PaymentWithDetails
import com.example.hostelmanager.ui.theme.AppBackground
import com.example.hostelmanager.ui.theme.AppBorder
import com.example.hostelmanager.ui.theme.AppCard
import com.example.hostelmanager.ui.theme.AppMuted
import com.example.hostelmanager.ui.theme.AppNavy
import com.example.hostelmanager.ui.theme.AppRed
import com.example.hostelmanager.ui.theme.AppText
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

    var paymentToDelete by remember { mutableStateOf<PaymentWithDetails?>(null) }

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
        containerColor = AppBackground,
        topBar = {
            TopAppBar(
                title = { Text("Payment History", fontWeight = FontWeight.Bold, color = AppText) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = AppText)
                    }
                },
                colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(
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
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
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
                        label = { Text("Month", color = AppMuted) },
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
                        label = { Text("Person", color = AppMuted) },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Search...") }
                    )
                } else {
                    OutlinedTextField(
                        value = selectedPersonName ?: "",
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Person", color = AppMuted) },
                        trailingIcon = {
                            IconButton(onClick = {
                                selectedPersonId = null
                                viewModel.setPerson(null)
                            }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", tint = AppMuted)
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            if (selectedPersonId == null && filteredResidents.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = AppCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AppBorder)
                ) {
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
                                    Text(resident.person.name, style = MaterialTheme.typography.bodyLarge, color = AppText, fontWeight = FontWeight.Medium)
                                    Text(resident.person.phone, style = MaterialTheme.typography.bodySmall, color = AppMuted)
                                }
                                Text("Room ${resident.roomNumber}", style = MaterialTheme.typography.bodySmall, color = AppNavy)
                            }
                        }
                    }
                }
            }

            if (payments.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("No payments found.", style = MaterialTheme.typography.titleLarge, color = AppText, fontWeight = FontWeight.Bold)
                    Text("Adjust filters or record a payment.", style = MaterialTheme.typography.bodyMedium, color = AppMuted)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(payments) { paymentWithDetails ->
                        PaymentCard(
                            paymentWithDetails = paymentWithDetails,
                            onDeleteClick = { paymentToDelete = paymentWithDetails }
                        )
                    }
                }
            }
        }
    }

    paymentToDelete?.let { paymentWithDetails ->
        val p = paymentWithDetails.payment
        AlertDialog(
            onDismissRequest = { paymentToDelete = null },
            shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
            containerColor = AppCard,
            titleContentColor = AppText,
            textContentColor = AppMuted,
            title = { Text("Delete Payment?", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Delete payment of ₹${p.amountPaise / 100} from ${paymentWithDetails.personName} on ${p.paymentDate}? " +
                            "This can't be undone."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deletePayment(p.id)
                        paymentToDelete = null
                    }
                ) {
                    Text("Delete", color = AppRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { paymentToDelete = null }) {
                    Text("Cancel", color = AppMuted)
                }
            }
        )
    }
}

@Composable
fun PaymentCard(
    paymentWithDetails: PaymentWithDetails,
    onDeleteClick: () -> Unit
) {
    val p = paymentWithDetails.payment

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = AppCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, AppBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    paymentWithDetails.personName,
                    style = MaterialTheme.typography.titleLarge,
                    color = AppText,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.padding(top = 4.dp))

                Row {
                    androidx.compose.foundation.layout.Box(
                        modifier = Modifier
                            .clip(androidx.compose.foundation.shape.RoundedCornerShape(50))
                            .background(com.example.hostelmanager.ui.theme.AppSoftBlue)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            "Room ${paymentWithDetails.roomNumber}",
                            style = MaterialTheme.typography.bodySmall,
                            color = AppNavy,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(modifier = Modifier.padding(start = 8.dp))
                    androidx.compose.foundation.layout.Box(
                        modifier = Modifier
                            .clip(androidx.compose.foundation.shape.RoundedCornerShape(50))
                            .background(com.example.hostelmanager.ui.theme.AppSoftBlue.copy(alpha = 0.6f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            "₹${paymentWithDetails.monthlyRent / 100}/mo",
                            style = MaterialTheme.typography.bodySmall,
                            color = AppNavy,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.padding(top = 8.dp))

                androidx.compose.foundation.layout.Box(
                    modifier = Modifier
                        .clip(androidx.compose.foundation.shape.RoundedCornerShape(50))
                        .background(com.example.hostelmanager.ui.theme.AppSoftGreen)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        "Received: ${p.paymentDate}",
                        style = MaterialTheme.typography.bodySmall,
                        color = com.example.hostelmanager.ui.theme.AppGreen,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                if (p.date != p.paymentDate) {
                    Spacer(modifier = Modifier.padding(top = 4.dp))
                    Text(
                        "Recorded: ${p.date}",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppMuted
                    )
                }

                Spacer(modifier = Modifier.padding(top = 4.dp))
                Text(
                    p.mode,
                    style = MaterialTheme.typography.bodySmall,
                    color = AppNavy,
                    fontWeight = FontWeight.Medium
                )

                if (p.note.isNotBlank()) {
                    Spacer(modifier = Modifier.padding(top = 4.dp))
                    Text(
                        p.note,
                        style = MaterialTheme.typography.bodySmall,
                        color = AppMuted
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "₹${p.amountPaise / 100}",
                    style = MaterialTheme.typography.titleLarge,
                    color = AppNavy,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDeleteClick) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = AppRed,
                        modifier = Modifier.size(20.dp)
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