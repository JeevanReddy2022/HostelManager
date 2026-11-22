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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hostelmanager.ui.theme.*
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val REFUND_MODES = listOf("Cash", "UPI", "Bank Transfer")

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
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val today = remember { LocalDate.now().toString() }

    var checkoutDate by remember { mutableStateOf(today) }
    var refundAmount by remember { mutableStateOf("") }
    var refundDate by remember { mutableStateOf(today) }
    var refundMode by remember { mutableStateOf(REFUND_MODES.first()) }
    var expandedMode by remember { mutableStateOf(false) }
    var showConfirmDialog by remember { mutableStateOf(false) }
    var prefilled by remember { mutableStateOf(false) }

    LaunchedEffect(activeStay) {
        val stay = activeStay
        if (stay != null && !prefilled) {
            refundAmount = (stay.depositCollected / 100).toString()
            prefilled = true
        }
    }

    val fieldColors = appTextFieldColors()

    val moveNextOrHide: () -> Unit = remember(focusManager, keyboardController) {
        {
            if (!focusManager.moveFocus(FocusDirection.Down)) {
                focusManager.clearFocus()
                keyboardController?.hide()
            }
        }
    }

    val hideKeyboard: () -> Unit = remember(focusManager, keyboardController) {
        {
            focusManager.clearFocus()
            keyboardController?.hide()
        }
    }

    // Derived validation states prevent full-screen recomposition on every keystroke
    val depositPaise = activeStay?.depositCollected ?: 0L
    val refundPaise by remember {
        derivedStateOf { (refundAmount.toLongOrNull() ?: -1L).let { if (it >= 0) it * 100 else -1L } }
    }
    val retainedPaise by remember {
        derivedStateOf { (depositPaise - refundPaise).coerceAtLeast(0L) }
    }
    val checkoutDateError by remember(activeStay?.joinDate) {
        derivedStateOf { validateDate(checkoutDate, minDate = activeStay?.joinDate, label = "Check-out date") }
    }
    val refundDateError by remember {
        derivedStateOf { validateDate(refundDate, label = "Refund date") }
    }
    val isRefundValid by remember(depositPaise) {
        derivedStateOf { refundPaise in 0..depositPaise }
    }
    val needsRefundDetails by remember {
        derivedStateOf { refundPaise > 0 }
    }
    val isFormValid by remember(activeStay) {
        derivedStateOf {
            activeStay != null &&
                    checkoutDateError == null &&
                    isRefundValid &&
                    (!needsRefundDetails || (refundMode.isNotBlank() && refundDateError == null))
        }
    }

    Scaffold(
        containerColor = AppBackground,
        topBar = {
            TopAppBar(
                title = { Text("Check Out", fontWeight = FontWeight.Bold, color = AppText) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = AppText)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AppBackground)
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
                text = person?.name ?: "Resident",
                style = MaterialTheme.typography.headlineSmall,
                color = AppText,
                fontWeight = FontWeight.Bold
            )

            // Current Stay Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = AppCard),
                border = BorderStroke(1.dp, AppBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    SectionHeader("Current Stay")
                    Spacer(Modifier.height(10.dp))
                    DetailRow("Room", currentRoom?.roomNumber ?: "—")
                    DetailRow("Monthly Rent", "₹${(activeStay?.monthlyRent ?: 0L) / 100}")
                    DetailRow("Joined", activeStay?.joinDate ?: "—")
                    DetailRow("Deposit held", "₹${depositPaise / 100}")
                }
            }

            checkoutError?.let { BannerMessage(text = it, textColor = AppRed, bgColor = AppSoftRed) }

            SectionHeader("Check-out Details")

            DateInputField(
                value = checkoutDate,
                onValueChange = { checkoutDate = it },
                label = "Check-out Date (YYYY-MM-DD) *",
                errorMessage = checkoutDateError,
                imeAction = ImeAction.Next,
                onImeAction = moveNextOrHide,
                colors = fieldColors
            )

            SectionHeader("Deposit Settlement")

            OutlinedTextField(
                value = refundAmount,
                onValueChange = { refundAmount = it.filter(Char::isDigit) },
                label = { Text("Refund Amount (₹) *") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = fieldColors,
                isError = refundAmount.isNotBlank() && !isRefundValid,
                supportingText = {
                    Text(
                        if (refundAmount.isNotBlank() && !isRefundValid)
                            "Cannot exceed the deposit held (₹${depositPaise / 100})"
                        else "Enter 0 if nothing is being refunded."
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { hideKeyboard() })
            )

            if (isRefundValid && retainedPaise > 0) {
                BannerMessage(
                    text = "₹${retainedPaise / 100} will be retained as deductions.",
                    textColor = AppGreen,
                    bgColor = AppSoftGreen
                )
            }

            if (needsRefundDetails) {
                ExposedDropdownMenuBox(
                    expanded = expandedMode,
                    onExpandedChange = { expandedMode = !expandedMode }
                ) {
                    OutlinedTextField(
                        value = refundMode,
                        onValueChange = {},
                        readOnly = true,
                        singleLine = true,
                        label = { Text("Refund Mode *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedMode) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        shape = RoundedCornerShape(12.dp),
                        colors = fieldColors
                    )
                    ExposedDropdownMenu(
                        expanded = expandedMode,
                        onDismissRequest = { expandedMode = false }
                    ) {
                        REFUND_MODES.forEach { mode ->
                            DropdownMenuItem(
                                text = { Text(mode) },
                                onClick = { refundMode = mode; expandedMode = false }
                            )
                        }
                    }
                }

                DateInputField(
                    value = refundDate,
                    onValueChange = { refundDate = it },
                    label = "Refund Date (YYYY-MM-DD) *",
                    errorMessage = refundDateError,
                    imeAction = ImeAction.Done,
                    onImeAction = hideKeyboard,
                    colors = fieldColors
                )
            }

            Text(
                "The stay will be closed and the bed becomes free. All records are kept.",
                style = MaterialTheme.typography.bodySmall,
                color = AppMuted
            )

            Button(
                onClick = { showConfirmDialog = true },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp).height(52.dp),
                shape = RoundedCornerShape(14.dp),
                enabled = isFormValid,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppRed,
                    contentColor = Color.White,
                    disabledContainerColor = AppBorder,
                    disabledContentColor = AppMuted
                )
            ) {
                Text("Check Out Resident", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            }
        }
    }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            shape = RoundedCornerShape(24.dp),
            containerColor = AppCard,
            titleContentColor = AppText,
            textContentColor = AppMuted,
            title = { Text("Confirm check-out?", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    buildString {
                        append("Check out ${person?.name ?: "this resident"} from Room ${currentRoom?.roomNumber ?: "—"} on ${checkoutDate.trim()}?\n\n")
                        append("Deposit held: ₹${depositPaise / 100}\n")
                        append("Refunding: ₹${refundPaise.coerceAtLeast(0L) / 100}")
                        if (needsRefundDetails) append(" via $refundMode")
                        if (retainedPaise > 0) append("\nRetained: ₹${retainedPaise / 100}")
                        append("\n\nThe bed becomes free. Records are kept.")
                    }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showConfirmDialog = false
                    scope.launch {
                        val success = viewModel.checkoutResident(
                            checkoutDate.trim(),
                            refundPaise,
                            refundMode.takeIf { needsRefundDetails },
                            refundDate.trim().takeIf { needsRefundDetails }
                        )
                        if (success) onCheckoutComplete()
                    }
                }) { Text("Yes, check out", color = AppRed, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) { Text("Cancel", color = AppMuted) }
            }
        )
    }
}

