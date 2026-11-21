package com.example.hostelmanager.ui.payments

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.hostelmanager.data.AppDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class YetToPayResident(
    val personId: String,
    val name: String,
    val phone: String,
    val profilePhotoPath: String,
    val roomNumber: String,
    val monthlyRent: Long,
    val cycleStart: String,
    val cycleEnd: String,
    val isCheckedOut: Boolean = false
)

class PaymentsViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val personDao = db.personDao()
    private val paymentDao = db.paymentDao()

    private val _hostelId = MutableStateFlow("")

    // FIXED: reacts to BOTH resident changes AND payment changes
    val residents: StateFlow<List<YetToPayResident>> = _hostelId.flatMapLatest { hostelId ->
        if (hostelId.isEmpty()) {
            flowOf(emptyList())
        } else {
            // combine() emits whenever EITHER flow emits
            combine(
                personDao.getAllResidentsWithLatestStay(hostelId),
                paymentDao.getAllPaymentsForHostel(hostelId)
            ) { residents, allPayments ->
                val today = LocalDate.now()
                val result = mutableListOf<YetToPayResident>()

                for (resident in residents) {
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

                    // Check in-memory (no DB query per resident)
                    val hasPaymentInCycle = allPayments.any { p ->
                        p.stayId == resident.stayId &&
                                p.paymentDate >= cycleStartStr &&
                                p.paymentDate <= cycleEndStr
                    }

                    if (!hasPaymentInCycle) {
                        result.add(
                            YetToPayResident(
                                personId = resident.personId,
                                name = resident.name,
                                phone = resident.phone,
                                profilePhotoPath = resident.profilePhotoPath,
                                roomNumber = resident.roomNumber,
                                monthlyRent = resident.monthlyRent,
                                cycleStart = cycleStartStr,
                                cycleEnd = cycleEndStr,
                                isCheckedOut = !resident.isActive
                            )
                        )
                    }
                }

                result.sortedBy { it.name }
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setHostelId(id: String) {
        _hostelId.value = id
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