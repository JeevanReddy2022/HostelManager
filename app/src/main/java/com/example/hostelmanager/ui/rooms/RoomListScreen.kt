package com.example.hostelmanager.ui.rooms

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Bed
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hostelmanager.data.RoomWithOccupancy
import com.example.hostelmanager.ui.theme.AppBackground
import com.example.hostelmanager.ui.theme.AppBorder
import com.example.hostelmanager.ui.theme.AppCard
import com.example.hostelmanager.ui.theme.AppGreen
import com.example.hostelmanager.ui.theme.AppMuted
import com.example.hostelmanager.ui.theme.AppNavy
import com.example.hostelmanager.ui.theme.AppOrange
import com.example.hostelmanager.ui.theme.AppRed
import com.example.hostelmanager.ui.theme.AppSoftBlue
import com.example.hostelmanager.ui.theme.AppSoftGreen
import com.example.hostelmanager.ui.theme.AppSoftOrange
import com.example.hostelmanager.ui.theme.AppSoftRed
import com.example.hostelmanager.ui.theme.AppText
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

private enum class FilterPicker { AVAILABILITY, FLOOR, SHARING, SORT }

@Composable
fun RoomListScreen(
    hostelId: String,
    onRoomClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RoomViewModel = viewModel()
) {
    LaunchedEffect(hostelId) { viewModel.setHostelId(hostelId) }

    val allRooms by viewModel.roomsWithOccupancy.collectAsState()
    val filters by viewModel.filters.collectAsState()
    val roomError by viewModel.roomExistsError.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var activePicker by remember { mutableStateOf<FilterPicker?>(null) }
    val scope = rememberCoroutineScope()

    val visibleRooms = remember(allRooms, filters) { filterAndSortRooms(allRooms, filters) }
    val floors = remember(allRooms) { allRooms.map { it.room.floor }.distinct().sorted() }
    val sharingOptions = remember(allRooms) { allRooms.map { it.room.bedCount }.distinct().sorted() }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackground)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            if (allRooms.isNotEmpty()) {
                RoomControlDeck(
                    filters = filters,
                    resultCount = visibleRooms.size,
                    totalCount = allRooms.size,
                    onQueryChange = viewModel::setQuery,
                    onOpenPicker = { activePicker = it },
                    onClearAll = viewModel::clearFilters
                )
            }

            when {
                allRooms.isEmpty() -> EmptyHostelRooms()

                visibleRooms.isEmpty() -> NoMatchingRooms(
                    query = filters.query,
                    onClearAll = viewModel::clearFilters
                )

                else -> LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(top = 14.dp, bottom = 96.dp)
                ) {
                    items(visibleRooms, key = { it.room.id }) { roomWithOccupancy ->
                        RoomCard(
                            roomWithOccupancy = roomWithOccupancy,
                            onClick = { onRoomClick(roomWithOccupancy.room.id) },
                            modifier = Modifier.animateItem()
                        )
                    }
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
            contentColor = Color.White,
            elevation = FloatingActionButtonDefaults.elevation(6.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Room")
        }
    }

    /* ---------------- Add room ---------------- */

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

    /* ---------------- Filter / sort pickers ---------------- */

    when (activePicker) {
        FilterPicker.AVAILABILITY -> {
            val options = RoomAvailability.entries.map { option ->
                val count = when (option) {
                    RoomAvailability.ALL -> allRooms.size
                    RoomAvailability.AVAILABLE -> allRooms.count { it.room.bedCount - it.occupiedBeds > 0 }
                    RoomAvailability.FULL -> allRooms.count { it.room.bedCount - it.occupiedBeds <= 0 }
                }
                PickerOption(
                    label = option.label,
                    caption = option.caption,
                    trailing = "$count",
                    selected = filters.availability == option,
                    onSelect = { viewModel.setAvailability(option) }
                )
            }
            FilterPickerDialog(
                title = "Availability",
                subtitle = "Filter rooms by free beds",
                icon = Icons.Default.FilterList,
                options = options,
                onDismiss = { activePicker = null }
            )
        }

        FilterPicker.FLOOR -> {
            val options = buildList {
                add(
                    PickerOption(
                        label = "All floors",
                        caption = "Show rooms from every floor",
                        trailing = "${allRooms.size}",
                        selected = filters.floor == null,
                        onSelect = { viewModel.setFloor(null) }
                    )
                )
                floors.forEach { floor ->
                    val count = allRooms.count { it.room.floor == floor }
                    add(
                        PickerOption(
                            label = floorLabel(floor),
                            caption = "$count room${if (count == 1) "" else "s"} on this floor",
                            trailing = "$count",
                            selected = filters.floor == floor,
                            onSelect = { viewModel.setFloor(floor) }
                        )
                    )
                }
            }
            FilterPickerDialog(
                title = "Floor",
                subtitle = "Pick a floor to narrow the list",
                icon = Icons.Default.Layers,
                options = options,
                onDismiss = { activePicker = null }
            )
        }

        FilterPicker.SHARING -> {
            val options = buildList {
                add(
                    PickerOption(
                        label = "All sharing types",
                        caption = "Any number of beds",
                        trailing = "${allRooms.size}",
                        selected = filters.sharing == null,
                        onSelect = { viewModel.setSharing(null) }
                    )
                )
                sharingOptions.forEach { beds ->
                    val count = allRooms.count { it.room.bedCount == beds }
                    add(
                        PickerOption(
                            label = "$beds-Sharing",
                            caption = sharingName(beds),
                            trailing = "$count",
                            selected = filters.sharing == beds,
                            onSelect = { viewModel.setSharing(beds) }
                        )
                    )
                }
            }
            FilterPickerDialog(
                title = "Sharing",
                subtitle = "Filter by beds per room",
                icon = Icons.Default.Bed,
                options = options,
                onDismiss = { activePicker = null }
            )
        }

        FilterPicker.SORT -> {
            val options = RoomSort.entries.map { option ->
                PickerOption(
                    label = option.label,
                    caption = option.caption,
                    trailing = option.short,
                    selected = filters.sort == option,
                    onSelect = { viewModel.setSort(option) }
                )
            }
            FilterPickerDialog(
                title = "Sort rooms",
                subtitle = "Choose the order of the list",
                icon = Icons.Default.SwapVert,
                options = options,
                onDismiss = { activePicker = null }
            )
        }

        null -> Unit
    }
}

