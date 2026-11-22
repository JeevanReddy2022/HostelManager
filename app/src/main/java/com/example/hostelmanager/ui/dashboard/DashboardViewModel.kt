package com.example.hostelmanager.ui.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.hostelmanager.data.AppDatabase
import com.example.hostelmanager.data.Hostel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

data class DashboardStats(
    val hostelName: String,
    val checkInsThisMonth: Int,
    val paymentsThisMonth: Int,
    val rentCollectedThisMonth: Long,
    val activeResidents: Int,
    val totalBeds: Int,
    val availableBeds: Int,
    val expensesLastMonth: Long,
    val expensesToday: Long,
    val expensesThisMonth: Long,
    val incomeLastMonth: Long,
    val incomeToday: Long,
    val incomeThisMonth: Long,
    val profitLastMonth: Long,
    val profitThisMonth: Long,
    val profitYTD: Long
)

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val hostelDao = db.hostelDao()
    private val stayDao = db.stayDao()
    private val paymentDao = db.paymentDao()
    private val expenseDao = db.expenseDao()
    private val roomDao = db.roomDao()

    private val _hostelId = MutableStateFlow("")

    private val today = LocalDate.now()
    private val currentMonth = YearMonth.now()
    private val lastMonth = currentMonth.minusMonths(1)
    private val yearStart = LocalDate.of(currentMonth.year, 1, 1)

    private val todayStr = today.format(DateTimeFormatter.ISO_LOCAL_DATE)
    private val thisMonthPattern = "${currentMonth.year}-${String.format("%02d", currentMonth.monthValue)}%"
    private val lastMonthPattern = "${lastMonth.year}-${String.format("%02d", lastMonth.monthValue)}%"
    private val yearStartStr = yearStart.format(DateTimeFormatter.ISO_LOCAL_DATE)

    val stats: StateFlow<DashboardStats?> = _hostelId.flatMapLatest { hostelId ->
        if (hostelId.isEmpty()) {
            flowOf(null)
        } else {
            combine(
                hostelDao.getAllHostels(),
                stayDao.getActiveResidentsCount(hostelId),
                stayDao.getCheckInsCount(hostelId, thisMonthPattern),
                paymentDao.getPaymentsCountForMonth(hostelId, thisMonthPattern),
                paymentDao.getTotalRentCollected(hostelId, thisMonthPattern),
                roomDao.getTotalBedsForHostel(hostelId),
                expenseDao.getTotalExpensesForDate(hostelId, todayStr),
                expenseDao.getTotalExpensesForMonth(hostelId, thisMonthPattern),
                expenseDao.getTotalExpensesForMonth(hostelId, lastMonthPattern),
                paymentDao.getTotalPaymentsForDate(hostelId, todayStr),
                paymentDao.getTotalPaymentsBetween(hostelId, "${lastMonth.year}-${String.format("%02d", lastMonth.monthValue)}-01",
                    lastMonth.atEndOfMonth().format(DateTimeFormatter.ISO_LOCAL_DATE)),
                paymentDao.getTotalPaymentsBetween(hostelId, yearStartStr, todayStr),
                expenseDao.getTotalExpensesBetween(hostelId, yearStartStr, todayStr)
            ) { values ->
                val hostels = values[0] as List<Hostel>
                val activeResidents = values[1] as Int
                val checkIns = values[2] as Int
                val paymentsCount = values[3] as Int
                val rentCollected = values[4] as Long
                val totalBeds = values[5] as Int
                val expensesToday = values[6] as Long
                val expensesThisMonth = values[7] as Long
                val expensesLastMonth = values[8] as Long
                val incomeToday = values[9] as Long
                val incomeLastMonth = values[10] as Long
                val incomeYTD = values[11] as Long
                val expensesYTD = values[12] as Long

                val hostel = hostels.find { it.id == hostelId }

                DashboardStats(
                    hostelName = hostel?.name ?: "Unknown",
                    checkInsThisMonth = checkIns,
                    paymentsThisMonth = paymentsCount,
                    rentCollectedThisMonth = rentCollected,
                    activeResidents = activeResidents,
                    totalBeds = totalBeds,
                    availableBeds = totalBeds - activeResidents,
                    expensesLastMonth = expensesLastMonth,
                    expensesToday = expensesToday,
                    expensesThisMonth = expensesThisMonth,
                    incomeLastMonth = incomeLastMonth,
                    incomeToday = incomeToday,
                    incomeThisMonth = rentCollected,
                    profitLastMonth = incomeLastMonth - expensesLastMonth,
                    profitThisMonth = rentCollected - expensesThisMonth,
                    profitYTD = incomeYTD - expensesYTD
                )
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun setHostelId(id: String) {
        _hostelId.value = id
    }
}