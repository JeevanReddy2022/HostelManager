package com.example.hostelmanager.ui.rooms

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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bed
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Message
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hostelmanager.data.ResidentWithRoom
import com.example.hostelmanager.ui.residents.ResidentAvatar
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
import java.net.URLEncoder
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomDetailsScreen(
    roomId: String,
    onBackClick: () -> Unit,
    onResidentClick: (String) -> Unit,
    viewModel: RoomDetailsViewModel = viewModel()
) {
    LaunchedEffect(roomId) {
        viewModel.setRoomId(roomId)
    }

    val room by viewModel.room.collectAsState()
    val residents by viewModel.residents.collectAsState()
    val totalStays by viewModel.totalStays.collectAsState()
    val rentPrompt by viewModel.rentPrompt.collectAsState()
    val roomRemoved by viewModel.roomRemoved.collectAsState()

    var showEditDialog by remember { mutableStateOf(false) }
    var showOccupiedDialog by remember { mutableStateOf(false) }
    var showArchiveDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(roomRemoved) {
        if (roomRemoved) onBackClick()
    }

    Scaffold(
        containerColor = AppBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        room?.roomNumber ?: "Room Details",
                        fontWeight = FontWeight.Bold,
                        color = AppText
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = AppText)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppBackground,
                    titleContentColor = AppText,
                    navigationIconContentColor = AppText
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (room != null) {
                val vacancy = room!!.bedCount - residents.size
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = AppCard),
                    border = BorderStroke(1.dp, AppBorder),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Room ${room!!.roomNumber}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = AppText
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Floor ${room!!.floor}", style = MaterialTheme.typography.bodyMedium, color = AppMuted)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .background(AppSoftBlue)
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        "${room!!.bedCount} Beds",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = AppNavy,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .background(if (vacancy <= 0) AppSoftRed else AppSoftGreen)
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        if (vacancy <= 0) "Full" else "$vacancy Available",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (vacancy <= 0) AppRed else AppGreen,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }

                        Row {
                            IconButton(onClick = { showEditDialog = true }) {
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = "Edit Room",
                                    tint = AppNavy
                                )
                            }
                            IconButton(onClick = {
                                if (residents.isNotEmpty()) {
                                    showOccupiedDialog = true
                                } else if (totalStays > 0) {
                                    showArchiveDialog = true
                                } else {
                                    showDeleteDialog = true
                                }
                            }) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Delete Room",
                                    tint = AppRed
                                )
                            }
                        }
                    }
                }
            }

            Text(
                "Residents in this room:",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = AppText,
                modifier = Modifier.padding(top = 8.dp)
            )

            if (residents.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = AppCard),
                    border = BorderStroke(1.dp, AppBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(AppSoftBlue),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Bed,
                                contentDescription = null,
                                tint = AppNavy,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "No residents currently in this room.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = AppMuted
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(residents) { residentWithRoom ->
                        ResidentDetailCard(
                            residentWithRoom,
                            onClick = { onResidentClick(residentWithRoom.person.id) }
                        )
                    }
                }
            }
        }
    }

    val currentRoom = room

    if (showEditDialog && currentRoom != null) {
        EditRoomDialog(
            room = currentRoom,
            occupiedBeds = residents.size,
            onDismiss = { showEditDialog = false },
            onConfirm = { number, floor, beds, rentPaise ->
                viewModel.updateRoom(number, floor, beds, rentPaise)
                showEditDialog = false
            }
        )
    }

    val prompt = rentPrompt
    if (prompt != null) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissRentPrompt() },
            shape = RoundedCornerShape(24.dp),
            containerColor = AppCard,
            titleContentColor = AppText,
            textContentColor = AppMuted,
            title = { Text("Update residents' rent?", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Room rent changed from ₹${prompt.oldRentPaise / 100} to ₹${prompt.newRentPaise / 100}.\n\n" +
                            "Update the monthly rent for all ${prompt.residentCount} resident(s) in this room?\n\n" +
                            "Anyone on a custom agreed rent will also be changed."
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.applyRentToAllResidents() }) {
                    Text("Yes, update all", color = AppGreen, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissRentPrompt() }) {
                    Text("No, keep existing", color = AppMuted)
                }
            }
        )
    }

    if (showOccupiedDialog && currentRoom != null) {
        AlertDialog(
            onDismissRequest = { showOccupiedDialog = false },
            shape = RoundedCornerShape(24.dp),
            containerColor = AppCard,
            titleContentColor = AppText,
            textContentColor = AppMuted,
            title = { Text("Can't remove this room", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "${residents.size} resident(s) still live in Room ${currentRoom.roomNumber}.\n\n" +
                            "Move them to another room or check them out first."
                )
            },
            confirmButton = {
                TextButton(onClick = { showOccupiedDialog = false }) {
                    Text("OK", color = AppNavy, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    if (showArchiveDialog && currentRoom != null) {
        AlertDialog(
            onDismissRequest = { showArchiveDialog = false },
            shape = RoundedCornerShape(24.dp),
            containerColor = AppCard,
            titleContentColor = AppText,
            textContentColor = AppMuted,
            title = { Text("Hide this room?", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Room ${currentRoom.roomNumber} has been used before, so we'll hide it instead of deleting it, to keep past records.\n\n" +
                            "It won't be available for new residents."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showArchiveDialog = false
                    viewModel.archiveRoom()
                }) { Text("Hide", color = AppNavy, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showArchiveDialog = false }) { Text("Cancel", color = AppMuted) }
            }
        )
    }

    if (showDeleteDialog && currentRoom != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            shape = RoundedCornerShape(24.dp),
            containerColor = AppCard,
            titleContentColor = AppText,
            textContentColor = AppMuted,
            title = { Text("Delete permanently?", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Room ${currentRoom.roomNumber} was never used, so there's nothing to keep.\n\n" +
                            "This can't be undone."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    viewModel.deleteRoomPermanently()
                }) { Text("Delete", color = AppRed, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel", color = AppMuted) }
            }
        )
    }
}

@Composable
fun ResidentDetailCard(residentWithRoom: ResidentWithRoom, onClick: () -> Unit) {
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
            ResidentAvatar(photoPath = person.profilePhotoPath)
            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(person.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = AppText)
                Text("📞 ${person.phone}", style = MaterialTheme.typography.bodyMedium, color = AppMuted)
                Text("💰 Rent: ₹${residentWithRoom.monthlyRent / 100}", style = MaterialTheme.typography.bodySmall, color = AppNavy, fontWeight = FontWeight.Medium)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(onClick = {
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${person.phone}"))
                    context.startActivity(intent)
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
        title = { Text("Send Message", fontWeight = FontWeight.Bold) },
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