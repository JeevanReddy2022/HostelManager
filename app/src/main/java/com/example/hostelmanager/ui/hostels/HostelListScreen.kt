package com.example.hostelmanager.ui.hostels

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel


import com.example.hostelmanager.data.Hostel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HostelListScreen(
    viewModel: HostelViewModel = viewModel()
) {
    val hostels by viewModel.hostels.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Hostels") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Hostel")
            }
        }
    ) { padding ->
        if (hostels.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    "No hostels yet",
                    style = MaterialTheme.typography.titleMedium
                )
                Text("Tap the + button to add your first hostel")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(hostels) { hostel ->
                    HostelCard(hostel)
                }
            }
        }
    }

    if (showAddDialog) {
        AddHostelDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, address, phone ->
                viewModel.addHostel(name, address, phone)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun HostelCard(hostel: Hostel) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                hostel.name,
                style = MaterialTheme.typography.titleLarge
            )
            if (hostel.address.isNotBlank()) {
                Text(
                    hostel.address,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            if (hostel.phone.isNotBlank()) {
                Text(
                    "📞 ${hostel.phone}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}