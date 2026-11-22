package com.example.hostelmanager.ui.residents

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.hostelmanager.ui.rooms.RoomPickerDialog
import com.example.hostelmanager.ui.theme.AppBackground
import com.example.hostelmanager.ui.theme.AppBorder
import com.example.hostelmanager.ui.theme.AppCard
import com.example.hostelmanager.ui.theme.AppMuted
import com.example.hostelmanager.ui.theme.AppNavy
import com.example.hostelmanager.ui.theme.AppRed
import com.example.hostelmanager.ui.theme.AppText
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

private const val PAISE = 100L
private val FieldShape = RoundedCornerShape(12.dp)

private val RoomWithOccupancy.vacancy get() = room.bedCount - occupiedBeds
private fun String.toPaise() = (toLongOrNull() ?: 0L) * PAISE

private fun parseDate(s: String): LocalDate? =
    if (s.length != 10) null else runCatching { LocalDate.parse(s) }.getOrNull()

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
    val roomsForHostel by viewModel.roomsForHostel.collectAsState()
    val availableRooms = remember(roomsForHostel) { roomsForHostel.filter { it.vacancy > 0 } }

    val scope = rememberCoroutineScope()
    val today = remember { LocalDate.now().toString() }

    var selectedRoom by remember { mutableStateOf<RoomWithOccupancy?>(null) }
    var joinDate by remember { mutableStateOf(today) }
    var rent by remember { mutableStateOf("") }
    var deposit by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    var showRoomPicker by remember { mutableStateOf(false) } // NEW
    var showConfirmDialog by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    val dateError by remember {
        derivedStateOf {
            if (joinDate.isBlank() || parseDate(joinDate) != null) null
            else "Enter a valid date (YYYY-MM-DD)"
        }
    }
    val isFormValid by remember {
        derivedStateOf {
            selectedRoom != null && joinDate.isNotBlank() && dateError == null &&
                    rent.isNotBlank() && deposit.isNotBlank()
        }
    }

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = AppText,
        unfocusedTextColor = AppText,
        disabledTextColor = AppMuted,
        errorTextColor = AppText,
        focusedLabelColor = AppNavy,
        unfocusedLabelColor = AppMuted,
        disabledLabelColor = AppMuted,
        errorLabelColor = AppRed,
        focusedBorderColor = AppNavy,
        unfocusedBorderColor = AppBorder,
        disabledBorderColor = AppBorder,
        errorBorderColor = AppRed,
        errorSupportingTextColor = AppRed,
        cursorColor = AppNavy
    )

    Scaffold(
        containerColor = AppBackground,
        topBar = {
            TopAppBar(
                title = { Text("Re-admit Resident", fontWeight = FontWeight.Bold, color = AppText) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = AppText)
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
                person?.name ?: "Past Resident",
                style = MaterialTheme.typography.headlineSmall,
                color = AppText,
                fontWeight = FontWeight.Bold
            )

            ResidentDetailsCard(phone = person?.phone, govtId = person?.govtIdNumber)

            SectionHeader("New Stay Details")

            // ✅ FIXED: Use Box + pointerInput for reliable mobile taps
            val currentRoom = selectedRoom
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = currentRoom?.room?.roomNumber ?: "",
                    onValueChange = {},
                    readOnly = true,
                    singleLine = true,
                    label = { Text("Select Room *") },
                    placeholder = { Text("Tap to search rooms", color = AppMuted) },
                    trailingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Search rooms", tint = AppNavy)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = FieldShape,
                    colors = textFieldColors,
                    supportingText = {
                        if (currentRoom != null) {
                            Text(
                                "Beds: ${currentRoom.room.bedCount} · Vacancy: ${currentRoom.vacancy}",
                                color = AppMuted
                            )
                        }
                    }
                )

                // Overlay: topmost child, catches the tap
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable { showRoomPicker = true }
                )
            }

            OutlinedTextField(
                value = joinDate,
                onValueChange = { joinDate = it.trim().take(10) },
                label = { Text("New Join Date (YYYY-MM-DD) *") },
                modifier = Modifier.fillMaxWidth(),
                shape = FieldShape,
                colors = textFieldColors,
                isError = dateError != null,
                supportingText = dateError?.let { { Text(it) } },
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.CalendarToday, "Pick date", tint = AppNavy)
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
            )

            OutlinedTextField(
                value = rent,
                onValueChange = { rent = it.filter(Char::isDigit).take(5) },
                label = { Text("New Monthly Rent (₹) *") },
                modifier = Modifier.fillMaxWidth(),
                shape = FieldShape,
                colors = textFieldColors,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
            )

            OutlinedTextField(
                value = deposit,
                onValueChange = { deposit = it.filter(Char::isDigit).take(5) },
                label = { Text("New Deposit Collected (₹) *") },
                modifier = Modifier.fillMaxWidth(),
                shape = FieldShape,
                colors = textFieldColors,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number,imeAction = ImeAction.Done),
                //keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
            )

            Text(
                "This will create a new active stay for this resident. Their past records remain intact.",
                style = MaterialTheme.typography.bodySmall,
                color = AppMuted
            )

            SubmitButton(
                text = "Re-admit Resident",
                enabled = isFormValid,
                onClick = { showConfirmDialog = true }
            )
        }
    }

    // ✅ Room Picker Dialog
    if (showRoomPicker) {
        RoomPickerDialog(
            rooms = availableRooms,
            onDismiss = { showRoomPicker = false },
            onSelect = { room ->
                selectedRoom = room
                if (rent.isEmpty()) rent = (room.room.defaultRentPerBed / PAISE).toString()
            }
        )
    }

    if (showDatePicker) {
        JoinDatePickerDialog(
            initialDate = joinDate,
            onDismiss = { showDatePicker = false },
            onPicked = { joinDate = it; showDatePicker = false }
        )
    }

    if (showConfirmDialog) {
        ConfirmReAdmitDialog(
            name = person?.name ?: "this resident",
            room = selectedRoom?.room?.roomNumber ?: "—",
            joinDate = joinDate,
            rent = rent,
            deposit = deposit,
            onDismiss = { showConfirmDialog = false },
            onConfirm = {
                showConfirmDialog = false
                selectedRoom?.let { room ->
                    scope.launch {
                        val ok = viewModel.reAdmitResident(
                            room.room.id, joinDate, rent.toPaise(), deposit.toPaise()
                        )
                        if (ok) onBackClick()
                    }
                }
            }
        )
    }
}

