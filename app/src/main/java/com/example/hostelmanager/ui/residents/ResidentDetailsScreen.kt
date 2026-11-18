package com.example.hostelmanager.ui.residents

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Message
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResidentDetailsScreen(
    personId: String,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit,
    viewModel: ResidentDetailsViewModel = viewModel()
) {
    LaunchedEffect(personId) {
        viewModel.setPersonId(personId)
    }

    val person by viewModel.person.collectAsState()
    val activeStay by viewModel.activeStay.collectAsState()
    val room by viewModel.room.collectAsState()
    val context = LocalContext.current

    // NEW: Controls the "Are you sure?" popup
    var showEditConfirmDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(person?.name ?: "Resident") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // NEW: Opens the confirmation popup instead of navigating immediately
                    IconButton(onClick = { showEditConfirmDialog = true }) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    person?.let { p ->
                        IconButton(onClick = {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${p.phone}"))
                            context.startActivity(intent)
                        }) { Icon(Icons.Default.Call, contentDescription = "Call") }

                        IconButton(onClick = {
                            val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:${p.phone}"))
                            context.startActivity(intent)
                        }) { Icon(Icons.Default.Message, contentDescription = "Message") }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val currentPerson = person

            if (currentPerson == null) {
                Text("Loading…", style = MaterialTheme.typography.bodyMedium)
            } else {
                // Person photo (if captured)
                if (!currentPerson.profilePhotoPath.isNullOrBlank()) {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        AsyncImage(
                            model = File(currentPerson.profilePhotoPath),
                            contentDescription = "Person photo",
                            modifier = Modifier.fillMaxWidth().height(200.dp),
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                Text(currentPerson.name, style = MaterialTheme.typography.headlineSmall)

                // Personal details
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Personal Details", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(8.dp))
                        DetailRow("Phone", currentPerson.phone)
                        DetailRow("Govt ID", currentPerson.govtIdNumber)
                        DetailRow("Emergency", currentPerson.emergencyContact)
                        DetailRow("Date of Birth", currentPerson.dateOfBirth.ifBlank { "—" })
                        DetailRow("Address", currentPerson.address.ifBlank { "—" })
                        DetailRow("Gender", currentPerson.gender)
                        DetailRow("Guardian", currentPerson.guardianName.ifBlank { "—" })
                    }
                }

                // Stay details
                val stay = activeStay
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Stay Details", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(8.dp))
                        DetailRow("Room", room?.roomNumber ?: "—")
                        DetailRow("Monthly Rent", if (stay != null) "₹${stay.monthlyRent / 100}" else "—")
                        DetailRow("Join Date", stay?.joinDate ?: "—")
                        DetailRow("Deposit", if (stay != null) "₹${stay.depositCollected / 100}" else "—")
                    }
                }

                // Govt ID documents
                Text("Govt ID Documents", style = MaterialTheme.typography.titleMedium)
                DocumentImage("Front", currentPerson.govtIdPhotoPath)
                DocumentImage("Back", currentPerson.govtIdBackPath)
            }
        }
    }

    // NEW: The Edit Confirmation Popup
    if (showEditConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showEditConfirmDialog = false },
            title = { Text("Edit Resident?") },
            text = { Text("Are you sure you want to edit this resident's details?") },
            confirmButton = {
                TextButton(onClick = {
                    showEditConfirmDialog = false
                    onEditClick() // Only navigate if they confirm
                }) {
                    Text("Yes, Edit")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(0.4f)
        )
        Text(value, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(0.6f))
    }
}

@Composable
private fun DocumentImage(label: String, path: String?) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                "$label side",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            if (path.isNullOrBlank()) {
                Text("Not provided", style = MaterialTheme.typography.bodySmall)
            } else {
                AsyncImage(
                    model = File(path),
                    contentDescription = "Govt ID $label",
                    modifier = Modifier.fillMaxWidth().height(180.dp),
                    contentScale = ContentScale.Fit
                )
            }
        }
    }
}