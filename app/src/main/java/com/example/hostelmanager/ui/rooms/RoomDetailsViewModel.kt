package com.example.hostelmanager.ui.rooms

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.hostelmanager.data.AppDatabase
import com.example.hostelmanager.data.ResidentWithRoom
import com.example.hostelmanager.data.RoomEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn

class RoomDetailsViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val roomDao = db.roomDao()
    private val personDao = db.personDao()

    private val _roomId = MutableStateFlow("")

    val room: StateFlow<RoomEntity?> = _roomId.flatMapLatest { id ->
        if (id.isEmpty()) flowOf(null) else roomDao.getRoomById(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val residents: StateFlow<List<ResidentWithRoom>> = _roomId.flatMapLatest { id ->
        if (id.isEmpty()) flowOf(emptyList()) else personDao.getActiveResidentsByRoom(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setRoomId(id: String) {
        _roomId.value = id
    }
}