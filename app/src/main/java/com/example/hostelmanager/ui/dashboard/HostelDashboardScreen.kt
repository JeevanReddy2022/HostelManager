package com.example.hostelmanager.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.hostelmanager.ui.expenses.ExpensesListScreen
import com.example.hostelmanager.ui.payments.PaymentsListScreen
import com.example.hostelmanager.ui.reports.ReportsScreen
import com.example.hostelmanager.ui.residents.ResidentListScreen
import com.example.hostelmanager.ui.rooms.RoomListScreen

// Premium Blue to Light Green Gradient
private val GradientBlue = Color(0xFF1E40AF) // Deep Royal Blue
private val GradientLightGreen = Color(0xFF34D399) // Fresh Mint/Emerald Green
private val HeaderFooterGradient = Brush.horizontalGradient(
    colors = listOf(GradientBlue, GradientLightGreen)
)

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
            // Gradient Header
            Box(modifier = Modifier.background(HeaderFooterGradient)) {
                TopAppBar(
                    title = {
                        Text(
                            text = tabs[selectedTab],
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                    },
                    actions = {
                        if (selectedTab == 0) {
                            IconButton(onClick = onArchivedRoomsClick) {
                                Icon(Icons.Default.Archive, contentDescription = "Archived Rooms", tint = Color.White)
                            }
                        } else if (selectedTab == 1) {
                            IconButton(onClick = onPastResidentsClick) {
                                Icon(Icons.Default.Archive, contentDescription = "Past Residents", tint = Color.White)
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent, // Allows gradient to show through
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White,
                        actionIconContentColor = Color.White
                    )
                )
            }
        },
        bottomBar = {
            // Gradient Footer
            Box(modifier = Modifier.background(HeaderFooterGradient)) {
                NavigationBar(
                    containerColor = Color.Transparent, // Allows gradient to show through
                    contentColor = Color.White,
                    tonalElevation = 0.dp // Remove shadow to blend perfectly with gradient
                ) {
                    tabs.forEachIndexed { index, title ->
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    icons[index],
                                    contentDescription = title,
                                    modifier = Modifier.size(24.dp)
                                )
                            },
                            label = {
                                Text(
                                    title,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.White,
                                unselectedIconColor = Color.White.copy(alpha = 0.6f),
                                selectedTextColor = Color.White,
                                unselectedTextColor = Color.White.copy(alpha = 0.6f),
                                indicatorColor = Color.White.copy(alpha = 0.2f) // Subtle white pill for selected tab
                            )
                        )
                    }
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