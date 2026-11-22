package com.example.hostelmanager.ui.residents

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hostelmanager.data.RoomWithOccupancy
import com.example.hostelmanager.ui.theme.*
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

private const val PAISE = 100L
private val FieldShape = RoundedCornerShape(12.dp)

private val RoomWithOccupancy.vacancy get() = room.bedCount - occupiedBeds
private fun String.toPaise() = (toLongOrNull() ?: 0L) * PAISE

// Length check first: avoids throwing a parse exception on every partial keystroke.
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
    var expandedRoom by remember { mutableStateOf(false) }
    var joinDate by remember { mutableStateOf(today) }
    var rent by remember { mutableStateOf("") }
    var deposit by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    var showConfirmDialog by remember { mutableStateOf(false) }

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

            ExposedDropdownMenuBox(
                expanded = expandedRoom,
                onExpandedChange = { expandedRoom = !expandedRoom }
            ) {
                AppTextField(
                    value = selectedRoom?.room?.roomNumber ?: "",
                    onValueChange = {},
                    label = "Select Room *",
                    readOnly = true,
                    modifier = Modifier.menuAnchor(),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedRoom) }
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
                        availableRooms.forEach { room ->
                            DropdownMenuItem(
                                text = { Text("Room ${room.room.roomNumber} (Vacancy: ${room.vacancy})") },
                                onClick = {
                                    selectedRoom = room
                                    expandedRoom = false
                                    // Auto-fill rent on selection (no LaunchedEffect needed)
                                    if (rent.isEmpty()) rent = (room.room.defaultRentPerBed / PAISE).toString()
                                }
                            )
                        }
                    }
                }
            }

            AppTextField(
                value = joinDate,
                onValueChange = { joinDate = it.trim().take(10) },
                label = "New Join Date (YYYY-MM-DD) *",
                error = dateError,
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.CalendarToday, "Pick date", tint = AppNavy)
                    }
                }
            )

            AppTextField(
                value = rent,
                onValueChange = { rent = it.filter(Char::isDigit).take(5) },
                label = "New Monthly Rent (₹) *"
            )

            AppTextField(
                value = deposit,
                onValueChange = { deposit = it.filter(Char::isDigit).take(5) },
                label = "New Deposit Collected (₹) *",
                onDone = { if (isFormValid) showConfirmDialog = true }
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
private fun appFieldColors() = OutlinedTextFieldDefaults.colors(
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

@Composable
private fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    readOnly: Boolean = false,
    error: String? = null,
    keyboardType: KeyboardType = KeyboardType.Number,
    onDone: (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    val focusManager = LocalFocusManager.current
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        readOnly = readOnly,
        singleLine = true,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth().then(modifier),
        shape = FieldShape,
        colors = appFieldColors(),
        isError = error != null,
        supportingText = error?.let { { Text(it) } },
        trailingIcon = trailingIcon,
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            imeAction = if (onDone != null) ImeAction.Done else ImeAction.Next
        ),
        keyboardActions = KeyboardActions(
            onNext = { focusManager.moveFocus(FocusDirection.Down) },
            onDone = {
                focusManager.clearFocus()
                onDone?.invoke()
            }
        )
    )
}

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

// DatePickerState is only created while the dialog is visible.
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