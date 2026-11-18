package com.example.hostelmanager.ui.rooms

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hostelmanager.data.ResidentWithRoom

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

    // Once the room is archived or deleted, go back to the Rooms list
    LaunchedEffect(roomRemoved) {
        if (roomRemoved) onBackClick()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(room?.roomNumber ?: "Room Details") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
        ) {
            if (room != null) {
                val vacancy = room!!.bedCount - residents.size
                Card(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Room ${room!!.roomNumber}", style = MaterialTheme.typography.titleLarge)
                            Text("Floor: ${room!!.floor}")
                            Text("Beds: ${room!!.bedCount} | Vacancy: $vacancy")
                            Text("Rent: ₹${room!!.defaultRentPerBed / 100}")
                        }

                        Row {
                            IconButton(onClick = { showEditDialog = true }) {
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = "Edit Room",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            IconButton(onClick = {
                                // Decide: refuse, archive, or hard-delete
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
                                    contentDescription = "Remove Room",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }

            Text("Residents in this room:", style = MaterialTheme.typography.titleMedium)

            if (residents.isEmpty()) {
                Text("No residents currently in this room.", modifier = Modifier.padding(top = 8.dp))
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
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

    // Edit Room popup
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

    // Rent revision popup
    val prompt = rentPrompt
    if (prompt != null) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissRentPrompt() },
            title = { Text("Update residents' rent?") },
            text = {
                Text(
                    "Room rent changed from ₹${prompt.oldRentPaise / 100} to ₹${prompt.newRentPaise / 100}.\n\n" +
                            "Update the monthly rent for all ${prompt.residentCount} resident(s) in this room?\n\n" +
                            "Anyone on a custom agreed rent will also be changed."
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.applyRentToAllResidents() }) {
                    Text("Yes, update all")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissRentPrompt() }) {
                    Text("No, keep existing")
                }
            }
        )
    }

    // 1) REFUSE: residents still live here
    if (showOccupiedDialog && currentRoom != null) {
        AlertDialog(
            onDismissRequest = { showOccupiedDialog = false },
            title = { Text("Can't remove this room") },
            text = {
                Text(
                    "${residents.size} resident(s) still live in Room ${currentRoom.roomNumber}.\n\n" +
                            "Move them to another room or check them out first."
                )
            },
            confirmButton = {
                TextButton(onClick = { showOccupiedDialog = false }) {
                    Text("OK")
                }
            }
        )
    }

    // 2) ARCHIVE: empty now, but used before -> keep history
    if (showArchiveDialog && currentRoom != null) {
        AlertDialog(
            onDismissRequest = { showArchiveDialog = false },
            title = { Text("Hide this room?") },
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
                }) { Text("Hide") }
            },
            dismissButton = {
                TextButton(onClick = { showArchiveDialog = false }) { Text("Cancel") }
            }
        )
    }

    // 3) HARD DELETE: never used -> gone for good
    if (showDeleteDialog && currentRoom != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete permanently?") },
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
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun ResidentDetailCard(residentWithRoom: ResidentWithRoom, onClick: () -> Unit) {
    val person = residentWithRoom.person
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(person.name, style = MaterialTheme.typography.titleLarge)
                Text("📞 ${person.phone}", style = MaterialTheme.typography.bodyMedium)
                Text("💰 Rent: ₹${residentWithRoom.monthlyRent / 100}", style = MaterialTheme.typography.bodyMedium)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(onClick = {
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${person.phone}"))
                    context.startActivity(intent)
                }) {
                    Icon(Icons.Default.Call, contentDescription = "Call", tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = {
                    val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:${person.phone}"))
                    context.startActivity(intent)
                }) {
                    Icon(Icons.Default.Message, contentDescription = "Message", tint = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}