/* ------------------------------------------------------------------ */
/*  Control deck — search, sort, filters                               */
/* ------------------------------------------------------------------ */

@Composable
private fun RoomControlDeck(
    filters: RoomFilters,
    resultCount: Int,
    totalCount: Int,
    onQueryChange: (String) -> Unit,
    onOpenPicker: (FilterPicker) -> Unit,
    onClearAll: () -> Unit
) {
    Surface(
        color = AppCard,
        shadowElevation = 3.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = filters.query,
                    onValueChange = onQueryChange,
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    placeholder = { Text("Search room number…", color = AppMuted) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(20.dp))
                    },
                    trailingIcon = {
                        if (filters.query.isNotEmpty()) {
                            IconButton(onClick = { onQueryChange("") }) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Clear search",
                                    tint = AppMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    },
                    textStyle = MaterialTheme.typography.bodyLarge,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = AppText,
                        unfocusedTextColor = AppText,
                        focusedBorderColor = AppNavy,
                        unfocusedBorderColor = AppBorder,
                        focusedContainerColor = AppBackground,
                        unfocusedContainerColor = AppBackground,
                        cursorColor = AppNavy,
                        focusedLeadingIconColor = AppNavy,
                        unfocusedLeadingIconColor = AppMuted
                    )
                )

                val sortChanged = filters.sort != RoomSort.NUMBER_ASC
                Surface(
                    onClick = { onOpenPicker(FilterPicker.SORT) },
                    shape = RoundedCornerShape(14.dp),
                    color = if (sortChanged) AppNavy else AppBackground,
                    contentColor = if (sortChanged) Color.White else AppText,
                    border = if (sortChanged) null else BorderStroke(1.dp, AppBorder),
                    modifier = Modifier
                        .width(70.dp)
                        .height(56.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.SwapVert,
                            contentDescription = "Sort rooms",
                            modifier = Modifier.size(19.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            filters.sort.short,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    label = filters.availability.label,
                    icon = Icons.Default.FilterList,
                    active = filters.availability != RoomAvailability.ALL,
                    onClick = { onOpenPicker(FilterPicker.AVAILABILITY) }
                )
                FilterChip(
                    label = filters.floor?.let { floorLabel(it) } ?: "Floor",
                    icon = Icons.Default.Layers,
                    active = filters.floor != null,
                    onClick = { onOpenPicker(FilterPicker.FLOOR) }
                )
                FilterChip(
                    label = filters.sharing?.let { "$it-Sharing" } ?: "Sharing",
                    icon = Icons.Default.Bed,
                    active = filters.sharing != null,
                    onClick = { onOpenPicker(FilterPicker.SHARING) }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Showing $resultCount of $totalCount rooms",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppMuted,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
                if (filters.isFiltered) {
                    Surface(
                        onClick = onClearAll,
                        shape = RoundedCornerShape(50),
                        color = AppSoftRed,
                        contentColor = AppRed
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = null,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                "Clear ${filters.activeCount}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterChip(
    label: String,
    icon: ImageVector,
    active: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        color = if (active) AppNavy else AppBackground,
        contentColor = if (active) Color.White else AppText,
        border = if (active) null else BorderStroke(1.dp, AppBorder)
    ) {
        Row(
            modifier = Modifier.padding(start = 12.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                maxLines = 1
            )
            Icon(
                Icons.Default.ArrowDropDown,
                contentDescription = null,
                modifier = Modifier.size(17.dp)
            )
        }
    }
}

/* ------------------------------------------------------------------ */
/*  Room card                                                          */
/* ------------------------------------------------------------------ */

@Composable
fun RoomCard(
    roomWithOccupancy: RoomWithOccupancy,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val room = roomWithOccupancy.room
    val vacancy = (room.bedCount - roomWithOccupancy.occupiedBeds).coerceAtLeast(0)
    val isFull = vacancy <= 0
    val isLastBed = vacancy == 1

    val accent = when {
        isFull -> AppRed
        isLastBed -> AppOrange
        else -> AppGreen
    }
    val softAccent = when {
        isFull -> AppSoftRed
        isLastBed -> AppSoftOrange
        else -> AppSoftGreen
    }
    val statusText = when {
        isFull -> "Full"
        isLastBed -> "1 left"
        else -> "$vacancy vacant"
    }
    val fillRatio = if (room.bedCount <= 0) 0f
    else (roomWithOccupancy.occupiedBeds.toFloat() / room.bedCount).coerceIn(0f, 1f)

    Card(
        modifier = modifier
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
                .height(IntrinsicSize.Min)
        ) {
            Box(
                modifier = Modifier
                    .width(5.dp)
                    .fillMaxHeight()
                    .background(accent)
            )

            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Sharing tile
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(softAccent),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "${room.bedCount}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = accent
                        )
                        Text(
                            "BEDS",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = accent.copy(alpha = 0.75f)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Room ${room.roomNumber}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = AppText,
                        maxLines = 1
                    )
                    Text(
                        "${floorLabel(room.floor)} · ${room.bedCount}-Sharing",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppMuted,
                        maxLines = 1
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(50))
                            .background(AppBorder)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(fillRatio)
                                .height(6.dp)
                                .clip(RoundedCornerShape(50))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(accent.copy(alpha = 0.65f), accent)
                                    )
                                )
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

//                    Text(
//                        "${roomWithOccupancy.occupiedBeds} of ${room.bedCount} beds occupied",
//                        fontSize = 11.sp,
//                        color = AppMuted,
//                        maxLines = 1
//                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        formatRupees(room.defaultRentPerBed),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = AppNavy,
                        maxLines = 1
                    )
                    Text(
                        "per bed",
                        fontSize = 10.sp,
                        color = AppMuted
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(softAccent)
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            statusText,
                            fontSize = 11.sp,
                            color = accent,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

/* ------------------------------------------------------------------ */
/*  Picker dialog                                                      */
/* ------------------------------------------------------------------ */

private data class PickerOption(
    val label: String,
    val caption: String,
    val trailing: String,
    val selected: Boolean,
    val onSelect: () -> Unit
)

@Composable
private fun FilterPickerDialog(
    title: String,
    subtitle: String,
    icon: ImageVector,
    options: List<PickerOption>,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = AppCard,
        titleContentColor = AppText,
        textContentColor = AppText,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(AppSoftBlue),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = AppNavy,
                        modifier = Modifier.size(19.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = AppMuted
                    )
                }
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier.heightIn(max = 380.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(options) { option ->
                    Surface(
                        onClick = {
                            option.onSelect()
                            onDismiss()
                        },
                        shape = RoundedCornerShape(14.dp),
                        color = if (option.selected) AppSoftBlue else AppBackground,
                        contentColor = AppText,
                        border = BorderStroke(
                            1.dp,
                            if (option.selected) AppNavy.copy(alpha = 0.35f) else AppBorder
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    option.label,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = if (option.selected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (option.selected) AppNavy else AppText
                                )
                                Text(
                                    option.caption,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = AppMuted
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(if (option.selected) AppNavy else AppBorder)
                                    .padding(horizontal = 9.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    option.trailing,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (option.selected) Color.White else AppMuted
                                )
                            }
                            if (option.selected) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = AppNavy,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done", color = AppNavy, fontWeight = FontWeight.Bold)
            }
        }
    )
}

/* ------------------------------------------------------------------ */
/*  Empty states                                                       */
/* ------------------------------------------------------------------ */

@Composable
private fun EmptyHostelRooms() {
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
            color = AppText,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            "Tap the + button to add a room.",
            style = MaterialTheme.typography.bodyMedium,
            color = AppMuted
        )
    }
}

@Composable
private fun NoMatchingRooms(query: String, onClearAll: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(AppSoftOrange),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.SearchOff,
                contentDescription = null,
                tint = AppOrange,
                modifier = Modifier.size(38.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "No rooms match",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = AppText
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            if (query.isNotBlank())
                "Nothing found for “$query” with the current filters."
            else "Try a different floor, sharing type or availability.",
            style = MaterialTheme.typography.bodyMedium,
            color = AppMuted,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(18.dp))
        Surface(
            onClick = onClearAll,
            shape = RoundedCornerShape(14.dp),
            color = AppNavy,
            contentColor = Color.White
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Clear all filters", fontWeight = FontWeight.Bold)
            }
        }
    }
}

/* ------------------------------------------------------------------ */
/*  Helpers                                                            */
/* ------------------------------------------------------------------ */

private fun formatRupees(amountPaise: Long): String {
    val rupees = amountPaise / 100
    val formatted = NumberFormat.getNumberInstance(Locale("en", "IN")).apply {
        maximumFractionDigits = 0
        minimumFractionDigits = 0
    }.format(rupees)
    return "₹$formatted"
}