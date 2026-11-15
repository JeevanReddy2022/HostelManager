package com.example.hostelmanager.ui.rooms

import android.content.Intent
import android.net.Uri
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
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
    viewModel: RoomDetailsViewModel = viewModel()
) {
    LaunchedEffect(roomId) {
        viewModel.setRoomId(roomId)
    }

    val room by viewModel.room.collectAsState()
    val residents by viewModel.residents.collectAsState()

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
            // Room Info Header with Edit & Delete buttons
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
                        // Left side: Room details
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Room ${room!!.roomNumber}", style = MaterialTheme.typography.titleLarge)
                            Text("Floor: ${room!!.floor}")
                            Text("Beds: ${room!!.bedCount} | Vacancy: $vacancy")
                            Text("Rent: ₹${room!!.defaultRentPerBed / 100}")
                        }

                        // Right side: Edit & Delete icons (UI only for now)
                        Row {
                            IconButton(onClick = {
                                // TODO: Implement Edit logic later
                            }) {
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = "Edit Room",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            IconButton(onClick = {
                                // TODO: Implement Delete logic later
                            }) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Delete Room",
                                    tint = MaterialTheme.colorScheme.error // Makes the delete icon red
                                )
                            }
                        }
                    }
                }
            }

            // Residents List
            Text("Residents in this room:", style = MaterialTheme.typography.titleMedium)

            if (residents.isEmpty()) {
                Text("No residents currently in this room.", modifier = Modifier.padding(top = 8.dp))
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(residents) { residentWithRoom ->
                        ResidentDetailCard(residentWithRoom)
                    }
                }
            }
        }
    }
}

@Composable
fun ResidentDetailCard(residentWithRoom: ResidentWithRoom) {
    val person = residentWithRoom.person
    val context = LocalContext.current

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(person.name, style = MaterialTheme.typography.titleLarge)
                Text("📞 ${person.phone}", style = MaterialTheme.typography.bodyMedium)
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