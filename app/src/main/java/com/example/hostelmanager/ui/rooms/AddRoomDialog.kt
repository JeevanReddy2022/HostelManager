package com.example.hostelmanager.ui.rooms

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.hostelmanager.ui.theme.AppBorder
import com.example.hostelmanager.ui.theme.AppCard
import com.example.hostelmanager.ui.theme.AppGreen
import com.example.hostelmanager.ui.theme.AppMuted
import com.example.hostelmanager.ui.theme.AppNavy
import com.example.hostelmanager.ui.theme.AppRed
import com.example.hostelmanager.ui.theme.AppText

@Composable
fun AddRoomDialog(
    onDismiss: () -> Unit,
    onConfirm: (roomNumber: String, floor: Int, bedCount: Int, rentPerBed: Long) -> Unit,
    errorMessage: String? = null,
    onErrorCleared: () -> Unit = {}
) {
    var roomNumber by remember { mutableStateOf("") }
    var floor by remember { mutableStateOf("1") }
    var bedCount by remember { mutableStateOf("1") }
    var rent by remember { mutableStateOf("") }

    val roomNumberValid = roomNumber.isNotBlank() && (roomNumber.toLongOrNull()?.let { it > 0 } ?: true)
    val floorValid = floor.toIntOrNull()?.let { it >= 0 } == true
    val bedCountValid = bedCount.toIntOrNull()?.let { it > 0 } == true

    val rentValue = rent.toLongOrNull()
    val rentValid = rentValue != null && rentValue > 0
    val showRentWarning = rentValid && rentValue!! < 4000

    val isInputValid = roomNumberValid && floorValid && bedCountValid && rentValid

    // Explicit high-contrast colors for text fields
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
        errorSupportingTextColor = AppRed
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
        containerColor = AppCard,
        titleContentColor = AppText,
        textContentColor = AppText,
        title = {
            Text(
                "Add Room",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = roomNumber,
                    onValueChange = {
                        roomNumber = it
                        onErrorCleared()
                    },
                    label = { Text("Room Number *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = textFieldColors,
                    isError = !roomNumberValid || errorMessage != null,
                    supportingText = {
                        when {
                            errorMessage != null -> Text(errorMessage)
                            !roomNumberValid -> Text("Room number must be greater than zero")
                        }
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = floor,
                    onValueChange = { floor = it },
                    label = { Text("Floor") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = textFieldColors,
                    isError = !floorValid,
                    supportingText = { if (!floorValid) Text("Floor cannot be negative or empty") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = bedCount,
                    onValueChange = { bedCount = it },
                    label = { Text("Number of Beds") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = textFieldColors,
                    isError = !bedCountValid,
                    supportingText = { if (!bedCountValid) Text("Number of beds must be greater than zero") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = rent,
                    onValueChange = { rent = it },
                    label = { Text("Rent per Bed (₹)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = textFieldColors,
                    isError = rent.isNotBlank() && !rentValid,
                    supportingText = {
                        when {
                            rent.isBlank() -> Text("Enter rent per bed")
                            !rentValid -> Text("Rent must be greater than zero")
                            showRentWarning -> Text(
                                text = "Warning: Rent is low (below ₹4,000)",
                                color = AppRed
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(roomNumber, floor.toInt(), bedCount.toInt(), rentValue!! * 100)
                },
                enabled = isInputValid && errorMessage == null
            ) {
                Text("Add", color = AppGreen, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = AppMuted, fontWeight = FontWeight.Medium)
            }
        }
    )
}