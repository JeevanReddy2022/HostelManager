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
import com.example.hostelmanager.data.RoomWithOccupancy
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransferRoomScreen(
    personId: String,
    onBackClick: () -> Unit,
    viewModel: TransferRoomViewModel = viewModel()
) {
    LaunchedEffect(personId) { viewModel.setPersonId(personId) }

    val person by viewModel.person.collectAsState()
    val activeStay by viewModel.activeStay.collectAsState()
    val currentRoom by viewModel.currentRoom.collectAsState()
    val allRooms by viewModel.availableRooms.collectAsState()
    val transferError by viewModel.transferError.collectAsState()

    val scope = rememberCoroutineScope()

    // Only rooms with vacancy, and never the room they're already in
    val transferTargets = allRooms.filter {
        it.room.id != currentRoom?.id && (it.room.bedCount - it.occupiedBeds) > 0
    }

    var selectedRoom by remember { mutableStateOf<RoomWithOccupancy?>(null) }
    var expandedRoom by remember { mutableStateOf(false) }
    var transferDate by remember {
        mutableStateOf(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE))
    }
    var rent by remember { mutableStateOf("") }

    val datePickerState = rememberDatePickerState()
    var showDatePicker by remember { mutableStateOf(false) }
    var showConfirmDialog by remember { mutableStateOf(false) }

    // Picking a room pulls in that room's rate (still editable)
    LaunchedEffect(selectedRoom) {
        val r = selectedRoom
        if (r != null) rent = (r.room.defaultRentPerBed / 100).toString()
    }

    val stay = activeStay
    val isDateValid = transferDate.isNotBlank() && (stay == null || transferDate >= stay.joinDate)
    val isFormValid = selectedRoom != null && isDateValid && rent.isNotBlank()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Transfer Room") },
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

            // Current stay (read-only)
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Current Stay", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    Text("Room: ${currentRoom?.roomNumber ?: "—"}")
                    Text("Monthly Rent: ₹${(stay?.monthlyRent ?: 0L) / 100}")
                    Text("Joined: ${stay?.joinDate ?: "—"}")
                    Text("Deposit held: ₹${(stay?.depositCollected ?: 0L) / 100}")
                }
            }

            if (transferError != null) {
                Text(
                    text = transferError!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text("Transfer To", style = MaterialTheme.typography.titleMedium)

            ExposedDropdownMenuBox(
                expanded = expandedRoom,
                onExpandedChange = { expandedRoom = !expandedRoom }
            ) {
                OutlinedTextField(
                    value = selectedRoom?.room?.roomNumber ?: "",
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("New Room *") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedRoom) },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = expandedRoom,
                    onDismissRequest = { expandedRoom = false }
                ) {
                    if (transferTargets.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text("No other rooms with vacancy") },
                            onClick = { expandedRoom = false }
                        )
                    } else {
                        transferTargets.forEach { roomWithOcc ->
                            val vacancy = roomWithOcc.room.bedCount - roomWithOcc.occupiedBeds
                            DropdownMenuItem(
                                text = { Text("Room ${roomWithOcc.room.roomNumber} (Vacancy: $vacancy)") },
                                onClick = {
                                    selectedRoom = roomWithOcc
                                    expandedRoom = false
                                }
                            )
                        }
                    }
                }
            }

            OutlinedTextField(
                value = transferDate,
                onValueChange = { transferDate = it },
                label = { Text("Transfer Date (YYYY-MM-DD) *") },
                modifier = Modifier.fillMaxWidth(),
                isError = transferDate.isNotBlank() && !isDateValid,
                supportingText = {
                    if (transferDate.isNotBlank() && !isDateValid) {
                        Text("Cannot be before the join date (${stay?.joinDate ?: ""})")
                    }
                },
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.CalendarToday, contentDescription = "Pick date")
                    }
                }
            )

            OutlinedTextField(
                value = rent,
                onValueChange = { input -> rent = input.filter { it.isDigit() } },
                label = { Text("New Monthly Rent (₹) *") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Text(
                "The deposit of ₹${(stay?.depositCollected ?: 0L) / 100} carries over to the new room.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Button(
                onClick = { showConfirmDialog = true },
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                enabled = isFormValid
            ) {
                Text("Transfer Room")
            }
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val date = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                        transferDate = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) { DatePicker(state = datePickerState) }
    }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Confirm transfer?") },
            text = {
                Text(
                    "Move ${person?.name ?: "this resident"} from Room ${currentRoom?.roomNumber ?: "—"} " +
                            "to Room ${selectedRoom?.room?.roomNumber ?: "—"} on $transferDate?\n\n" +
                            "Their current stay will be closed (history kept) and a new stay opened. " +
                            "The deposit carries over."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showConfirmDialog = false
                    scope.launch {
                        val success = viewModel.transferRoom(
                            selectedRoom!!.room.id,
                            transferDate,
                            (rent.toLongOrNull() ?: 0L) * 100
                        )
                        if (success) onBackClick()
                    }
                }) { Text("Yes, transfer") }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) { Text("Cancel") }
            }
        )
    }
}