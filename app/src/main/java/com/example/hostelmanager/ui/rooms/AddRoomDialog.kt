package com.example.hostelmanager.ui.rooms

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


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

    // Validation values
    val roomNumberValid = roomNumber.isNotBlank() &&
            (roomNumber.toLongOrNull()?.let { it > 0 } ?: true)

    val floorValid = floor.toIntOrNull()?.let { it >= 0 } == true

    val bedCountValid = bedCount.toIntOrNull()?.let { it > 0 } == true

    val rentValue = rent.toLongOrNull()
    val rentValid = rentValue != null && rentValue > 0

    // Warning for low rent
    val showRentWarning = rentValid && rentValue!! < 4000

    val isInputValid = roomNumberValid &&
            floorValid &&
            bedCountValid &&
            rentValid

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Room") },
        text = {
            Column {
                // Room Number
                OutlinedTextField(
                    value = roomNumber,
                    onValueChange = {
                        roomNumber = it
                        onErrorCleared()
                    },
                    label = { Text("Room Number *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = !roomNumberValid || errorMessage != null,
                    supportingText = {
                        when {
                            errorMessage != null -> Text(errorMessage)
                            !roomNumberValid -> Text("Room number must be greater than zero")
                        }
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Floor
                OutlinedTextField(
                    value = floor,
                    onValueChange = { floor = it },
                    label = { Text("Floor") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = !floorValid,
                    supportingText = {
                        if (!floorValid) {
                            Text("Floor cannot be negative or empty")
                        }
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Number of Beds
                OutlinedTextField(
                    value = bedCount,
                    onValueChange = { bedCount = it },
                    label = { Text("Number of Beds") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = !bedCountValid,
                    supportingText = {
                        if (!bedCountValid) {
                            Text("Number of beds must be greater than zero")
                        }
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Rent
                OutlinedTextField(
                    value = rent,
                    onValueChange = { rent = it },
                    label = { Text("Rent per Bed (₹)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = rent.isNotBlank() && !rentValid,
                    supportingText = {
                        when {
                            rent.isBlank() -> Text("Enter rent per bed")
                            !rentValid -> Text("Rent must be greater than zero")
                            showRentWarning -> Text(
                                text = "Warning: Rent is low (below ₹4,000)",
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(
                        roomNumber,
                        floor.toInt(),
                        bedCount.toInt(),
                        rentValue!! * 100
                    )
                },
                enabled = isInputValid && errorMessage == null
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
