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
import kotlinx.coroutines.launch

// The information we need to ask the owner about a rent revision
data class RentRevisionPrompt(
    val oldRentPaise: Long,
    val newRentPaise: Long,
    val residentCount: Int
)

class RoomDetailsViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val roomDao = db.roomDao()
    private val personDao = db.personDao()
    private val stayDao = db.stayDao()

    private val _roomId = MutableStateFlow("")

    val room: StateFlow<RoomEntity?> = _roomId.flatMapLatest { id ->
        if (id.isEmpty()) flowOf(null) else roomDao.getRoomById(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val residents: StateFlow<List<ResidentWithRoom>> = _roomId.flatMapLatest { id ->
        if (id.isEmpty()) flowOf(emptyList()) else personDao.getActiveResidentsByRoom(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // How many stays have EVER existed in this room (active + past).
    // 0 means "never used" -> safe to hard-delete.
    val totalStays: StateFlow<Int> = _roomId.flatMapLatest { id ->
        if (id.isEmpty()) flowOf(0) else stayDao.countStaysForRoom(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // When non-null, the screen shows the "update all residents?" popup
    private val _rentPrompt = MutableStateFlow<RentRevisionPrompt?>(null)
    val rentPrompt: StateFlow<RentRevisionPrompt?> = _rentPrompt

    // Becomes true once the room is archived OR deleted -> the screen goes back
    private val _roomRemoved = MutableStateFlow(false)
    val roomRemoved: StateFlow<Boolean> = _roomRemoved

    fun setRoomId(id: String) {
        _roomId.value = id
    }

    fun updateRoom(roomNumber: String, floor: Int, bedCount: Int, rentPerBedPaise: Long) {
        val current = room.value ?: return
        val oldRent = current.defaultRentPerBed
        val residentCount = residents.value.size

        viewModelScope.launch {
            roomDao.updateRoom(
                current.copy(
                    roomNumber = roomNumber,
                    floor = floor,
                    bedCount = bedCount,
                    defaultRentPerBed = rentPerBedPaise,
                    updatedAt = System.currentTimeMillis()
                )
            )
            // Ask ONLY if the rent actually changed AND someone lives here
            if (rentPerBedPaise != oldRent && residentCount > 0) {
                _rentPrompt.value = RentRevisionPrompt(oldRent, rentPerBedPaise, residentCount)
            }
        }
    }

    // Owner tapped "Yes, update all"
    fun applyRentToAllResidents() {
        val prompt = _rentPrompt.value ?: return
        val id = _roomId.value
        if (id.isEmpty()) return
        viewModelScope.launch {
            stayDao.updateRentForActiveStays(id, prompt.newRentPaise, System.currentTimeMillis())
            _rentPrompt.value = null
        }
    }

    // Owner tapped "No, keep existing"
    fun dismissRentPrompt() {
        _rentPrompt.value = null
    }

    // ARCHIVE: hide a used-but-empty room (keeps its history)
    fun archiveRoom() {
        val current = room.value ?: return
        viewModelScope.launch {
            roomDao.updateRoom(
                current.copy(
                    isArchived = true,
                    updatedAt = System.currentTimeMillis()
                )
            )
            _roomRemoved.value = true
        }
    }

    // HARD DELETE: permanently erase a room that was NEVER used
    fun deleteRoomPermanently() {
        val current = room.value ?: return
        viewModelScope.launch {
            roomDao.deleteRoom(current)
            _roomRemoved.value = true
        }
    }
}