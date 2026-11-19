package com.example.hostelmanager.ui.residents

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.hostelmanager.data.AppDatabase
import com.example.hostelmanager.data.Person
import com.example.hostelmanager.data.RoomWithOccupancy
import com.example.hostelmanager.data.Stay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ReAdmitResidentViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val personDao = db.personDao()
    private val stayDao = db.stayDao()
    private val roomDao = db.roomDao()

    private val _personId = MutableStateFlow("")
    private val _hostelId = MutableStateFlow("")

    val person: StateFlow<Person?> = _personId.flatMapLatest { id ->
        if (id.isEmpty()) flowOf(null) else personDao.getPersonByIdFlow(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val roomsForHostel: StateFlow<List<RoomWithOccupancy>> = _hostelId.flatMapLatest { id ->
        if (id.isEmpty()) flowOf(emptyList())
        else roomDao.getRoomsWithOccupancy(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setPersonId(id: String) {
        _personId.value = id
    }

    fun setHostelId(id: String) {
        _hostelId.value = id
    }

    suspend fun reAdmitResident(
        roomId: String,
        joinDate: String,
        rentPaise: Long,
        depositPaise: Long
    ): Boolean {
        val p = person.value ?: return false
        if (_hostelId.value.isEmpty() || roomId.isEmpty() || joinDate.isEmpty()) return false

        stayDao.insertStay(
            Stay(
                personId = p.id,
                hostelId = _hostelId.value,
                roomId = roomId,
                joinDate = joinDate,
                monthlyRent = rentPaise,
                depositCollected = depositPaise,
                isActive = true
            )
        )
        return true
    }
}