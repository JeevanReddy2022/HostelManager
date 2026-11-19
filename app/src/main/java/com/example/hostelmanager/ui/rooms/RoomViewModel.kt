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

    // NEW: error message shown when a duplicate room number is attempted
    private val _roomExistsError = MutableStateFlow<String?>(null)
    val roomExistsError: StateFlow<String?> = _roomExistsError

    fun setHostelId(id: String) {
        _hostelId.value = id
    }

    fun clearRoomError() {
        _roomExistsError.value = null
    }

    // NEW: checks for duplicates before inserting; returns true on success
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