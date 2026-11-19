package com.example.hostelmanager.ui.residents

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.hostelmanager.data.AppDatabase
import com.example.hostelmanager.data.Person
import com.example.hostelmanager.data.RoomEntity
import com.example.hostelmanager.data.RoomWithOccupancy
import com.example.hostelmanager.data.Stay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn

class TransferRoomViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val personDao = db.personDao()
    private val stayDao = db.stayDao()
    private val roomDao = db.roomDao()

    private val _personId = MutableStateFlow("")

    val person: StateFlow<Person?> = _personId.flatMapLatest { id ->
        if (id.isEmpty()) flowOf(null) else personDao.getPersonByIdFlow(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val activeStay: StateFlow<Stay?> = _personId.flatMapLatest { id ->
        if (id.isEmpty()) flowOf(null) else stayDao.getActiveStayByPerson(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // person -> active stay -> that room
    val currentRoom: StateFlow<RoomEntity?> = _personId.flatMapLatest { id ->
        if (id.isEmpty()) flowOf(null)
        else stayDao.getActiveStayByPerson(id).flatMapLatest { stay ->
            if (stay == null) flowOf(null) else roomDao.getRoomById(stay.roomId)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // person -> active stay -> that hostel's rooms
    val availableRooms: StateFlow<List<RoomWithOccupancy>> = _personId.flatMapLatest { id ->
        if (id.isEmpty()) flowOf(emptyList())
        else stayDao.getActiveStayByPerson(id).flatMapLatest { stay ->
            if (stay == null) flowOf(emptyList()) else roomDao.getRoomsWithOccupancy(stay.hostelId)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _transferError = MutableStateFlow<String?>(null)
    val transferError: StateFlow<String?> = _transferError

    fun setPersonId(id: String) {
        _personId.value = id
    }

    suspend fun transferRoom(newRoomId: String, transferDate: String, newRentPaise: Long): Boolean {
        val stay = activeStay.value ?: return false

        _transferError.value = null

        if (newRoomId.isEmpty() || transferDate.isEmpty()) return false

        if (newRoomId == stay.roomId) {
            _transferError.value = "Please choose a different room."
            return false
        }

        // Re-check the target room is still free (safety against stale UI)
        val target = availableRooms.value.firstOrNull { it.room.id == newRoomId }
        if (target == null) {
            _transferError.value = "That room is no longer available."
            return false
        }
        if (target.room.bedCount - target.occupiedBeds <= 0) {
            _transferError.value = "That room is now full."
            return false
        }

        // Dates are stored as YYYY-MM-DD, so text comparison works correctly
        if (transferDate < stay.joinDate) {
            _transferError.value = "Transfer date cannot be before the join date (${stay.joinDate})."
            return false
        }

        // 1) CLOSE the old stay — history is kept, not deleted
        stayDao.updateStay(
            stay.copy(
                isActive = false,
                checkoutDate = transferDate,
                updatedAt = System.currentTimeMillis()
            )
        )

        // 2) OPEN a new stay in the new room; deposit carries over
        stayDao.insertStay(
            Stay(
                personId = stay.personId,
                hostelId = stay.hostelId,
                roomId = newRoomId,
                joinDate = transferDate,
                monthlyRent = newRentPaise,
                depositCollected = stay.depositCollected,
                isActive = true
            )
        )

        return true
    }
}