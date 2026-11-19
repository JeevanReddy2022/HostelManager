package com.example.hostelmanager.ui.rooms

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hostelmanager.data.RoomWithOccupancy
import kotlinx.coroutines.launch

@Composable
fun RoomListScreen(
    hostelId: String,
    onRoomClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RoomViewModel = viewModel()
) {
    LaunchedEffect(hostelId) {
        viewModel.setHostelId(hostelId)
    }

    val roomsWithOccupancy by viewModel.roomsWithOccupancy.collectAsState()
    val roomError by viewModel.roomExistsError.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Box(modifier = modifier.fillMaxSize()) {
        if (roomsWithOccupancy.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
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
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 88.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(roomsWithOccupancy) { roomWithOccupancy ->
                    RoomCard(roomWithOccupancy, onClick = { onRoomClick(roomWithOccupancy.room.id) })
                }
            }
        }

        FloatingActionButton(
            onClick = {
                viewModel.clearRoomError()
                showAddDialog = true
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Room")
        }
    }

    if (showAddDialog) {
        AddRoomDialog(
            onDismiss = {
                viewModel.clearRoomError()
                showAddDialog = false
            },
            errorMessage = roomError,
            onErrorCleared = { viewModel.clearRoomError() },
            onConfirm = { number, floor, beds, rent ->
                scope.launch {
                    val success = viewModel.tryAddRoom(number, floor, beds, rent)
                    if (success) showAddDialog = false
                    // If not successful, the dialog stays open with the error shown
                }
            }
        )
    }
}

@Composable
fun RoomCard(roomWithOccupancy: RoomWithOccupancy, onClick: () -> Unit) {
    val room = roomWithOccupancy.room
    val vacancy = room.bedCount - roomWithOccupancy.occupiedBeds

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Room ${room.roomNumber}", style = MaterialTheme.typography.titleLarge)
            Text("Floor: ${room.floor}")
            Text("Beds: ${room.bedCount} | Vacancy: $vacancy")
            val rentInRupees = room.defaultRentPerBed / 100
            Text("Rent per bed: ₹$rentInRupees")
        }
    }
}