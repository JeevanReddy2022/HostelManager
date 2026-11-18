package com.example.hostelmanager.ui.residents

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.hostelmanager.data.AppDatabase
import com.example.hostelmanager.data.Person
import com.example.hostelmanager.data.RoomEntity
import com.example.hostelmanager.data.Stay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn

class ResidentDetailsViewModel(application: Application) : AndroidViewModel(application) {

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

    // The room of the active stay: person -> active stay -> room
    val room: StateFlow<RoomEntity?> = _personId.flatMapLatest { id ->
        if (id.isEmpty()) flowOf(null)
        else stayDao.getActiveStayByPerson(id).flatMapLatest { stay ->
            if (stay == null) flowOf(null) else roomDao.getRoomById(stay.roomId)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun setPersonId(id: String) {
        _personId.value = id
    }
}