package com.example.hostelmanager.ui.dashboard

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Receipt
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.hostelmanager.ui.expenses.ExpensesListScreen
import com.example.hostelmanager.ui.payments.PaymentsListScreen
import com.example.hostelmanager.ui.reports.ReportsScreen
import com.example.hostelmanager.ui.residents.ResidentListScreen
import com.example.hostelmanager.ui.rooms.RoomListScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HostelDashboardScreen(
    hostelId: String,
    onBackClick: () -> Unit,
    onAddResidentClick: () -> Unit,
    onRoomClick: (String) -> Unit,
    onResidentClick: (String) -> Unit,
    onArchivedRoomsClick: () -> Unit,
    onPastResidentsClick: () -> Unit,
    onRecordPaymentClick: (String) -> Unit,
    onPaymentHistoryClick: () -> Unit,
    onAddExpenseClick: () -> Unit
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    val tabs = listOf("Rooms", "Residents", "Payments", "Expenses", "Reports")
    val icons = listOf(
        Icons.Default.Home,
        Icons.Default.People,
        Icons.Default.Payments,
        Icons.Default.Receipt,
        Icons.Default.BarChart
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(tabs[selectedTab]) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (selectedTab == 0) {
                        IconButton(onClick = onArchivedRoomsClick) {
                            Icon(Icons.Default.Archive, contentDescription = "Archived Rooms")
                        }
                    } else if (selectedTab == 1) {
                        IconButton(onClick = onPastResidentsClick) {
                            Icon(Icons.Default.Archive, contentDescription = "Past Residents")
                        }
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
            2 -> PaymentsListScreen(
                hostelId = hostelId,
                onRecordPaymentClick = { personId -> onRecordPaymentClick(personId) },
                onPaymentHistoryClick = { onPaymentHistoryClick() },
                modifier = Modifier.padding(padding)
            )
            3 -> ExpensesListScreen(
                hostelId = hostelId,
                onAddExpenseClick = { onAddExpenseClick() },
                modifier = Modifier.padding(padding)
            )
            4 -> ReportsScreen(
                hostelId = hostelId,
                modifier = Modifier.padding(padding)
            )
        }
    }
}