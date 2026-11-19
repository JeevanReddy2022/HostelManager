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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import java.net.URLEncoder
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResidentDetailsScreen(
    personId: String,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit,
    onTransferClick: () -> Unit,
    onCheckoutClick: () -> Unit,
    viewModel: ResidentDetailsViewModel = viewModel()
) {
    LaunchedEffect(personId) {
        viewModel.setPersonId(personId)
    }

    val person by viewModel.person.collectAsState()
    val activeStay by viewModel.activeStay.collectAsState()
    val room by viewModel.room.collectAsState()
    val context = LocalContext.current

    var showEditConfirmDialog by remember { mutableStateOf(false) }
    var showMessageDialog by remember { mutableStateOf(false) }

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
                        }) {
                            Icon(Icons.Default.Call, contentDescription = "Call")
                        }

                        IconButton(onClick = { showMessageDialog = true }) {
                            Icon(Icons.Default.Message, contentDescription = "Message")
                        }
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

                val stay = activeStay
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Stay Details", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(8.dp))

                        if (stay == null) {
                            Text(
                                "No active stay — this resident has checked out.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            DetailRow("Room", room?.roomNumber ?: "—")
                            DetailRow("Monthly Rent", "₹${stay.monthlyRent / 100}")
                            DetailRow("Join Date", stay.joinDate)
                            DetailRow("Deposit", "₹${stay.depositCollected / 100}")

                            Spacer(Modifier.height(16.dp))

                            // TRANSFER ROOM — OutlinedButton (clear border, secondary action)
                            OutlinedButton(
                                onClick = onTransferClick,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Transfer Room")
                            }

                            Spacer(Modifier.height(8.dp))

                            // CHECK OUT — filled Button with error color (destructive, stands out)
                            Button(
                                onClick = onCheckoutClick,
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error,
                                    contentColor = MaterialTheme.colorScheme.onError
                                )
                            ) {
                                Text("Check Out Resident")
                            }
                        }
                    }
                }

                Text("Govt ID Documents", style = MaterialTheme.typography.titleMedium)
                DocumentImage("Front", currentPerson.govtIdPhotoPath)
                DocumentImage("Back", currentPerson.govtIdBackPath)
            }
        }
    }

    if (showEditConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showEditConfirmDialog = false },
            title = { Text("Edit Resident?") },
            text = { Text("Are you sure you want to edit this resident's details?") },
            confirmButton = {
                TextButton(onClick = {
                    showEditConfirmDialog = false
                    onEditClick()
                }) { Text("Yes, Edit") }
            },
            dismissButton = {
                TextButton(onClick = { showEditConfirmDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showMessageDialog && person != null && activeStay != null) {
        val rentAmount = activeStay!!.monthlyRent / 100

        MessageChoiceDialog(
            personName = person!!.name,
            phone = person!!.phone,
            rentAmount = rentAmount,
            onDismiss = { showMessageDialog = false },
            onWhatsAppClick = {
                val monthName = LocalDate.now().month.getDisplayName(TextStyle.FULL, Locale.getDefault())
                val message = "Hi ${person!!.name},\nPlease pay the rent ₹$rentAmount for $monthName.\nThanks!"
                val whatsappNumber = "91${person!!.phone}"
                val encodedMessage = URLEncoder.encode(message, "UTF-8")
                val whatsappUri = Uri.parse("https://wa.me/$whatsappNumber?text=$encodedMessage")
                val intent = Intent(Intent.ACTION_VIEW, whatsappUri)
                context.startActivity(intent)
                showMessageDialog = false
            },
            onSmsClick = {
                val monthName = LocalDate.now().month.getDisplayName(TextStyle.FULL, Locale.getDefault())
                val message = "Hi ${person!!.name},\nPlease pay the rent ₹$rentAmount for $monthName.\nThanks!"
                val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:${person!!.phone}"))
                intent.putExtra("sms_body", message)
                context.startActivity(intent)
                showMessageDialog = false
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