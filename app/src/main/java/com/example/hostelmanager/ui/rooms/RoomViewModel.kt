package com.example.hostelmanager.ui.rooms

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.hostelmanager.data.AppDatabase
import com.example.hostelmanager.data.RoomWithOccupancy
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.example.hostelmanager.data.RoomEntity

class RoomViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val dao = db.roomDao()

    private val _hostelId = MutableStateFlow("")

    val roomsWithOccupancy: StateFlow<List<RoomWithOccupancy>> = _hostelId.flatMapLatest { id ->
        if (id.isEmpty()) {
            flowOf(emptyList())
        } else {
            dao.getRoomsWithOccupancy(id)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun setHostelId(id: String) {
        _hostelId.value = id
    }

    fun addRoom(roomNumber: String, floor: Int, bedCount: Int, rentPerBed: Long) {
        if (_hostelId.value.isEmpty()) return
        viewModelScope.launch {
            dao.insertRoom(
                RoomEntity(
                    hostelId = _hostelId.value,
                    roomNumber = roomNumber,
                    floor = floor,
                    bedCount = bedCount,
                    defaultRentPerBed = rentPerBed
                )
            )
        }
    }
}