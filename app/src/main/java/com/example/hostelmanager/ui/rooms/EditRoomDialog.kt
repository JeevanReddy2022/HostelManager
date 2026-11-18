package com.example.hostelmanager.ui.rooms

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.hostelmanager.data.RoomEntity

@Composable
fun EditRoomDialog(
    room: RoomEntity,
    occupiedBeds: Int,
    onDismiss: () -> Unit,
    onConfirm: (roomNumber: String, floor: Int, bedCount: Int, rentPerBedPaise: Long) -> Unit
) {
    // Pre-fill the boxes with the room's CURRENT values
    var roomNumber by remember { mutableStateOf(room.roomNumber) }
    var floor by remember { mutableStateOf(room.floor.toString()) }
    var beds by remember { mutableStateOf(room.bedCount.toString()) }
    var rent by remember { mutableStateOf((room.defaultRentPerBed / 100).toString()) }

    val bedsInt = beds.toIntOrNull() ?: 0
    val isBedsNumberValid = bedsInt >= 1
    val isBedsEnough = bedsInt >= occupiedBeds   // Safety rule
    val isFormValid = roomNumber.isNotBlank() && isBedsNumberValid && isBedsEnough && rent.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Room") },
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
                    onValueChange = { input -> floor = input.filter { it.isDigit() } },
                    label = { Text("Floor") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = beds,
                    onValueChange = { input -> beds = input.filter { it.isDigit() } },
                    label = { Text("Number of Beds *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = beds.isNotBlank() && (!isBedsNumberValid || !isBedsEnough),
                    supportingText = {
                        if (beds.isNotBlank() && !isBedsNumberValid) {
                            Text("Must be at least 1")
                        } else if (!isBedsEnough) {
                            Text("$occupiedBeds resident(s) live here. Beds cannot be below $occupiedBeds.")
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = rent,
                    onValueChange = { input -> rent = input.filter { it.isDigit() } },
                    label = { Text("Rent per Bed (₹) *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(
                        roomNumber.trim(),
                        floor.toIntOrNull() ?: 0,
                        bedsInt,
                        (rent.toLongOrNull() ?: 0L) * 100  // rupees → paise
                    )
                },
                enabled = isFormValid
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}