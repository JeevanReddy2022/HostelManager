package com.example.hostelmanager.ui.rooms

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.hostelmanager.data.AppDatabase
import com.example.hostelmanager.data.RoomEntity
import com.example.hostelmanager.data.RoomWithOccupancy
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/* ------------------------------------------------------------------ */
/*  Filter / sort model                                                */
/* ------------------------------------------------------------------ */

enum class RoomAvailability(val label: String, val caption: String) {
    ALL("All rooms", "Show every room in this hostel"),
    AVAILABLE("Available", "Rooms with at least one free bed"),
    FULL("Full", "Rooms with no free beds")
}

enum class RoomSort(val label: String, val short: String, val caption: String) {
    NUMBER_ASC("Room number · Low → High", "No. ↑", "101, 102, 103 …"),
    NUMBER_DESC("Room number · High → Low", "No. ↓", "305, 304, 303 …"),
    FLOOR_ASC("Floor · Ground up", "Floor ↑", "Ground floor first"),
    SHARING_ASC("Sharing · Fewest beds", "Beds ↑", "Single, double, triple …"),
    SHARING_DESC("Sharing · Most beds", "Beds ↓", "Largest sharing first"),
    VACANCY_DESC("Vacancy · Most free beds", "Vacant", "Emptiest rooms first"),
    RENT_ASC("Rent · Low → High", "₹ ↑", "Cheapest bed first"),
    RENT_DESC("Rent · High → Low", "₹ ↓", "Costliest bed first")
}

data class RoomFilters(
    val query: String = "",
    val floor: Int? = null,
    val sharing: Int? = null,
    val availability: RoomAvailability = RoomAvailability.ALL,
    val sort: RoomSort = RoomSort.NUMBER_ASC
) {
    val activeCount: Int
        get() = (if (query.isNotBlank()) 1 else 0) +
                (if (floor != null) 1 else 0) +
                (if (sharing != null) 1 else 0) +
                (if (availability != RoomAvailability.ALL) 1 else 0)

    val isFiltered: Boolean get() = activeCount > 0
}

data class RoomSummary(
    val rooms: Int = 0,
    val beds: Int = 0,
    val occupied: Int = 0,
    val fullRooms: Int = 0
) {
    val vacant: Int get() = (beds - occupied).coerceAtLeast(0)
    val openRooms: Int get() = (rooms - fullRooms).coerceAtLeast(0)
    val ratio: Float get() = if (beds <= 0) 0f else (occupied.toFloat() / beds).coerceIn(0f, 1f)
}

internal fun floorLabel(floor: Int): String = if (floor == 0) "Ground Floor" else "Floor $floor"

internal fun sharingName(beds: Int): String = when (beds) {
    1 -> "Single occupancy"
    2 -> "Double occupancy"
    3 -> "Triple occupancy"
    4 -> "Four occupancy"
    else -> "$beds occupancy"
}

internal fun filterAndSortRooms(
    rooms: List<RoomWithOccupancy>,
    f: RoomFilters
): List<RoomWithOccupancy> {
    val q = f.query.trim()

    val filtered = rooms.filter { r ->
        val vacancy = r.room.bedCount - r.occupiedBeds
        val matchesQuery = q.isEmpty() || r.room.roomNumber.contains(q, ignoreCase = true)
        val matchesFloor = f.floor == null || r.room.floor == f.floor
        val matchesSharing = f.sharing == null || r.room.bedCount == f.sharing
        val matchesAvailability = when (f.availability) {
            RoomAvailability.ALL -> true
            RoomAvailability.AVAILABLE -> vacancy > 0
            RoomAvailability.FULL -> vacancy <= 0
        }
        matchesQuery && matchesFloor && matchesSharing && matchesAvailability
    }

    val byNumber = compareBy<RoomWithOccupancy>(
        { it.room.roomNumber.toLongOrNull() ?: Long.MAX_VALUE },
        { it.room.roomNumber }
    )

    val comparator: Comparator<RoomWithOccupancy> = when (f.sort) {
        RoomSort.NUMBER_ASC -> byNumber
        RoomSort.NUMBER_DESC -> byNumber.reversed()
        RoomSort.FLOOR_ASC -> compareBy<RoomWithOccupancy>({ it.room.floor }).then(byNumber)
        RoomSort.SHARING_ASC -> compareBy<RoomWithOccupancy>({ it.room.bedCount }).then(byNumber)
        RoomSort.SHARING_DESC -> compareByDescending<RoomWithOccupancy>({ it.room.bedCount }).then(byNumber)
        RoomSort.VACANCY_DESC -> compareByDescending<RoomWithOccupancy>({ it.room.bedCount - it.occupiedBeds }).then(byNumber)
        RoomSort.RENT_ASC -> compareBy<RoomWithOccupancy>({ it.room.defaultRentPerBed }).then(byNumber)
        RoomSort.RENT_DESC -> compareByDescending<RoomWithOccupancy>({ it.room.defaultRentPerBed }).then(byNumber)
    }

    return filtered.sortedWith(comparator)
}

