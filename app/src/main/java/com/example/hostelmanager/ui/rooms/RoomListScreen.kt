package com.example.hostelmanager.ui.rooms

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hostelmanager.data.RoomWithOccupancy

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomListScreen(
    hostelId: String,
    onBackClick: () -> Unit,
    onAddResidentClick: (String) -> Unit,
    viewModel: RoomViewModel = viewModel()
) {
    LaunchedEffect(hostelId) {
        viewModel.setHostelId(hostelId)
    }

    val roomsWithOccupancy by viewModel.roomsWithOccupancy.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Rooms") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End) {
                // Single button now: "+" to add a room
                FloatingActionButton(onClick = { showAddDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Room")
                }
                Spacer(modifier = Modifier.height(16.dp))
                // Bottom button: Add Room (Standard + icon)
                FloatingActionButton(onClick = { showAddDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Room")
                }
            }
        }
    ) { padding ->
        if (roomsWithOccupancy.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text("No rooms in this hostel yet.", style = MaterialTheme.typography.titleMedium)
                Text("Tap the + button to add a room.")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(roomsWithOccupancy) { roomWithOccupancy ->
                    RoomCard(roomWithOccupancy)
                }
            }
        }
    }

    if (showAddDialog) {
        AddRoomDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { number, floor, beds, rent ->
                viewModel.addRoom(number, floor, beds, rent)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun RoomCard(roomWithOccupancy: RoomWithOccupancy) {
    val room = roomWithOccupancy.room
    val vacancy = room.bedCount - roomWithOccupancy.occupiedBeds

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Room ${room.roomNumber}", style = MaterialTheme.typography.titleLarge)
            Text("Floor: ${room.floor}")
            Text("Beds: ${room.bedCount} | Vacancy: $vacancy")
            val rentInRupees = room.defaultRentPerBed / 100
            Text("Rent per bed: ₹$rentInRupees")
        }
    }
}