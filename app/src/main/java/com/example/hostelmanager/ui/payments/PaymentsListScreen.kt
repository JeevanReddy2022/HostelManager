package com.example.hostelmanager.ui.payments

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.hostelmanager.ui.residents.MessageChoiceDialog
import com.example.hostelmanager.ui.theme.AppBackground
import com.example.hostelmanager.ui.theme.AppBorder
import com.example.hostelmanager.ui.theme.AppCard
import com.example.hostelmanager.ui.theme.AppMuted
import com.example.hostelmanager.ui.theme.AppNavy
import com.example.hostelmanager.ui.theme.AppRed
import com.example.hostelmanager.ui.theme.AppSoftBlue
import com.example.hostelmanager.ui.theme.AppSoftRed
import com.example.hostelmanager.ui.theme.AppText
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
    onRecordPaymentClick: (String) -> Unit,
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

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackground)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Action buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { onRecordPaymentClick("none") },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.5.dp, AppNavy)
                ) {
                    Text("Record Payment", color = AppNavy, fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = onPaymentHistoryClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF5B82F6), // Light Royal Blue
                        contentColor = Color.White
                    )
                ) {
                    Text("Payment History", fontWeight = FontWeight.Bold)
                }
            }

            // Premium Sort Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SortChip("By Name", sortOption == SortOption.NAME) { sortOption = SortOption.NAME }
                SortChip("By Room", sortOption == SortOption.ROOM) { sortOption = SortOption.ROOM }
                SortChip("By Amount", sortOption == SortOption.AMOUNT) { sortOption = SortOption.AMOUNT }
            }

            // List or Empty State
            if (sortedResidents.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(AppSoftBlue),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint = AppNavy,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "All residents have paid!",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = AppText
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "No one is in the 'Yet to Pay' list.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppMuted
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(sortedResidents) { resident ->
                        YetToPayCard(resident, onRecordPaymentClick)
                    }
                }
            }
        }
    }
}

@Composable
fun SortChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val backgroundColor = if (selected) AppNavy else AppSoftBlue
    val textColor = if (selected) Color.White else AppNavy
    val fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(backgroundColor)
            .clickable { onClick() }
            .padding(horizontal = 18.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = textColor,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = fontWeight
        )
    }
}

@Composable
fun YetToPayCard(resident: YetToPayResident, onRecordPaymentClick: (String) -> Unit) {
    val context = LocalContext.current
    var showMessageDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onRecordPaymentClick(resident.personId) },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = AppCard),
        border = BorderStroke(1.dp, AppBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(AppSoftBlue),
                contentAlignment = Alignment.Center
            ) {
                if (resident.profilePhotoPath.isNullOrBlank()) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = AppNavy,
                        modifier = Modifier.size(26.dp)
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

            Spacer(modifier = Modifier.width(14.dp))

            // Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    resident.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = AppText
                )
                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "Room ${resident.roomNumber}",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppMuted
                    )
                    if (resident.isCheckedOut) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(AppSoftRed)
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                "Checked out",
                                style = MaterialTheme.typography.bodySmall,
                                color = AppRed,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    "₹${resident.monthlyRent / 100}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = AppNavy
                )
                Text(
                    "Due: ${formatCycle(resident.cycleStart, resident.cycleEnd)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppMuted
                )
            }

            // Call + Message (These remain separate and won't trigger the card click)
            Column {
                IconButton(onClick = {
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${resident.phone}"))
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
                try {
                    context.startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    Toast.makeText(context, "WhatsApp not installed", Toast.LENGTH_SHORT).show()
                }
                showMessageDialog = false
            },
            onSmsClick = {
                val message = "Hi ${resident.name},\nPlease pay the rent ₹$rentAmount for ${formatCycle(resident.cycleStart, resident.cycleEnd)}.\nThanks!"
                val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:${resident.phone}"))
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
}

private fun formatCycle(start: String, end: String): String {
    val startDate = LocalDate.parse(start, DateTimeFormatter.ISO_LOCAL_DATE)
    val endDate = LocalDate.parse(end, DateTimeFormatter.ISO_LOCAL_DATE)
    return "${startDate.dayOfMonth} ${startDate.month.getDisplayName(TextStyle.SHORT, Locale.getDefault())} – ${endDate.dayOfMonth} ${endDate.month.getDisplayName(TextStyle.SHORT, Locale.getDefault())}"
}