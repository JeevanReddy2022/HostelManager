package com.example.hostelmanager.ui.payments

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.hostelmanager.ui.residents.MessageChoiceDialog
import java.io.File
import java.net.URLEncoder
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

enum class SortOption { NAME, ROOM, AMOUNT }

@Composable
fun PaymentsListScreen(
    hostelId: String,
    onRecordPaymentClick: (String) -> Unit, // CHANGED: now takes personId
    onPaymentHistoryClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PaymentsViewModel = viewModel()
) {
    LaunchedEffect(hostelId) {
        viewModel.setHostelId(hostelId)
    }

    val residents by viewModel.residents.collectAsState()
    var sortOption by remember { mutableStateOf(SortOption.NAME) }

    val sortedResidents = when (sortOption) {
        SortOption.NAME -> residents.sortedBy { it.name }
        SortOption.ROOM -> residents.sortedBy { it.roomNumber }
        SortOption.AMOUNT -> residents.sortedByDescending { it.monthlyRent }
    }

    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            androidx.compose.material3.OutlinedButton(
                onClick = { onRecordPaymentClick("none") }, // "none" = manual search mode
                modifier = Modifier.weight(1f)
            ) {
                Text("Record Payment")
            }
            androidx.compose.material3.Button(
                onClick = onPaymentHistoryClick,
                modifier = Modifier.weight(1f)
            ) {
                Text("Payment History")
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SortChip("By Name", sortOption == SortOption.NAME) { sortOption = SortOption.NAME }
            SortChip("By Room", sortOption == SortOption.ROOM) { sortOption = SortOption.ROOM }
            SortChip("By Amount", sortOption == SortOption.AMOUNT) { sortOption = SortOption.AMOUNT }
        }

        if (sortedResidents.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text("All residents have paid!", style = MaterialTheme.typography.titleMedium)
                Text("No one is in the 'Yet to Pay' list.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(sortedResidents) { resident ->
                    YetToPayCard(resident, onRecordPaymentClick)
                }
            }
        }
    }
}

@Composable
fun SortChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (selected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surfaceVariant
            )
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            color = if (selected) MaterialTheme.colorScheme.onPrimary
            else MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun YetToPayCard(resident: YetToPayResident, onRecordPaymentClick: (String) -> Unit) {
    val context = LocalContext.current
    var showMessageDialog by remember { mutableStateOf(false) }

    // Card is now clickable, passing the personId
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onRecordPaymentClick(resident.personId) }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Photo
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (resident.profilePhotoPath.isNullOrBlank()) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    AsyncImage(
                        model = File(resident.profilePhotoPath),
                        contentDescription = "Resident photo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Details
            Column(modifier = Modifier.weight(1f)) {
                Text(resident.name, style = MaterialTheme.typography.titleMedium)
                Row {
                    Text("Room ${resident.roomNumber}", style = MaterialTheme.typography.bodySmall)
                    if (resident.isCheckedOut) {
                        Text(" · ", style = MaterialTheme.typography.bodySmall)
                        Text(
                            "Checked out",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }
                Text("₹${resident.monthlyRent / 100}", style = MaterialTheme.typography.titleLarge)
                Text(
                    "Due ${formatCycle(resident.cycleStart, resident.cycleEnd)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Call + Message (These remain separate and won't trigger the card click)
            Column {
                IconButton(onClick = {
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${resident.phone}"))
                    context.startActivity(intent)
                }) {
                    Icon(Icons.Default.Call, contentDescription = "Call", tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = { showMessageDialog = true }) {
                    Icon(Icons.Default.Message, contentDescription = "Message", tint = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }

    if (showMessageDialog) {
        val rentAmount = resident.monthlyRent / 100

        MessageChoiceDialog(
            personName = resident.name,
            phone = resident.phone,
            rentAmount = rentAmount,
            onDismiss = { showMessageDialog = false },
            onWhatsAppClick = {
                val message = "Hi ${resident.name},\nPlease pay the rent ₹$rentAmount for ${formatCycle(resident.cycleStart, resident.cycleEnd)}.\nThanks!"
                val whatsappNumber = "91${resident.phone}"
                val encodedMessage = URLEncoder.encode(message, "UTF-8")
                val whatsappUri = Uri.parse("https://wa.me/$whatsappNumber?text=$encodedMessage")
                val intent = Intent(Intent.ACTION_VIEW, whatsappUri)
                context.startActivity(intent)
                showMessageDialog = false
            },
            onSmsClick = {
                val message = "Hi ${resident.name},\nPlease pay the rent ₹$rentAmount for ${formatCycle(resident.cycleStart, resident.cycleEnd)}.\nThanks!"
                val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:${resident.phone}"))
                intent.putExtra("sms_body", message)
                context.startActivity(intent)
                showMessageDialog = false
            }
        )
    }
}

private fun formatCycle(start: String, end: String): String {
    val startDate = LocalDate.parse(start, DateTimeFormatter.ISO_LOCAL_DATE)
    val endDate = LocalDate.parse(end, DateTimeFormatter.ISO_LOCAL_DATE)
    return "${startDate.dayOfMonth} ${startDate.month.getDisplayName(TextStyle.SHORT, Locale.getDefault())} – ${endDate.dayOfMonth} ${endDate.month.getDisplayName(TextStyle.SHORT, Locale.getDefault())}"
}