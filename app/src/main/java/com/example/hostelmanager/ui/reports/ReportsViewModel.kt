package com.example.hostelmanager.ui.reports

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

data class ExpenseBreakdown(
    val category: String,
    val totalPaise: Long
)

data class MonthlyReport(
    val totalIncomePaise: Long,
    val totalExpensePaise: Long,
    val netProfitPaise: Long,
    val expenseBreakdown: List<ExpenseBreakdown>
)

class ReportsViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val paymentDao = db.paymentDao()
    private val expenseDao = db.expenseDao()

    private val _hostelId = MutableStateFlow("")
    private val _selectedMonth = MutableStateFlow(
        java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM"))
    )

    val selectedMonth: StateFlow<String> = _selectedMonth

    val report: StateFlow<MonthlyReport> = combine(
        _hostelId,
        _selectedMonth
    ) { hostelId, month ->
        Pair(hostelId, month)
    }.flatMapLatest { (hostelId, month) ->
        if (hostelId.isEmpty()) {
            flowOf(MonthlyReport(0, 0, 0, emptyList()))
        } else {
            combine(
                paymentDao.getAllPaymentsForHostel(hostelId),
                expenseDao.getAllExpensesForHostel(hostelId)
            ) { payments, expenses ->
                // Filter payments for the selected month
                val monthPayments = payments.filter { it.paymentDate.startsWith(month) }
                val totalIncome = monthPayments.sumOf { it.amountPaise }

                // Filter expenses for the selected month
                val monthExpenses = expenses.filter { it.expenseDate.startsWith(month) }
                val totalExpense = monthExpenses.sumOf { it.amountPaise }

                // Group expenses by category
                val breakdown = monthExpenses
                    .groupBy { it.category }
                    .map { (category, items) ->
                        ExpenseBreakdown(category, items.sumOf { it.amountPaise })
                    }
                    .sortedByDescending { it.totalPaise }

                MonthlyReport(
                    totalIncomePaise = totalIncome,
                    totalExpensePaise = totalExpense,
                    netProfitPaise = totalIncome - totalExpense,
                    expenseBreakdown = breakdown
                )
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MonthlyReport(0, 0, 0, emptyList()))

    fun setHostelId(id: String) {
        _hostelId.value = id
    }

    fun setMonth(month: String) {
        _selectedMonth.value = month
    }

    fun nextMonth() {
        val current = java.time.YearMonth.parse(_selectedMonth.value)
        _selectedMonth.value = current.plusMonths(1)
            .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM"))
    }

    fun previousMonth() {
        val current = java.time.YearMonth.parse(_selectedMonth.value)
        _selectedMonth.value = current.minusMonths(1)
            .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM"))
    }
}