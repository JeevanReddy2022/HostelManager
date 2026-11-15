package com.example.hostelmanager.ui.rooms

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
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
    onConfirm: (roomNumber: String, floor: Int, bedCount: Int, rentPerBed: Long) -> Unit
) {
    var roomNumber by remember { mutableStateOf("") }
    var floor by remember { mutableStateOf("1") }
    var bedCount by remember { mutableStateOf("1") }
    var rent by remember { mutableStateOf("") }

    val isInputValid = roomNumber.isNotBlank() && rent.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Room") },
        text = {
            Column {
                OutlinedTextField(
                    value = roomNumber,
                    onValueChange = { roomNumber = it },
                    label = { Text("Room Number *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = floor,
                    onValueChange = { floor = it },
                    label = { Text("Floor") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = bedCount,
                    onValueChange = { bedCount = it },
                    label = { Text("Number of Beds") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = rent,
                    onValueChange = { rent = it },
                    label = { Text("Rent per Bed (₹)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    // Convert rupees typed by user into paise for the database
                    val rentInPaise = rent.toLongOrNull()?.times(100) ?: 0L
                    onConfirm(
                        roomNumber,
                        floor.toIntOrNull() ?: 1,
                        bedCount.toIntOrNull() ?: 1,
                        rentInPaise
                    )
                },
                enabled = isInputValid
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