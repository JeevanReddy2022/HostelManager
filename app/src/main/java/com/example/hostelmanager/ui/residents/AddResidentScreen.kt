package com.example.hostelmanager.ui.residents

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.setValue
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
import com.example.hostelmanager.ui.components.ImagePickerField
import com.example.hostelmanager.ui.theme.AppBackground
import com.example.hostelmanager.ui.theme.AppBorder
import com.example.hostelmanager.ui.theme.AppMuted
import com.example.hostelmanager.ui.theme.AppNavy
import com.example.hostelmanager.ui.theme.AppRed
import com.example.hostelmanager.ui.theme.AppText
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddResidentScreen(
    hostelId: String,
    onBackClick: () -> Unit,
    viewModel: ResidentViewModel = viewModel()
) {
    LaunchedEffect(hostelId) { viewModel.setHostelId(hostelId) }

    val roomsWithOccupancy by viewModel.roomsForHostel.collectAsState()
    val availableRooms = roomsWithOccupancy.filter { (it.room.bedCount - it.occupiedBeds) > 0 }

    val phoneError by viewModel.phoneError.collectAsState()
    val govtIdError by viewModel.govtIdError.collectAsState()
    val emergencyError by viewModel.emergencyError.collectAsState()

    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    // High-contrast, warm-colored text field styling
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

    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    val idTypes = listOf("Aadhaar", "Voter ID", "Driving License")
    var selectedIdType by remember { mutableStateOf("Aadhaar") }
    var govtId by remember { mutableStateOf("") }

    var emergency by remember { mutableStateOf("") }
    var dob by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var guardian by remember { mutableStateOf("") }
    var joinDate by remember { mutableStateOf("") }
    var rent by remember { mutableStateOf("") }
    var deposit by remember { mutableStateOf("") }

    var govtFrontPath by remember { mutableStateOf<String?>(null) }
    var govtBackPath by remember { mutableStateOf<String?>(null) }
    var profilePhotoPath by remember { mutableStateOf<String?>(null) }

    val genders = listOf("Male", "Female", "Other")
    var expandedGender by remember { mutableStateOf(false) }
    var selectedGender by remember { mutableStateOf("Male") }

    var expandedIdType by remember { mutableStateOf(false) }
    var expandedRoomSelect by remember { mutableStateOf(false) }
    var selectedRoom by remember { mutableStateOf<RoomWithOccupancy?>(null) }

    val joinDatePickerState = rememberDatePickerState()
    var showJoinDatePicker by remember { mutableStateOf(false) }
    val dobDatePickerState = rememberDatePickerState()
    var showDobDatePicker by remember { mutableStateOf(false) }

    LaunchedEffect(govtId, selectedIdType) {
        if (selectedIdType == "Voter ID" || selectedIdType == "Driving License") {
            val cleaned = govtId.replace(" ", "").replace("-", "").uppercase()
            if (cleaned != govtId) govtId = cleaned
        }
    }

    LaunchedEffect(selectedRoom) {
        if (selectedRoom != null && rent.isEmpty()) {
            rent = (selectedRoom!!.room.defaultRentPerBed / 100).toString()
        }
    }

    val isPhoneValid = phone.matches(Regex("^\\d{10}$"))
    val isEmergencyValid = emergency.matches(Regex("^\\d{10}$"))
    val isSameContact = phone.isNotBlank() && emergency.isNotBlank() && phone == emergency

    val isIdValid = when (selectedIdType) {
        "Aadhaar" -> govtId.matches(Regex("^[2-9][0-9]{11}$"))
        "Voter ID" -> govtId.matches(Regex("^[A-Z]{3}[0-9]{7}$"))
        "Driving License" -> govtId.matches(Regex("^(([A-Z]{2}[0-9]{2})( )?|([A-Z]{2}-[0-9]{2}))((19|20)[0-9]{2})[0-9]{7}$"))
        else -> false
    }

    val isDobValid = dob.isNotBlank() && try {
        LocalDate.parse(dob, DateTimeFormatter.ISO_LOCAL_DATE) <= LocalDate.now()
    } catch (e: Exception) { false }

    val isJoinDateValid = joinDate.isNotBlank() && try {
        LocalDate.parse(joinDate, DateTimeFormatter.ISO_LOCAL_DATE)
        true
    } catch (e: Exception) { false }

    val isFormValid = name.isNotBlank() && isPhoneValid && isIdValid && isEmergencyValid &&
            !isSameContact && selectedRoom != null && isJoinDateValid && isDobValid &&
            rent.isNotBlank() && deposit.isNotBlank() &&
            !govtFrontPath.isNullOrBlank() && !govtBackPath.isNullOrBlank()

    Scaffold(
        containerColor = AppBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Add Resident",
                        fontWeight = FontWeight.Bold,
                        color = AppText
                    )
                },
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SectionHeader("Personal Details")

            OutlinedTextField(
                value = name, onValueChange = { name = it },
                label = { Text("Name *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = textFieldColors,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
            )

            OutlinedTextField(
                value = phone, onValueChange = { input -> phone = input.filter { it.isDigit() } },
                label = { Text("Phone (10 digits) *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = textFieldColors,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                isError = (phone.isNotBlank() && !isPhoneValid) || phoneError != null,
                supportingText = {
                    if (phone.isNotBlank() && !isPhoneValid) Text("Must be exactly 10 digits")
                    else if (phoneError != null) Text(phoneError!!)
                }
            )

            ExposedDropdownMenuBox(expanded = expandedIdType, onExpandedChange = { expandedIdType = !expandedIdType }) {
                OutlinedTextField(
                    value = selectedIdType, onValueChange = { }, readOnly = true,
                    label = { Text("ID Type *") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedIdType) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = textFieldColors
                )
                ExposedDropdownMenu(expanded = expandedIdType, onDismissRequest = { expandedIdType = false }) {
                    idTypes.forEach { type ->
                        DropdownMenuItem(text = { Text(type) }, onClick = { selectedIdType = type; govtId = ""; expandedIdType = false })
                    }
                }
            }

            OutlinedTextField(
                value = govtId, onValueChange = { govtId = it },
                label = { Text("ID Number *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = textFieldColors,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                isError = (govtId.isNotBlank() && !isIdValid) || govtIdError != null,
                supportingText = {
                    if (govtId.isNotBlank() && !isIdValid) {
                        when (selectedIdType) {
                            "Aadhaar" -> Text("Must be 12 digits, starting with 2-9")
                            "Voter ID" -> Text("Format: 3 Letters + 7 Digits")
                            "Driving License" -> Text("Format: State+RTO+Year+7 Digits")
                            else -> Text("Invalid format")
                        }
                    } else if (govtIdError != null) Text(govtIdError!!)
                }
            )

            OutlinedTextField(
                value = emergency, onValueChange = { input -> emergency = input.filter { it.isDigit() } },
                label = { Text("Emergency Contact (10 digits) *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = textFieldColors,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                isError = (emergency.isNotBlank() && !isEmergencyValid) || emergencyError != null || isSameContact,
                supportingText = {
                    if (isSameContact) {
                        Text("Emergency contact cannot be the same as the primary phone number")
                    } else if (emergency.isNotBlank() && !isEmergencyValid) {
                        Text("Must be exactly 10 digits")
                    } else if (emergencyError != null) {
                        Text(emergencyError!!)
                    }
                }
            )

            OutlinedTextField(
                value = dob, onValueChange = { dob = it },
                label = { Text("Date of Birth (YYYY-MM-DD) *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = textFieldColors,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                isError = dob.isNotBlank() && !isDobValid,
                supportingText = {
                    if (dob.isNotBlank() && !isDobValid) {
                        Text("Invalid date or date is in the future")
                    }
                },
                trailingIcon = {
                    IconButton(onClick = { showDobDatePicker = true }) {
                        Icon(Icons.Default.CalendarToday, contentDescription = "Pick DOB", tint = AppNavy)
                    }
                }
            )

            OutlinedTextField(
                value = address, onValueChange = { address = it },
                label = { Text("Address") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = textFieldColors,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
            )

            ExposedDropdownMenuBox(expanded = expandedGender, onExpandedChange = { expandedGender = !expandedGender }) {
                OutlinedTextField(
                    value = selectedGender, onValueChange = { }, readOnly = true,
                    label = { Text("Gender") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedGender) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = textFieldColors
                )
                ExposedDropdownMenu(expanded = expandedGender, onDismissRequest = { expandedGender = false }) {
                    genders.forEach { gender ->
                        DropdownMenuItem(text = { Text(gender) }, onClick = { selectedGender = gender; expandedGender = false })
                    }
                }
            }

            OutlinedTextField(
                value = guardian, onValueChange = { guardian = it },
                label = { Text("Guardian Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = textFieldColors,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
            )

            Spacer(modifier = Modifier.height(8.dp))
            SectionHeader("Stay Details")

            ExposedDropdownMenuBox(expanded = expandedRoomSelect, onExpandedChange = { expandedRoomSelect = !expandedRoomSelect }) {
                OutlinedTextField(
                    value = selectedRoom?.room?.roomNumber ?: "", onValueChange = { }, readOnly = true,
                    label = { Text("Select Room *") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedRoomSelect) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = textFieldColors
                )
                ExposedDropdownMenu(expanded = expandedRoomSelect, onDismissRequest = { expandedRoomSelect = false }) {
                    if (availableRooms.isEmpty()) {
                        DropdownMenuItem(text = { Text("No rooms with vacancy") }, onClick = { expandedRoomSelect = false })
                    } else {
                        availableRooms.forEach { roomWithOcc ->
                            val vacancy = roomWithOcc.room.bedCount - roomWithOcc.occupiedBeds
                            DropdownMenuItem(
                                text = { Text("Room ${roomWithOcc.room.roomNumber} (Vacancy: $vacancy)") },
                                onClick = { selectedRoom = roomWithOcc; expandedRoomSelect = false }
                            )
                        }
                    }
                }
            }

            OutlinedTextField(
                value = joinDate, onValueChange = { joinDate = it },
                label = { Text("Join Date (YYYY-MM-DD) *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = textFieldColors,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                isError = joinDate.isNotBlank() && !isJoinDateValid,
                supportingText = {
                    if (joinDate.isNotBlank() && !isJoinDateValid) {
                        Text("Invalid date format. Use YYYY-MM-DD")
                    }
                },
                trailingIcon = {
                    IconButton(onClick = { showJoinDatePicker = true }) {
                        Icon(Icons.Default.CalendarToday, contentDescription = "Pick Date", tint = AppNavy)
                    }
                }
            )

            OutlinedTextField(
                value = rent,
                onValueChange = { input -> rent = input.filter { it.isDigit() } },
                label = { Text("Monthly Rent (₹)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = textFieldColors,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
            )

            OutlinedTextField(
                value = deposit,
                onValueChange = { input -> deposit = input.filter { it.isDigit() } },
                label = { Text("Deposit Collected (₹)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = textFieldColors,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
            )

            Spacer(modifier = Modifier.height(8.dp))
            SectionHeader("Documents & Photo")

            ImagePickerField(
                label = "Govt ID - Front",
                imagePath = govtFrontPath,
                isRequired = true,
                onImageCaptured = { govtFrontPath = it }
            )
            ImagePickerField(
                label = "Govt ID - Back",
                imagePath = govtBackPath,
                isRequired = true,
                onImageCaptured = { govtBackPath = it }
            )
            ImagePickerField(
                label = "Person Photo",
                imagePath = profilePhotoPath,
                isRequired = false,
                onImageCaptured = { profilePhotoPath = it }
            )

            Button(
                onClick = {
                    val rentInPaise = rent.toLongOrNull()?.times(100) ?: 0L
                    val depositInPaise = deposit.toLongOrNull()?.times(100) ?: 0L

                    scope.launch {
                        val isSuccess = viewModel.addResident(
                            hostelId, name, phone, govtId, emergency, dob, address, selectedGender, guardian,
                            selectedRoom!!.room.id, joinDate, rentInPaise, depositInPaise,
                            govtFrontPath ?: "", govtBackPath ?: "", profilePhotoPath ?: ""
                        )
                        if (isSuccess) onBackClick()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppNavy,
                    contentColor = Color.White,
                    disabledContainerColor = AppBorder,
                    disabledContentColor = AppMuted
                ),
                enabled = isFormValid
            ) {
                Text(
                    "Save Resident",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
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

    if (showDobDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDobDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    dobDatePickerState.selectedDateMillis?.let { millis ->
                        val date = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                        dob = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
                    }
                    showDobDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDobDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = dobDatePickerState)
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Column(modifier = Modifier.padding(top = 8.dp)) {
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