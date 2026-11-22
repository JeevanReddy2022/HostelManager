package com.example.hostelmanager.ui.rooms

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.hostelmanager.data.RoomWithOccupancy
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

@Composable
fun RoomPickerDialog(
    rooms: List<RoomWithOccupancy>,
    onDismiss: () -> Unit,
    onSelect: (RoomWithOccupancy) -> Unit
) {
    var query by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    // Filter by room number, sort naturally (101 before 1021)
    val filtered = rooms
        .filter { it.room.roomNumber.contains(query.trim(), ignoreCase = true) }
        .sortedWith(compareBy(
            { it.room.roomNumber.toLongOrNull() ?: Long.MAX_VALUE },
            { it.room.roomNumber }
        ))

    val vacantCount = rooms.count { (it.room.bedCount - it.occupiedBeds) > 0 }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = AppCard,
        titleContentColor = AppText,
        textContentColor = AppText,
        title = {
            Column {
                Text(
                    "Select Room",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    "$vacantCount of ${rooms.size} rooms have vacancy",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppMuted
                )
            }
        },
        text = {
            Column {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("Search room number…", color = AppMuted) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = AppNavy)
                    },
                    trailingIcon = {
                        if (query.isNotEmpty()) {
                            IconButton(onClick = { query = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", tint = AppMuted)
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = AppText,
                        unfocusedTextColor = AppText,
                        focusedBorderColor = AppNavy,
                        unfocusedBorderColor = AppBorder,
                        cursorColor = AppNavy
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (filtered.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No rooms match \"$query\"",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AppMuted
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.height(340.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 4.dp)
                    ) {
                        items(filtered) { roomWithOcc ->
                            RoomPickerRow(
                                roomWithOcc = roomWithOcc,
                                onClick = {
                                    onSelect(roomWithOcc)
                                    onDismiss()
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = AppMuted)
            }
        }
    )
}

@Composable
private fun RoomPickerRow(
    roomWithOcc: RoomWithOccupancy,
    onClick: () -> Unit
) {
    val room = roomWithOcc.room
    val vacancy = room.bedCount - roomWithOcc.occupiedBeds
    val isFull = vacancy <= 0

    val rowAlpha = if (isFull) 0.45f else 1f

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (isFull) AppCard else AppSoftBlue.copy(alpha = 0.35f))
            .then(
                if (isFull) Modifier else Modifier.clickable { onClick() }
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Room icon circle
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (isFull) AppBorder else AppSoftBlue),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "${room.bedCount}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = AppNavy.copy(alpha = rowAlpha)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Room ${room.roomNumber}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = AppText.copy(alpha = rowAlpha)
                )
                Text(
                    "Beds: ${room.bedCount} · Rent: ₹${room.defaultRentPerBed / 100}",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppMuted.copy(alpha = rowAlpha)
                )
            }

            // Vacancy pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(if (isFull) AppSoftRed else AppSoftGreen)
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Text(
                    if (isFull) "Full" else "$vacancy vacant",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isFull) AppRed else AppGreen,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}