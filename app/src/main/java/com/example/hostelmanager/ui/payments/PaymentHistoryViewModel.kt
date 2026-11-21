package com.example.hostelmanager.ui.payments

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.hostelmanager.data.AppDatabase
import com.example.hostelmanager.data.Payment
import com.example.hostelmanager.data.ResidentWithRoom
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PaymentHistoryViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val personDao = db.personDao()
    private val paymentDao = db.paymentDao()

    private val _hostelId = MutableStateFlow("")
    private val _selectedMonth = MutableStateFlow<String?>(null)
    private val _selectedPersonId = MutableStateFlow<String?>(null)

    val residents: StateFlow<List<ResidentWithRoom>> = _hostelId.flatMapLatest { id ->
        if (id.isEmpty()) flowOf(emptyList())
        else personDao.getActiveResidentsByHostel(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val payments: StateFlow<List<Payment>> = combine(
        _hostelId,
        _selectedMonth,
        _selectedPersonId
    ) { hostelId, month, personId ->
        Triple(hostelId, month, personId)
    }.flatMapLatest { (hostelId, month, personId) ->
        if (hostelId.isEmpty()) {
            flowOf(emptyList())
        } else {
            paymentDao.getAllPaymentsForHostel(hostelId).flatMapLatest { allPayments ->
                val filtered = allPayments.filter { payment ->
                    val matchesMonth = month == null || payment.paymentDate.startsWith(month)
                    val matchesPerson = personId == null || payment.personId == personId
                    matchesMonth && matchesPerson
                }
                flowOf(filtered)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setHostelId(id: String) {
        _hostelId.value = id
    }

    fun setMonth(month: String?) {
        _selectedMonth.value = month
    }

    fun setPerson(personId: String?) {
        _selectedPersonId.value = personId
    }

    // NEW: delete payment
    fun deletePayment(paymentId: String) {
        viewModelScope.launch {
            paymentDao.deletePayment(paymentId)
        }
    }
}