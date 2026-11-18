package com.example.hostelmanager.ui.dashboard

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.hostelmanager.ui.residents.ResidentListScreen
import com.example.hostelmanager.ui.rooms.RoomListScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HostelDashboardScreen(
    hostelId: String,
    onBackClick: () -> Unit,
    onAddResidentClick: () -> Unit,
    onRoomClick: (String) -> Unit,
    onResidentClick: (String) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    val tabs = listOf("Rooms", "Residents")
    val icons = listOf(Icons.Default.Home, Icons.Default.People)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(tabs[selectedTab]) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                tabs.forEachIndexed { index, title ->
                    NavigationBarItem(
                        icon = { Icon(icons[index], contentDescription = title) },
                        label = { Text(title) },
                        selected = selectedTab == index,
                        onClick = { selectedTab = index }
                    )
                }
            }
        }
    ) { padding ->
        when (selectedTab) {
            0 -> RoomListScreen(
                hostelId = hostelId,
                onRoomClick = { roomId -> onRoomClick(roomId) },
                modifier = Modifier.padding(padding)
            )
            1 -> ResidentListScreen(
                hostelId = hostelId,
                onAddClick = { onAddResidentClick() },
                onResidentClick = { personId -> onResidentClick(personId) },
                modifier = Modifier.padding(padding)
            )
        }
    }
}