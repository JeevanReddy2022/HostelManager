package com.example.hostelmanager.ui.rooms

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
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hostelmanager.data.RoomEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArchivedRoomsScreen(
    hostelId: String,
    onBackClick: () -> Unit,
    viewModel: ArchivedRoomsViewModel = viewModel()
) {
    LaunchedEffect(hostelId) { viewModel.setHostelId(hostelId) }

    val rooms by viewModel.archivedRooms.collectAsState()
    var roomToRestore by remember { mutableStateOf<RoomEntity?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Archived Rooms") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (rooms.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text("No archived rooms.", style = MaterialTheme.typography.titleMedium)
                Text("Rooms you hide will appear here so you can bring them back.")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(rooms) { room ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Room ${room.roomNumber}", style = MaterialTheme.typography.titleLarge)
                                Text("Floor: ${room.floor}")
                                Text("Beds: ${room.bedCount}")
                                Text("Rent: ₹${room.defaultRentPerBed / 100}")
                            }
                            OutlinedButton(onClick = { roomToRestore = room }) {
                                Text("Restore")
                            }
                        }
                    }
                }
            }
        }
    }

    val target = roomToRestore
    if (target != null) {
        AlertDialog(
            onDismissRequest = { roomToRestore = null },
            title = { Text("Restore this room?") },
            text = {
                Text(
                    "Room ${target.roomNumber} will reappear in the Rooms list and become " +
                            "available for new residents.\n\nIts past records are still intact."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.restoreRoom(target)
                    roomToRestore = null
                }) { Text("Yes, restore") }
            },
            dismissButton = {
                TextButton(onClick = { roomToRestore = null }) { Text("Cancel") }
            }
        )
    }
}