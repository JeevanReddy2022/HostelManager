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
fun ReAdmitResidentScreen(
    personId: String,
    hostelId: String,
    onBackClick: () -> Unit,
    viewModel: ReAdmitResidentViewModel = viewModel()
) {
    LaunchedEffect(personId, hostelId) {
        viewModel.setPersonId(personId)
        viewModel.setHostelId(hostelId)
    }

    val person by viewModel.person.collectAsState()
    val roomsWithOccupancy by viewModel.roomsForHostel.collectAsState()
    val availableRooms = roomsWithOccupancy.filter { (it.room.bedCount - it.occupiedBeds) > 0 }

    val scope = rememberCoroutineScope()

    var selectedRoom by remember { mutableStateOf<RoomWithOccupancy?>(null) }
    var expandedRoom by remember { mutableStateOf(false) }
    var joinDate by remember {
        mutableStateOf(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE))
    }
    var rent by remember { mutableStateOf("") }
    var deposit by remember { mutableStateOf("") }

    val joinDatePickerState = rememberDatePickerState()
    var showJoinDatePicker by remember { mutableStateOf(false) }

    LaunchedEffect(selectedRoom) {
        if (selectedRoom != null && rent.isEmpty()) {
            rent = (selectedRoom!!.room.defaultRentPerBed / 100).toString()
        }
    }

    val isFormValid = selectedRoom != null && joinDate.isNotBlank() &&
            rent.isNotBlank() && deposit.isNotBlank()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Re-admit Resident") },
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
            val p = person
            if (p == null) {
                Text("Loading…")
            } else {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Resident Details", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(8.dp))
                        Text("Name: ${p.name}")
                        Text("Phone: ${p.phone}")
                        Text("Govt ID: ${p.govtIdNumber}")
                        Text("Emergency: ${p.emergencyContact}")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text("New Stay Details", style = MaterialTheme.typography.titleMedium)

                ExposedDropdownMenuBox(
                    expanded = expandedRoom,
                    onExpandedChange = { expandedRoom = !expandedRoom }
                ) {
                    OutlinedTextField(
                        value = selectedRoom?.room?.roomNumber ?: "",
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Select Room *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedRoom) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedRoom,
                        onDismissRequest = { expandedRoom = false }
                    ) {
                        if (availableRooms.isEmpty()) {
                            DropdownMenuItem(
                                text = { Text("No rooms with vacancy") },
                                onClick = { expandedRoom = false }
                            )
                        } else {
                            availableRooms.forEach { roomWithOcc ->
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
                    value = joinDate,
                    onValueChange = { joinDate = it },
                    label = { Text("Join Date (YYYY-MM-DD) *") },
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(onClick = { showJoinDatePicker = true }) {
                            Icon(Icons.Default.CalendarToday, contentDescription = "Pick Date")
                        }
                    }
                )

                OutlinedTextField(
                    value = rent,
                    onValueChange = { input -> rent = input.filter { it.isDigit() } },
                    label = { Text("Monthly Rent (₹)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                OutlinedTextField(
                    value = deposit,
                    onValueChange = { input -> deposit = input.filter { it.isDigit() } },
                    label = { Text("Deposit Collected (₹)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                Button(
                    onClick = {
                        val rentInPaise = rent.toLongOrNull()?.times(100) ?: 0L
                        val depositInPaise = deposit.toLongOrNull()?.times(100) ?: 0L

                        scope.launch {
                            val success = viewModel.reAdmitResident(
                                selectedRoom!!.room.id,
                                joinDate,
                                rentInPaise,
                                depositInPaise
                            )
                            if (success) onBackClick()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    enabled = isFormValid
                ) {
                    Text("Re-admit Resident")
                }
            }
        }
    }

    if (showJoinDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showJoinDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    joinDatePickerState.selectedDateMillis?.let { millis ->
                        val date = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                        joinDate = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
                    }
                    showJoinDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showJoinDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = joinDatePickerState)
        }
    }
}