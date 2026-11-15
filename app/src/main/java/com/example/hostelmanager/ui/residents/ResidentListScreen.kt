package com.example.hostelmanager.ui.residents

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
fun ResidentListScreen(
    hostelId: String,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ResidentListViewModel = viewModel()
) {
    LaunchedEffect(hostelId) {
        viewModel.setHostelId(hostelId)
    }

    val residents by viewModel.residents.collectAsState()

    Box(modifier = modifier.fillMaxSize()) {
        if (residents.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text("No active residents in this hostel.", style = MaterialTheme.typography.titleMedium)
                Text("Tap the person-icon button to admit a new resident.")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(residents) { residentWithRoom ->
                    ResidentCard(residentWithRoom)
                }
            }
        }

        FloatingActionButton(
            onClick = onAddClick,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .padding(bottom = 70.dp)
        ) {
            Icon(Icons.Default.PersonAdd, contentDescription = "Add Resident")
        }
    }
}

@Composable
fun ResidentCard(residentWithRoom: ResidentWithRoom) {
    val person = residentWithRoom.person
    val context = LocalContext.current // Gives us access to the phone's capabilities

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween, // Pushes text to left, icons to right
            verticalAlignment = Alignment.CenterVertically
        ) {
            // LEFT SIDE: Name and Room
            Column(modifier = Modifier.weight(1f)) {
                Text(person.name, style = MaterialTheme.typography.titleLarge)
                Text("🛏️ Room: ${residentWithRoom.roomNumber}", style = MaterialTheme.typography.bodyMedium)
            }

            // RIGHT SIDE: Call and Message Icons
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Call Button
                IconButton(
                    onClick = {
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${person.phone}"))
                        context.startActivity(intent)
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = "Call",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                // Message Button
                IconButton(
                    onClick = {
                        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:${person.phone}"))
                        context.startActivity(intent)
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Message,
                        contentDescription = "Message",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}