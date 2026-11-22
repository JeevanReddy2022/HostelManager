package com.example.hostelmanager.ui.residents

import android.content.Intent
import android.net.Uri
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
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.hostelmanager.data.ResidentWithRoom
import com.example.hostelmanager.ui.theme.AppBackground
import com.example.hostelmanager.ui.theme.AppBorder
import com.example.hostelmanager.ui.theme.AppCard
import com.example.hostelmanager.ui.theme.AppGreen
import com.example.hostelmanager.ui.theme.AppMuted
import com.example.hostelmanager.ui.theme.AppNavy
import com.example.hostelmanager.ui.theme.AppSoftBlue
import com.example.hostelmanager.ui.theme.AppText
import java.io.File
import java.net.URLEncoder
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun ResidentListScreen(
    hostelId: String,
    onAddClick: () -> Unit,
    onResidentClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ResidentListViewModel = viewModel() // ✅ Reverted to your original ViewModel
) {
    LaunchedEffect(hostelId) {
        viewModel.setHostelId(hostelId)
    }

    // ✅ This now correctly resolves to List<ResidentWithRoom>
    val residents by viewModel.residents.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackground)
    ) {
        if (residents.isEmpty()) {
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
                    "No active residents in this hostel.",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = AppText
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    "Tap the + button to admit a new resident.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppMuted
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 88.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(residents) { residentWithRoom -> // ✅ Now correctly iterates over ResidentWithRoom
                    ResidentCard(
                        residentWithRoom = residentWithRoom,
                        onClick = { onResidentClick(residentWithRoom.person.id) }
                    )
                }
            }
        }

        FloatingActionButton(
            onClick = onAddClick,
            shape = RoundedCornerShape(18.dp),
            containerColor = AppNavy,
            contentColor = androidx.compose.ui.graphics.Color.White,
            elevation = FloatingActionButtonDefaults.elevation(6.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
        ) {
            Icon(Icons.Default.PersonAdd, contentDescription = "Add Resident")
        }
    }
}

@Composable
fun ResidentCard(residentWithRoom: ResidentWithRoom, onClick: () -> Unit) {
    val person = residentWithRoom.person
    val context = LocalContext.current
    var showMessageDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
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
            // LEFT: Circular person photo
            ResidentAvatar(photoPath = person.profilePhotoPath)

            Spacer(modifier = Modifier.width(14.dp))

            // MIDDLE: Name, room, rent
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    person.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = AppText
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Premium Pills for Room and Rent
                Row {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(AppSoftBlue)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            "Room ${residentWithRoom.roomNumber}",
                            style = MaterialTheme.typography.bodySmall,
                            color = AppNavy,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(AppSoftBlue.copy(alpha = 0.6f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            "₹${residentWithRoom.monthlyRent / 100}",
                            style = MaterialTheme.typography.bodySmall,
                            color = AppNavy,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // RIGHT: Call + Message buttons
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(
                    onClick = {
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${person.phone}"))
                        context.startActivity(intent)
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = "Call",
                        tint = AppNavy
                    )
                }

                IconButton(
                    onClick = { showMessageDialog = true }
                ) {
                    Icon(
                        imageVector = Icons.Default.Message,
                        contentDescription = "Message",
                        tint = AppNavy
                    )
                }
            }
        }
    }

    // WhatsApp/SMS Choice Dialog (Fully retained)
    if (showMessageDialog) {
        val rentAmount = residentWithRoom.monthlyRent / 100

        MessageChoiceDialog(
            personName = person.name,
            phone = person.phone,
            rentAmount = rentAmount,
            onDismiss = { showMessageDialog = false },
            onWhatsAppClick = {
                val monthName = LocalDate.now().month.getDisplayName(TextStyle.FULL, Locale.getDefault())
                val message = "Hi ${person.name},\nPlease pay the rent ₹$rentAmount for $monthName.\nThanks!"
                val whatsappNumber = "91${person.phone}"
                val encodedMessage = URLEncoder.encode(message, "UTF-8")
                val whatsappUri = Uri.parse("https://wa.me/$whatsappNumber?text=$encodedMessage")
                val intent = Intent(Intent.ACTION_VIEW, whatsappUri)
                context.startActivity(intent)
                showMessageDialog = false
            },
            onSmsClick = {
                val monthName = LocalDate.now().month.getDisplayName(TextStyle.FULL, Locale.getDefault())
                val message = "Hi ${person.name},\nPlease pay the rent ₹$rentAmount for $monthName.\nThanks!"
                val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:${person.phone}"))
                intent.putExtra("sms_body", message)
                context.startActivity(intent)
                showMessageDialog = false
            }
        )
    }
}

@Composable
fun ResidentAvatar(photoPath: String?) {
    Box(
        modifier = Modifier
            .size(52.dp)
            .clip(CircleShape)
            .background(AppSoftBlue),
        contentAlignment = Alignment.Center
    ) {
        if (photoPath.isNullOrBlank()) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = AppNavy,
                modifier = Modifier.size(26.dp)
            )
        } else {
            AsyncImage(
                model = File(photoPath),
                contentDescription = "Resident photo",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
    }
}

@Composable
fun MessageChoiceDialog(
    personName: String,
    phone: String,
    rentAmount: Long,
    onDismiss: () -> Unit,
    onWhatsAppClick: () -> Unit,
    onSmsClick: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = AppCard,
        titleContentColor = AppText,
        textContentColor = AppMuted,
        title = {
            Text("Send Message", fontWeight = FontWeight.Bold)
        },
        text = {
            Column {
                Text("Choose how to send the rent reminder to $personName:")
                Spacer(modifier = Modifier.height(16.dp))
                TextButton(
                    onClick = onWhatsAppClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("💬 WhatsApp", color = AppGreen, fontWeight = FontWeight.Bold)
                }
                TextButton(
                    onClick = onSmsClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("📱 SMS", color = AppNavy, fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = AppMuted)
            }
        }
    )
}