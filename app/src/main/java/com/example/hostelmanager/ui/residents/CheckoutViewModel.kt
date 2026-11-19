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

class CheckoutViewModel(application: Application) : AndroidViewModel(application) {

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

    private val _checkoutError = MutableStateFlow<String?>(null)
    val checkoutError: StateFlow<String?> = _checkoutError

    fun setPersonId(id: String) {
        _personId.value = id
    }

    suspend fun checkoutResident(
        checkoutDate: String,
        refundPaise: Long,
        refundMode: String?,
        refundDate: String?
    ): Boolean {
        val stay = activeStay.value ?: return false

        _checkoutError.value = null

        if (checkoutDate.isEmpty()) {
            _checkoutError.value = "Please choose a check-out date."
            return false
        }

        // Dates are stored as YYYY-MM-DD, so text comparison is correct
        if (checkoutDate < stay.joinDate) {
            _checkoutError.value = "Check-out date cannot be before the join date (${stay.joinDate})."
            return false
        }

        if (refundPaise < 0) {
            _checkoutError.value = "Refund cannot be negative."
            return false
        }

        if (refundPaise > stay.depositCollected) {
            _checkoutError.value = "Refund cannot be more than the deposit held (₹${stay.depositCollected / 100})."
            return false
        }

        if (refundPaise > 0 && (refundMode.isNullOrBlank() || refundDate.isNullOrBlank())) {
            _checkoutError.value = "Choose a refund mode and refund date."
            return false
        }

        // Close the stay. Nothing is deleted — the record and its history stay intact.
        stayDao.updateStay(
            stay.copy(
                isActive = false,
                checkoutDate = checkoutDate,
                depositRefunded = refundPaise,
                depositRefundMode = if (refundPaise > 0) refundMode else null,
                depositRefundDate = if (refundPaise > 0) refundDate else null,
                updatedAt = System.currentTimeMillis()
            )
        )

        return true
    }
}