internal fun summarizeRooms(rooms: List<RoomWithOccupancy>): RoomSummary = RoomSummary(
    rooms = rooms.size,
    beds = rooms.sumOf { it.room.bedCount },
    occupied = rooms.sumOf { it.occupiedBeds },
    fullRooms = rooms.count { it.occupiedBeds >= it.room.bedCount }
)

/* ------------------------------------------------------------------ */
/*  ViewModel                                                          */
/* ------------------------------------------------------------------ */

class RoomViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val dao = db.roomDao()

    private val _hostelId = MutableStateFlow("")

    val roomsWithOccupancy: StateFlow<List<RoomWithOccupancy>> = _hostelId.flatMapLatest { id ->
        if (id.isEmpty()) flowOf(emptyList())
        else dao.getRoomsWithOccupancy(id)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Lives in the ViewModel so filters survive tab switches and rotation.
    private val _filters = MutableStateFlow(RoomFilters())
    val filters: StateFlow<RoomFilters> = _filters

    private val _roomExistsError = MutableStateFlow<String?>(null)
    val roomExistsError: StateFlow<String?> = _roomExistsError

    fun setHostelId(id: String) {
        if (_hostelId.value != id) {
            _hostelId.value = id
            _filters.value = RoomFilters()
        }
    }

    fun setQuery(value: String) {
        _filters.value = _filters.value.copy(query = value.take(12))
    }

    fun setFloor(floor: Int?) {
        _filters.value = _filters.value.copy(floor = floor)
    }

    fun setSharing(beds: Int?) {
        _filters.value = _filters.value.copy(sharing = beds)
    }

    fun setAvailability(value: RoomAvailability) {
        _filters.value = _filters.value.copy(availability = value)
    }

    fun setSort(value: RoomSort) {
        _filters.value = _filters.value.copy(sort = value)
    }

    /** Resets search + filters, keeps the chosen sort order. */
    fun clearFilters() {
        _filters.value = _filters.value.copy(
            query = "",
            floor = null,
            sharing = null,
            availability = RoomAvailability.ALL
        )
    }

    fun clearRoomError() {
        _roomExistsError.value = null
    }

    suspend fun tryAddRoom(
        roomNumber: String,
        floor: Int,
        bedCount: Int,
        rentPerBed: Long
    ): Boolean {
        if (_hostelId.value.isEmpty()) return false

        val count = dao.countRoomsWithNumber(_hostelId.value, roomNumber)
        if (count > 0) {
            _roomExistsError.value =
                "Room $roomNumber already exists in this hostel. You can restore it from the Archived Rooms screen."
            return false
        }

        _roomExistsError.value = null
        dao.insertRoom(
            RoomEntity(
                hostelId = _hostelId.value,
                roomNumber = roomNumber,
                floor = floor,
                bedCount = bedCount,
                defaultRentPerBed = rentPerBed
            )
        )
        return true
    }
}