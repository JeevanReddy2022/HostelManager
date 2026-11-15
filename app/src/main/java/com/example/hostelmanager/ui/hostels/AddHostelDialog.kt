package com.example.hostelmanager.ui.hostels

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

@Composable
fun AddHostelDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, address: String, phone: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    // Validation: Phone must be exactly 10 digits
    val isPhoneValid = phone.matches(Regex("^\\d{10}$"))
    val isFormValid = name.isNotBlank() && isPhoneValid

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Hostel") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Address") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = phone,
                    onValueChange = { input -> phone = input.filter { it.isDigit() } }, // Only numbers
                    label = { Text("Phone (10 digits) *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = phone.isNotBlank() && !isPhoneValid,
                    supportingText = { if (phone.isNotBlank() && !isPhoneValid) Text("Must be exactly 10 digits") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(name, address, phone) },
                enabled = isFormValid // Button stays grey until valid
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