// ---------- Reusable pieces ----------

@Composable
private fun SubmitButton(text: String, enabled: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp).height(52.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = AppNavy,
            contentColor = Color.White,
            disabledContainerColor = AppBorder,
            disabledContentColor = AppMuted
        )
    ) {
        Text(text, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
private fun DialogAction(
    text: String,
    color: Color,
    bold: Boolean = false,
    onClick: () -> Unit
) {
    TextButton(onClick = onClick) {
        Text(text, color = color, fontWeight = if (bold) FontWeight.Bold else null)
    }
}

@Composable
private fun ResidentDetailsCard(phone: String?, govtId: String?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = AppCard),
        border = BorderStroke(1.dp, AppBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(18.dp)) {
            SectionHeader("Past Resident Details")
            Spacer(Modifier.height(10.dp))
            DetailRow("Phone", phone ?: "—")
            DetailRow("Govt ID", govtId ?: "—")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun JoinDatePickerDialog(
    initialDate: String,
    onDismiss: () -> Unit,
    onPicked: (String) -> Unit
) {
    val state = rememberDatePickerState(
        initialSelectedDateMillis = parseDate(initialDate)
            ?.atStartOfDay(ZoneOffset.UTC)?.toInstant()?.toEpochMilli()
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            DialogAction("OK", AppNavy, bold = true) {
                state.selectedDateMillis
                    ?.let { Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate().toString() }
                    ?.let(onPicked)
                    ?: onDismiss()
            }
        },
        dismissButton = { DialogAction("Cancel", AppMuted, onClick = onDismiss) }
    ) {
        DatePicker(state = state)
    }
}

@Composable
private fun ConfirmReAdmitDialog(
    name: String,
    room: String,
    joinDate: String,
    rent: String,
    deposit: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = AppCard,
        titleContentColor = AppText,
        textContentColor = AppMuted,
        title = { Text("Confirm re-admission?", fontWeight = FontWeight.Bold) },
        text = {
            Text(
                "Re-admit $name to Room $room starting $joinDate?\n\n" +
                        "New Rent: ₹$rent\nNew Deposit: ₹$deposit\n\n" +
                        "A new active stay will be created."
            )
        },
        confirmButton = { DialogAction("Yes, re-admit", AppNavy, bold = true, onClick = onConfirm) },
        dismissButton = { DialogAction("Cancel", AppMuted, onClick = onDismiss) }
    )
}

@Composable
private fun SectionHeader(title: String) {
    Column(Modifier.padding(top = 4.dp)) {
        Text(
            title,
            style = MaterialTheme.typography.titleMedium,
            color = AppText,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(6.dp))
        Box(Modifier.width(32.dp).height(2.dp).background(AppNavy))
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