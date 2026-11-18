package com.example.hostelmanager.ui.residents

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.hostelmanager.data.AppDatabase
import com.example.hostelmanager.data.Person
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn

class EditResidentViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val personDao = db.personDao()

    private val _personId = MutableStateFlow("")

    val person: StateFlow<Person?> = _personId.flatMapLatest { id ->
        if (id.isEmpty()) flowOf(null) else personDao.getPersonByIdFlow(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _phoneError = MutableStateFlow<String?>(null)
    val phoneError: StateFlow<String?> = _phoneError

    private val _govtIdError = MutableStateFlow<String?>(null)
    val govtIdError: StateFlow<String?> = _govtIdError

    private val _emergencyError = MutableStateFlow<String?>(null)
    val emergencyError: StateFlow<String?> = _emergencyError

    fun setPersonId(id: String) {
        _personId.value = id
    }

    suspend fun updateResident(
        name: String, phone: String, govtIdNumber: String, emergencyContact: String,
        dateOfBirth: String, address: String, gender: String, guardianName: String,
        govtFrontPath: String, govtBackPath: String, profilePhotoPath: String
    ): Boolean {
        val current = person.value ?: return false
        val personId = current.id

        _phoneError.value = null
        _govtIdError.value = null
        _emergencyError.value = null

        var hasDuplicates = false

        if (personDao.isPhoneDuplicateExcluding(phone, personId)) {
            _phoneError.value = "This phone number is already in use!"
            hasDuplicates = true
        }
        if (personDao.isGovtIdDuplicateExcluding(govtIdNumber, personId)) {
            _govtIdError.value = "This Govt ID is already in use!"
            hasDuplicates = true
        }
        if (personDao.isEmergencyDuplicateExcluding(emergencyContact, personId)) {
            _emergencyError.value = "This emergency contact is already in use!"
            hasDuplicates = true
        }

        if (hasDuplicates) return false

        // copy() keeps the same id + createdAt; only these fields change
        personDao.updatePerson(
            current.copy(
                name = name,
                phone = phone,
                govtIdNumber = govtIdNumber,
                emergencyContact = emergencyContact,
                dateOfBirth = dateOfBirth,
                address = address,
                gender = gender,
                guardianName = guardianName,
                govtIdPhotoPath = govtFrontPath,
                govtIdBackPath = govtBackPath,
                profilePhotoPath = profilePhotoPath,
                updatedAt = System.currentTimeMillis()
            )
        )
        return true
    }
}