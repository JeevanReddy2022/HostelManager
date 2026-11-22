package com.example.hostelmanager.ui.residents

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hostelmanager.data.RoomWithOccupancy
import com.example.hostelmanager.ui.theme.AppBackground
import com.example.hostelmanager.ui.theme.AppBorder
import com.example.hostelmanager.ui.theme.AppCard
import com.example.hostelmanager.ui.theme.AppMuted
import com.example.hostelmanager.ui.theme.AppNavy
import com.example.hostelmanager.ui.theme.AppRed
import com.example.hostelmanager.ui.theme.AppSoftRed
import com.example.hostelmanager.ui.theme.AppText
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
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
    val stay by viewModel.activeStay.collectAsState()
    val currentRoom by viewModel.currentRoom.collectAsState()
    val allRooms by viewModel.availableRooms.collectAsState()
    val transferError by viewModel.transferError.collectAsState()

    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    // Memoized so filtering only runs when room list or current room changes
    val transferTargets = remember(allRooms, currentRoom) {
        allRooms.filter { it.room.id != currentRoom?.id && (it.room.bedCount - it.occupiedBeds) > 0 }
    }

    var selectedRoom by remember { mutableStateOf<RoomWithOccupancy?>(null) }
    var expandedRoom by remember { mutableStateOf(false) }
    var transferDate by remember {
        mutableStateOf(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE))
    }
    var rent by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    var showConfirmDialog by remember { mutableStateOf(false) }

    // Parse joinDate once and validate both format & chronological order
    val joinLocalDate = remember(stay?.joinDate) {
        stay?.joinDate?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
    }
    val dateError = remember(transferDate, joinLocalDate) {
        if (transferDate.isBlank()) return@remember null
        val parsed = runCatching { LocalDate.parse(transferDate) }.getOrNull()
            ?: return@remember "Enter a valid date (YYYY-MM-DD)"
        if (joinLocalDate != null && parsed.isBefore(joinLocalDate)) {
            "Cannot be before the join date (${stay?.joinDate})"
        } else null
    }

    // Keep DatePicker in sync with joinDate (disables invalid past dates)
    val latestMinDateMillis by rememberUpdatedState(
        joinLocalDate?.atStartOfDay(ZoneOffset.UTC)?.toInstant()?.toEpochMilli()
    )
    val datePickerState = rememberDatePickerState(
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean =
                latestMinDateMillis == null || utcTimeMillis >= latestMinDateMillis!!
        }
    )

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

    LaunchedEffect(selectedRoom) {
        selectedRoom?.let { rent = (it.room.defaultRentPerBed / 100).toString() }
    }

    val isFormValid = selectedRoom != null && transferDate.isNotBlank() && dateError == null && rent.isNotBlank()

    Scaffold(
        containerColor = AppBackground,
        topBar = {
            TopAppBar(
                title = { Text("Transfer Room", fontWeight = FontWeight.Bold, color = AppText) },
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                person?.name ?: "Resident",
                style = MaterialTheme.typography.headlineSmall,
                color = AppText,
                fontWeight = FontWeight.Bold
            )

            // Current stay (read-only)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = AppCard),
                border = BorderStroke(1.dp, AppBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    SectionHeader("Current Stay")
                    Spacer(modifier = Modifier.height(10.dp))
                    DetailRow("Room", currentRoom?.roomNumber ?: "—")
                    DetailRow("Monthly Rent", "₹${(stay?.monthlyRent ?: 0L) / 100}")
                    DetailRow("Joined", stay?.joinDate ?: "—")
                    DetailRow("Deposit held", "₹${(stay?.depositCollected ?: 0L) / 100}")
                }
            }

            transferError?.let { errorMsg ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(AppSoftRed)
                        .padding(14.dp)
                ) {
                    Text(
                        text = errorMsg,
                        color = AppRed,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            SectionHeader("Transfer To")

            ExposedDropdownMenuBox(
                expanded = expandedRoom,
                onExpandedChange = { expandedRoom = !expandedRoom }
            ) {
                OutlinedTextField(
                    value = selectedRoom?.room?.roomNumber ?: "",
                    onValueChange = { },
                    readOnly = true,
                    singleLine = true,
                    label = { Text("New Room *") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedRoom) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    shape = RoundedCornerShape(12.dp),
                    colors = textFieldColors
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
                onValueChange = { transferDate = it.trim() },
                label = { Text("Transfer Date (YYYY-MM-DD) *") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = textFieldColors,
                singleLine = true,
                isError = dateError != null,
                supportingText = dateError?.let { { Text(it) } },

                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number,imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.CalendarToday, contentDescription = "Pick date", tint = AppNavy)
                    }
                }
            )

            OutlinedTextField(
                value = rent,
                onValueChange = { input -> rent = input.filter { it.isDigit() } },
                label = { Text("New Monthly Rent (₹) *") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = textFieldColors,
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                       // if (isFormValid) showConfirmDialog = true
                    }
                )
            )

            Text(
                "The deposit of ₹${(stay?.depositCollected ?: 0L) / 100} carries over to the new room.",
                style = MaterialTheme.typography.bodySmall,
                color = AppMuted
            )

            Button(
                onClick = { showConfirmDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                enabled = isFormValid,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppNavy,
                    contentColor = Color.White,
                    disabledContainerColor = AppBorder,
                    disabledContentColor = AppMuted
                )
            ) {
                Text(
                    "Transfer Room",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        transferDate = Instant.ofEpochMilli(millis)
                            .atZone(ZoneOffset.UTC)
                            .toLocalDate()
                            .format(DateTimeFormatter.ISO_LOCAL_DATE)
                    }
                    showDatePicker = false
                }) { Text("OK", color = AppNavy, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel", color = AppMuted) }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            shape = RoundedCornerShape(24.dp),
            containerColor = AppCard,
            titleContentColor = AppText,
            textContentColor = AppMuted,
            title = { Text("Confirm transfer?", fontWeight = FontWeight.Bold) },
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
                }) { Text("Yes, transfer", color = AppNavy, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) { Text("Cancel", color = AppMuted) }
            }
        )
    }
}

// Reusable helpers for consistent styling
@Composable
private fun SectionHeader(title: String) {
    Column(modifier = Modifier.padding(top = 4.dp)) {
        Text(
            title,
            style = MaterialTheme.typography.titleMedium,
            color = AppText,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .width(32.dp)
                .height(2.dp)
                .background(AppNavy)
        )
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = AppMuted,
            modifier = Modifier.weight(0.4f)
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            color = AppText,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(0.6f)
        )
    }
}