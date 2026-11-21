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

class ResidentViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val personDao = db.personDao()
    private val stayDao = db.stayDao()
    private val roomDao = db.roomDao()

    private val _hostelId = MutableStateFlow("")

    private val _phoneError = MutableStateFlow<String?>(null)
    val phoneError: StateFlow<String?> = _phoneError

    private val _govtIdError = MutableStateFlow<String?>(null)
    val govtIdError: StateFlow<String?> = _govtIdError

    private val _emergencyError = MutableStateFlow<String?>(null)
    val emergencyError: StateFlow<String?> = _emergencyError

    val roomsForHostel: StateFlow<List<RoomWithOccupancy>> = _hostelId.flatMapLatest { id ->
        if (id.isEmpty()) flowOf(emptyList())
        else roomDao.getRoomsWithOccupancy(id)
    }.stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = emptyList())

    fun setHostelId(id: String) {
        _hostelId.value = id
    }

    suspend fun addResident(
        hostelId: String,
        name: String, phone: String, govtIdNumber: String, emergencyContact: String,
        dateOfBirth: String, address: String, gender: String, guardianName: String,
        roomId: String, joinDate: String, rentPaise: Long, depositPaise: Long,
        govtFrontPath: String, govtBackPath: String, profilePhotoPath: String
    ): Boolean {
        if (hostelId.isEmpty() || roomId.isEmpty()) return false

        _phoneError.value = null
        _govtIdError.value = null
        _emergencyError.value = null

        var hasDuplicates = false

        if (personDao.isPhoneDuplicate(phone)) {
            _phoneError.value = "This phone number is already in use!"
            hasDuplicates = true
        }
        if (personDao.isGovtIdDuplicate(govtIdNumber)) {
            _govtIdError.value = "This Govt ID is already in use!"
            hasDuplicates = true
        }
        if (personDao.isEmergencyDuplicate(emergencyContact)) {
            _emergencyError.value = "This emergency contact is already in use!"
            hasDuplicates = true
        }

        if (hasDuplicates) return false

        val newPerson = Person(
            name = name, phone = phone, govtIdNumber = govtIdNumber,
            emergencyContact = emergencyContact, dateOfBirth = dateOfBirth,
            address = address, gender = gender, guardianName = guardianName,
            govtIdPhotoPath = govtFrontPath,
            govtIdBackPath = govtBackPath,
            profilePhotoPath = profilePhotoPath
        )
        personDao.insertPerson(newPerson)

        // NEW: extract day of month from joinDate ("YYYY-MM-DD" → day)
        val anchorDay = joinDate.substring(8, 10).toIntOrNull() ?: 1

        val newStay = Stay(
            personId = newPerson.id,
            hostelId = hostelId,
            roomId = roomId,
            joinDate = joinDate,
            monthlyRent = rentPaise,
            depositCollected = depositPaise,
            billingAnchorDay = anchorDay,
            isActive = true
        )
        stayDao.insertStay(newStay)

        return true
    }
}