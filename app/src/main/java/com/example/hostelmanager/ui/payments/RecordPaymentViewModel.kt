package com.example.hostelmanager.ui.payments

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.hostelmanager.data.AppDatabase
import com.example.hostelmanager.data.Payment
import com.example.hostelmanager.data.Person
import com.example.hostelmanager.data.ResidentWithRoom
import com.example.hostelmanager.data.ResidentWithStayInfo
import com.example.hostelmanager.data.Stay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class RecordPaymentViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val personDao = db.personDao()
    private val stayDao = db.stayDao()
    private val paymentDao = db.paymentDao()

    private val _hostelId = MutableStateFlow("")

    val residents: StateFlow<List<ResidentWithRoom>> = _hostelId.flatMapLatest { hostelId ->
        if (hostelId.isEmpty()) {
            flowOf(emptyList())
        } else {
            personDao.getAllResidentsWithLatestStay(hostelId).flatMapLatest { allResidents ->
                flow {
                    val result = mutableListOf<ResidentWithRoom>()
                    val today = LocalDate.now()

                    for (resident in allResidents) {
                        val anchorDay = resident.billingAnchorDay.coerceIn(1, 31)

                        val cycleStart: LocalDate = if (resident.isActive) {
                            calculateCycleStart(today, anchorDay)
                        } else {
                            val checkoutDate = resident.checkoutDate?.let {
                                LocalDate.parse(it, DateTimeFormatter.ISO_LOCAL_DATE)
                            } ?: continue
                            calculateCycleStart(checkoutDate, anchorDay)
                        }
                        val cycleEnd = cycleStart.plusMonths(1).minusDays(1)

                        val cycleStartStr = cycleStart.format(DateTimeFormatter.ISO_LOCAL_DATE)
                        val cycleEndStr = cycleEnd.format(DateTimeFormatter.ISO_LOCAL_DATE)

                        val payments = paymentDao.getPaymentsInCycle(
                            resident.stayId, cycleStartStr, cycleEndStr
                        )

                        if (payments.isEmpty()) {
                            val person = Person(
                                id = resident.personId,
                                name = resident.name,
                                phone = resident.phone,
                                profilePhotoPath = resident.profilePhotoPath,
                                govtIdNumber = "",
                                emergencyContact = "",
                                dateOfBirth = "",
                                address = "",
                                gender = "",
                                guardianName = "",
                                govtIdPhotoPath = "",
                                govtIdBackPath = ""
                            )
                            result.add(
                                ResidentWithRoom(
                                    person = person,
                                    roomNumber = resident.roomNumber,
                                    monthlyRent = resident.monthlyRent
                                )
                            )
                        }
                    }

                    emit(result.sortedBy { it.person.name })
                }
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setHostelId(id: String) {
        _hostelId.value = id
    }

    // UPDATED: accepts both paymentDate and recordedDate
    suspend fun recordPayment(
        personId: String,
        amountPaise: Long,
        mode: String,
        paymentDate: String,
        recordedDate: String,
        note: String
    ): Boolean {
        if (personId.isEmpty() || amountPaise <= 0 || paymentDate.isEmpty() || recordedDate.isEmpty()) return false

        val stay = stayDao.getStaysByPerson(personId).stateIn(viewModelScope).value.firstOrNull()
        if (stay == null) return false

        paymentDao.insertPayment(
            Payment(
                stayId = stay.id,
                personId = personId,
                hostelId = stay.hostelId,
                amountPaise = amountPaise,
                mode = mode,
                paymentDate = paymentDate,
                date = recordedDate,
                note = note
            )
        )
        return true
    }

    private fun calculateCycleStart(referenceDate: LocalDate, anchorDay: Int): LocalDate {
        var cycleStart = referenceDate.withDayOfMonth(
            anchorDay.coerceAtMost(referenceDate.lengthOfMonth())
        )
        if (cycleStart.isAfter(referenceDate)) {
            val prevMonth = referenceDate.minusMonths(1)
            cycleStart = prevMonth.withDayOfMonth(
                anchorDay.coerceAtMost(prevMonth.lengthOfMonth())
            )
        }
        return cycleStart
    }
}