package com.example.hostelmanager.ui.residents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hostelmanager.ui.components.ImagePickerField
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditResidentScreen(
    personId: String,
    onBackClick: () -> Unit,
    viewModel: EditResidentViewModel = viewModel()
) {
    LaunchedEffect(personId) { viewModel.setPersonId(personId) }

    val person by viewModel.person.collectAsState()
    val phoneError by viewModel.phoneError.collectAsState()
    val govtIdError by viewModel.govtIdError.collectAsState()
    val emergencyError by viewModel.emergencyError.collectAsState()

    val scope = rememberCoroutineScope()

    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    val idTypes = listOf("Aadhaar", "Voter ID", "Driving License")
    var selectedIdType by remember { mutableStateOf("Aadhaar") }
    var govtId by remember { mutableStateOf("") }
    var emergency by remember { mutableStateOf("") }
    var dob by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var guardian by remember { mutableStateOf("") }
    val genders = listOf("Male", "Female", "Other")
    var expandedGender by remember { mutableStateOf(false) }
    var selectedGender by remember { mutableStateOf("Male") }
    var expandedIdType by remember { mutableStateOf(false) }

    var govtFrontPath by remember { mutableStateOf<String?>(null) }
    var govtBackPath by remember { mutableStateOf<String?>(null) }
    var profilePhotoPath by remember { mutableStateOf<String?>(null) }

    val dobDatePickerState = rememberDatePickerState()
    var showDobDatePicker by remember { mutableStateOf(false) }

    var isLoaded by remember { mutableStateOf(false) }

    // Pre-fill the form ONCE when the resident loads
    LaunchedEffect(person) {
        val p = person
        if (p != null && !isLoaded) {
            name = p.name
            phone = p.phone
            govtId = p.govtIdNumber
            selectedIdType = detectIdType(p.govtIdNumber)
            emergency = p.emergencyContact
            dob = p.dateOfBirth
            address = p.address
            guardian = p.guardianName
            selectedGender = if (p.gender in genders) p.gender else "Male"
            govtFrontPath = p.govtIdPhotoPath
            govtBackPath = p.govtIdBackPath
            profilePhotoPath = p.profilePhotoPath
            isLoaded = true
        }
    }

    LaunchedEffect(govtId, selectedIdType) {
        if (selectedIdType == "Voter ID" || selectedIdType == "Driving License") {
            val cleaned = govtId.replace(" ", "").replace("-", "").uppercase()
            if (cleaned != govtId) govtId = cleaned
        }
    }

    val isPhoneValid = phone.matches(Regex("^\\d{10}$"))
    val isEmergencyValid = emergency.matches(Regex("^\\d{10}$"))
    val isIdValid = when (selectedIdType) {
        "Aadhaar" -> govtId.matches(Regex("^[2-9][0-9]{11}$"))
        "Voter ID" -> govtId.matches(Regex("^[A-Z]{3}[0-9]{7}$"))
        "Driving License" -> govtId.matches(Regex("^(([A-Z]{2}[0-9]{2})( )?|([A-Z]{2}-[0-9]{2}))((19|20)[0-9]{2})[0-9]{7}$"))
        else -> false
    }

    val isFormValid = name.isNotBlank() && isPhoneValid && isIdValid && isEmergencyValid &&
            dob.isNotBlank() &&
            !govtFrontPath.isNullOrBlank() && !govtBackPath.isNullOrBlank()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Resident") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (!isLoaded) {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Personal Details", style = MaterialTheme.typography.titleMedium)

                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name *") }, modifier = Modifier.fillMaxWidth())

                OutlinedTextField(
                    value = phone, onValueChange = { phone = it },
                    label = { Text("Phone (10 digits) *") },
                    modifier = Modifier.fillMaxWidth(),
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
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = expandedIdType, onDismissRequest = { expandedIdType = false }) {
                        idTypes.forEach { type ->
                            DropdownMenuItem(text = { Text(type) }, onClick = { selectedIdType = type; expandedIdType = false })
                        }
                    }
                }

                OutlinedTextField(
                    value = govtId, onValueChange = { govtId = it },
                    label = { Text("ID Number *") },
                    modifier = Modifier.fillMaxWidth(),
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
                    value = emergency, onValueChange = { emergency = it },
                    label = { Text("Emergency Contact (10 digits) *") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = (emergency.isNotBlank() && !isEmergencyValid) || emergencyError != null,
                    supportingText = {
                        if (emergency.isNotBlank() && !isEmergencyValid) Text("Must be exactly 10 digits")
                        else if (emergencyError != null) Text(emergencyError!!)
                    }
                )

                OutlinedTextField(
                    value = dob, onValueChange = { dob = it },
                    label = { Text("Date of Birth (YYYY-MM-DD) *") },
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(onClick = { showDobDatePicker = true }) {
                            Icon(Icons.Default.CalendarToday, contentDescription = "Pick DOB")
                        }
                    }
                )

                OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Address") }, modifier = Modifier.fillMaxWidth())

                ExposedDropdownMenuBox(expanded = expandedGender, onExpandedChange = { expandedGender = !expandedGender }) {
                    OutlinedTextField(
                        value = selectedGender, onValueChange = { }, readOnly = true,
                        label = { Text("Gender") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedGender) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = expandedGender, onDismissRequest = { expandedGender = false }) {
                        genders.forEach { gender ->
                            DropdownMenuItem(text = { Text(gender) }, onClick = { selectedGender = gender; expandedGender = false })
                        }
                    }
                }

                OutlinedTextField(value = guardian, onValueChange = { guardian = it }, label = { Text("Guardian Name") }, modifier = Modifier.fillMaxWidth())

                Spacer(modifier = Modifier.height(16.dp))
                Text("Documents & Photo", style = MaterialTheme.typography.titleMedium)

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
                        scope.launch {
                            val isSuccess = viewModel.updateResident(
                                name, phone, govtId, emergency, dob, address, selectedGender, guardian,
                                govtFrontPath ?: "", govtBackPath ?: "", profilePhotoPath ?: ""
                            )
                            if (isSuccess) onBackClick()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    enabled = isFormValid
                ) {
                    Text("Save Changes")
                }
            }
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
            dismissButton = { TextButton(onClick = { showDobDatePicker = false }) { Text("Cancel") } }
        ) { DatePicker(state = dobDatePickerState) }
    }
}

// Guess the ID type from the saved number (we never stored the type)
private fun detectIdType(govtId: String): String {
    return when {
        govtId.matches(Regex("^[2-9][0-9]{11}$")) -> "Aadhaar"
        govtId.matches(Regex("^[A-Z]{3}[0-9]{7}$")) -> "Voter ID"
        govtId.matches(Regex("^(([A-Z]{2}[0-9]{2})( )?|([A-Z]{2}-[0-9]{2}))((19|20)[0-9]{2})[0-9]{7}$")) -> "Driving License"
        else -> "Aadhaar"
    }
}