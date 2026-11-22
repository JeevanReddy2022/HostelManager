package com.example.hostelmanager.ui.rooms

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hostelmanager.data.RoomWithOccupancy
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

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackground)
    ) {
        if (roomsWithOccupancy.isEmpty()) {
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
                        Icons.Default.Bed,
                        contentDescription = null,
                        tint = AppNavy,
                        modifier = Modifier.size(40.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "No rooms in this hostel yet.",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = AppText
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    "Tap the + button to add a room.",
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
            shape = RoundedCornerShape(18.dp),
            containerColor = AppNavy,
            contentColor = androidx.compose.ui.graphics.Color.White,
            elevation = FloatingActionButtonDefaults.elevation(6.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
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
                }
            }
        )
    }
}

@Composable
fun RoomCard(roomWithOccupancy: RoomWithOccupancy, onClick: () -> Unit) {
    val room = roomWithOccupancy.room
    val vacancy = room.bedCount - roomWithOccupancy.occupiedBeds
    val isFull = vacancy <= 0

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
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon circle
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(if (isFull) AppSoftRed else AppSoftBlue),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Bed,
                    contentDescription = null,
                    tint = if (isFull) AppRed else AppNavy,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Room ${room.roomNumber}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = AppText
                )
                Text(
                    "Floor ${room.floor}",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppMuted
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row {
                    // Beds pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(AppSoftBlue)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            "${room.bedCount} Beds",
                            style = MaterialTheme.typography.bodySmall,
                            color = AppNavy,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    // Vacancy pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(if (isFull) AppSoftRed else AppSoftGreen)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            if (isFull) "Full" else "$vacancy Available",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isFull) AppRed else AppGreen,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "₹${room.defaultRentPerBed / 100}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = AppText
                )
                Text(
                    "per bed",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppMuted
                )
            }
        }
    }
}