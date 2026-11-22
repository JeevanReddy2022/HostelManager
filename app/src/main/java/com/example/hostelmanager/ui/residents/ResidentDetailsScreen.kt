package com.example.hostelmanager.ui.residents

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Message
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.hostelmanager.data.Payment
import com.example.hostelmanager.ui.theme.AppBackground
import com.example.hostelmanager.ui.theme.AppBorder
import com.example.hostelmanager.ui.theme.AppCard
import com.example.hostelmanager.ui.theme.AppGreen
import com.example.hostelmanager.ui.theme.AppMuted
import com.example.hostelmanager.ui.theme.AppNavy
import com.example.hostelmanager.ui.theme.AppRed
import com.example.hostelmanager.ui.theme.AppSoftBlue
import com.example.hostelmanager.ui.theme.AppSoftGreen
import com.example.hostelmanager.ui.theme.AppSoftRed
import com.example.hostelmanager.ui.theme.AppText
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
    val payments by viewModel.payments.collectAsState()
    val context = LocalContext.current

    var showEditConfirmDialog by remember { mutableStateOf(false) }
    var showMessageDialog by remember { mutableStateOf(false) }
    var paymentToDelete by remember { mutableStateOf<Payment?>(null) }

    Scaffold(
        containerColor = AppBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        person?.name ?: "Resident",
                        fontWeight = FontWeight.Bold,
                        color = AppText,
                        maxLines = 1
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = AppText)
                    }
                },
                actions = {
                    IconButton(onClick = { showEditConfirmDialog = true }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = AppNavy)
                    }
                    person?.let { p ->
                        IconButton(onClick = {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${p.phone}"))
                            try {
                                context.startActivity(intent)
                            } catch (e: ActivityNotFoundException) {
                                Toast.makeText(context, "No dialer app found", Toast.LENGTH_SHORT).show()
                            }
                        }) {
                            Icon(Icons.Default.Call, contentDescription = "Call", tint = AppNavy)
                        }
                        IconButton(onClick = { showMessageDialog = true }) {
                            Icon(Icons.Default.Message, contentDescription = "Message", tint = AppNavy)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppBackground,
                    titleContentColor = AppText,
                    navigationIconContentColor = AppText,
                    actionIconContentColor = AppNavy
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val currentPerson = person

            if (currentPerson == null) {
                Text("Loading…", style = MaterialTheme.typography.bodyMedium, color = AppMuted)
            } else {
                // Profile photo
                if (!currentPerson.profilePhotoPath.isNullOrBlank()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = AppCard),
                        border = BorderStroke(1.dp, AppBorder),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        AsyncImage(
                            model = File(currentPerson.profilePhotoPath),
                            contentDescription = "Person photo",
                            modifier = Modifier.fillMaxWidth().height(220.dp),
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                // Name header
                Text(
                    currentPerson.name,
                    style = MaterialTheme.typography.headlineSmall,
                    color = AppText,
                    fontWeight = FontWeight.Bold
                )

                // Personal Details Card
                PremiumCard {
                    SectionHeader("Personal Details")
                    Spacer(modifier = Modifier.height(10.dp))
                    DetailRow("Phone", currentPerson.phone)
                    DetailRow("Govt ID", currentPerson.govtIdNumber)
                    DetailRow("Emergency", currentPerson.emergencyContact)
                    DetailRow("Date of Birth", currentPerson.dateOfBirth.ifBlank { "—" })
                    DetailRow("Address", currentPerson.address.ifBlank { "—" })
                    DetailRow("Gender", currentPerson.gender)
                    DetailRow("Guardian", currentPerson.guardianName.ifBlank { "—" })
                }

                // Stay Details Card
                val stay = activeStay
                PremiumCard {
                    SectionHeader("Stay Details")
                    Spacer(modifier = Modifier.height(10.dp))

                    if (stay == null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(AppSoftRed)
                                .padding(14.dp)
                        ) {
                            Text(
                                "No active stay — this resident has checked out.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = AppRed,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    } else {
                        DetailRow("Room", room?.roomNumber ?: "—")
                        DetailRow("Monthly Rent", "₹${stay.monthlyRent / 100}")
                        DetailRow("Join Date", stay.joinDate)
                        DetailRow("Deposit", "₹${stay.depositCollected / 100}")

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedButton(
                            onClick = onTransferClick,
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.5.dp, AppNavy),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = Color.Transparent,
                                contentColor = AppNavy
                            )
                        ) {
                            Text("Transfer Room", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = onCheckoutClick,
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AppRed,
                                contentColor = Color.White
                            )
                        ) {
                            Text("Check Out Resident", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }

                // Govt ID Documents
                SectionHeader("Govt ID Documents")
                DocumentImage("Front", currentPerson.govtIdPhotoPath)
                DocumentImage("Back", currentPerson.govtIdBackPath)

                // Payment History
                SectionHeader("Payment History")
                if (payments.isEmpty()) {
                    PremiumCard {
                        Text("No payments recorded yet.", style = MaterialTheme.typography.bodyMedium, color = AppMuted)
                    }
                } else {
                    payments.forEach { payment ->
                        PaymentHistoryCard(
                            payment = payment,
                            onDeleteClick = { paymentToDelete = payment }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Edit Confirmation Dialog
    if (showEditConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showEditConfirmDialog = false },
            shape = RoundedCornerShape(24.dp),
            containerColor = AppCard,
            titleContentColor = AppText,
            textContentColor = AppMuted,
            title = { Text("Edit Resident?", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to edit this resident's details?") },
            confirmButton = {
                TextButton(onClick = {
                    showEditConfirmDialog = false
                    onEditClick()
                }) { Text("Yes, Edit", color = AppNavy, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showEditConfirmDialog = false }) { Text("Cancel", color = AppMuted) }
            }
        )
    }

    // Message Choice Dialog (MessageChoiceDialog lives in ResidentListScreen.kt, same package)
    val dialogPerson = person
    val dialogStay = activeStay
    if (showMessageDialog && dialogPerson != null && dialogStay != null) {
        val rentAmount = dialogStay.monthlyRent / 100
        MessageChoiceDialog(
            personName = dialogPerson.name,
            phone = dialogPerson.phone,
            rentAmount = rentAmount,
            onDismiss = { showMessageDialog = false },
            onWhatsAppClick = {
                val monthName = LocalDate.now().month.getDisplayName(TextStyle.FULL, Locale.getDefault())
                val message = "Hi ${dialogPerson.name},\nPlease pay the rent ₹$rentAmount for $monthName.\nThanks!"
                val whatsappNumber = "91${dialogPerson.phone}"
                val encodedMessage = URLEncoder.encode(message, "UTF-8")
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$whatsappNumber?text=$encodedMessage"))
                try {
                    context.startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    Toast.makeText(context, "WhatsApp not installed", Toast.LENGTH_SHORT).show()
                }
                showMessageDialog = false
            },
            onSmsClick = {
                val monthName = LocalDate.now().month.getDisplayName(TextStyle.FULL, Locale.getDefault())
                val message = "Hi ${dialogPerson.name},\nPlease pay the rent ₹$rentAmount for $monthName.\nThanks!"
                val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:${dialogPerson.phone}"))
                intent.putExtra("sms_body", message)
                try {
                    context.startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    Toast.makeText(context, "No SMS app found", Toast.LENGTH_SHORT).show()
                }
                showMessageDialog = false
            }
        )
    }

    // Delete Payment Confirmation Dialog
    paymentToDelete?.let { payment ->
        AlertDialog(
            onDismissRequest = { paymentToDelete = null },
            shape = RoundedCornerShape(24.dp),
            containerColor = AppCard,
            titleContentColor = AppText,
            textContentColor = AppMuted,
            title = { Text("Delete Payment?", fontWeight = FontWeight.Bold) },
            text = {
                Text("Delete payment of ₹${payment.amountPaise / 100} from ${payment.paymentDate}? This can't be undone.")
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deletePayment(payment.id)
                    paymentToDelete = null
                }) { Text("Delete", color = AppRed, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { paymentToDelete = null }) { Text("Cancel", color = AppMuted) }
            }
        )
    }
}

@Composable
private fun PremiumCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = AppCard),
        border = BorderStroke(1.dp, AppBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            content()
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Column(modifier = Modifier.padding(top = 4.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium, color = AppText, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .width(32.dp)
                .height(2.dp)
                .background(AppNavy)
        )
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
        Text(value, style = MaterialTheme.typography.bodyMedium, color = AppText, fontWeight = FontWeight.Medium, modifier = Modifier.weight(0.6f))
    }
}

@Composable
private fun DocumentImage(label: String, path: String?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = AppCard),
        border = BorderStroke(1.dp, AppBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text("$label side", style = MaterialTheme.typography.bodyMedium, color = AppMuted, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(10.dp))
            if (path.isNullOrBlank()) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(160.dp).clip(RoundedCornerShape(12.dp)).background(AppSoftBlue),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Not provided", style = MaterialTheme.typography.bodySmall, color = AppNavy)
                }
            } else {
                AsyncImage(
                    model = File(path),
                    contentDescription = "Govt ID $label",
                    modifier = Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Fit
                )
            }
        }
    }
}

@Composable
private fun PaymentHistoryCard(payment: Payment, onDeleteClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = AppCard),
        border = BorderStroke(1.dp, AppBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier.clip(RoundedCornerShape(50)).background(AppSoftGreen).padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text("Received: ${payment.paymentDate}", style = MaterialTheme.typography.bodySmall, color = AppGreen, fontWeight = FontWeight.SemiBold)
                }
                if (payment.date != payment.paymentDate) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Recorded: ${payment.date}", style = MaterialTheme.typography.bodySmall, color = AppMuted)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(payment.mode, style = MaterialTheme.typography.bodySmall, color = AppNavy, fontWeight = FontWeight.Medium)
                if (payment.note.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(payment.note, style = MaterialTheme.typography.bodySmall, color = AppMuted)
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text("₹${payment.amountPaise / 100}", style = MaterialTheme.typography.titleLarge, color = AppNavy, fontWeight = FontWeight.Bold)
                IconButton(onClick = onDeleteClick) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = AppRed, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}