package com.example.hostelmanager.ui.rooms

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.hostelmanager.data.AppDatabase
import com.example.hostelmanager.data.RoomEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ArchivedRoomsViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val roomDao = db.roomDao()

    private val _hostelId = MutableStateFlow("")

    val archivedRooms: StateFlow<List<RoomEntity>> = _hostelId.flatMapLatest { id ->
        if (id.isEmpty()) flowOf(emptyList()) else roomDao.getArchivedRooms(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setHostelId(id: String) {
        _hostelId.value = id
    }

    // Bring a hidden room back — same id, so all its past records stay linked
    fun restoreRoom(room: RoomEntity) {
        viewModelScope.launch {
            roomDao.updateRoom(
                room.copy(
                    isArchived = false,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }
}