// --- Pure validation helper ---
private fun validateDate(raw: String, minDate: String? = null, label: String): String? {
    val trimmed = raw.trim()
    if (trimmed.isEmpty()) return "$label is required."
    val parsed = runCatching { LocalDate.parse(trimmed, DateTimeFormatter.ISO_LOCAL_DATE) }.getOrNull()
        ?: return "Enter a valid date in YYYY-MM-DD format."
    val minParsed = minDate?.let { runCatching { LocalDate.parse(it.trim(), DateTimeFormatter.ISO_LOCAL_DATE) }.getOrNull() }
    if (minParsed != null && parsed.isBefore(minParsed)) {
        return "Cannot be before the join date ($minDate)."
    }
    return null
}

// --- Reusable UI components ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    errorMessage: String?,
    imeAction: ImeAction,
    onImeAction: () -> Unit,
    colors: TextFieldColors
) {
    var showPicker by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = value,
        onValueChange = { input ->
            onValueChange(input.filter { it.isDigit() || it == '-' }.take(10))
        },
        label = { Text(label) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = colors,
        isError = errorMessage != null,
        supportingText = errorMessage?.let { { Text(it) } },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = imeAction),
        keyboardActions = KeyboardActions(
            onNext = { onImeAction() },
            onDone = { onImeAction() }
        ),
        trailingIcon = {
            IconButton(onClick = { showPicker = true }) {
                Icon(Icons.Default.CalendarToday, contentDescription = "Pick date", tint = AppNavy)
            }
        }
    )

    if (showPicker) {
        val pickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { millis ->
                        val date = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                        onValueChange(date.format(DateTimeFormatter.ISO_LOCAL_DATE))
                    }
                    showPicker = false
                }) { Text("OK", color = AppNavy, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) { Text("Cancel", color = AppMuted) }
            }
        ) { DatePicker(state = pickerState) }
    }
}

@Composable
private fun BannerMessage(text: String, textColor: Color, bgColor: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .padding(14.dp)
    ) {
        Text(text, color = textColor, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun appTextFieldColors(): TextFieldColors = OutlinedTextFieldDefaults.colors(
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

@Composable
private fun SectionHeader(title: String) {
    Column(modifier = Modifier.padding(top = 8.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium, color = AppText, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(6.dp))
        Box(modifier = Modifier.width(32.dp).height(2.dp).background(AppNavy))
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = AppMuted, modifier = Modifier.weight(0.4f))
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            color = AppText,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(0.6f)
        )
    }
}