package com.example.hostelmanager.ui.expenses

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.hostelmanager.data.AppDatabase
import com.example.hostelmanager.data.Expense
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

/* ------------------------------------------------------------------ */
/*  Search / filter / sort model                                       */
/* ------------------------------------------------------------------ */

enum class ExpenseSort(val label: String, val short: String, val caption: String) {
    DATE_DESC("Date · Newest first", "Date ↓", "Most recent expense date on top"),
    DATE_ASC("Date · Oldest first", "Date ↑", "Earliest expense date on top"),
    AMOUNT_DESC("Amount · High to Low", "₹ ↓", "Largest expense first"),
    AMOUNT_ASC("Amount · Low to High", "₹ ↑", "Smallest expense first"),
    CATEGORY_ASC("Category · A to Z", "A–Z", "Alphabetical by category"),
    ENTERED_DESC("Recently added", "New", "Latest entries you recorded")
}

data class ExpenseFilters(
    val query: String = "",
    val month: String? = null,        // "yyyy-MM", null = all months
    val category: String? = null,     // null = all categories
    val sort: ExpenseSort = ExpenseSort.DATE_DESC
) {
    val activeCount: Int
        get() = (if (query.isNotBlank()) 1 else 0) +
                (if (month != null) 1 else 0) +
                (if (category != null) 1 else 0)

    val isFiltered: Boolean get() = activeCount > 0
}

internal fun monthKeyOf(date: String): String =
    if (date.length >= 7) date.substring(0, 7) else ""

internal fun formatExpenseMonth(yyyyMm: String): String {
    val parsed = runCatching { YearMonth.parse(yyyyMm) }.getOrNull() ?: return yyyyMm
    val name = parsed.month.getDisplayName(TextStyle.FULL, Locale.getDefault())
    return "$name ${parsed.year}"
}

internal fun filterAndSortExpenses(
    expenses: List<Expense>,
    f: ExpenseFilters
): List<Expense> {
    val q = f.query.trim()

    val filtered = expenses.filter { e ->
        val matchesQuery = q.isEmpty() ||
                e.note.contains(q, ignoreCase = true) ||
                e.category.contains(q, ignoreCase = true)
        val matchesMonth = f.month == null || e.expenseDate.startsWith(f.month)
        val matchesCategory = f.category == null || e.category == f.category
        matchesQuery && matchesMonth && matchesCategory
    }

    val byDateDesc = compareByDescending<Expense>({ it.expenseDate })
        .thenByDescending { it.createdAt }

    val comparator: Comparator<Expense> = when (f.sort) {
        ExpenseSort.DATE_DESC -> byDateDesc
        ExpenseSort.DATE_ASC -> compareBy<Expense>({ it.expenseDate }).thenBy { it.createdAt }
        ExpenseSort.AMOUNT_DESC -> compareByDescending<Expense>({ it.amountPaise }).then(byDateDesc)
        ExpenseSort.AMOUNT_ASC -> compareBy<Expense>({ it.amountPaise }).then(byDateDesc)
        ExpenseSort.CATEGORY_ASC -> compareBy<Expense>({ it.category.lowercase() }).then(byDateDesc)
        ExpenseSort.ENTERED_DESC -> compareByDescending<Expense>({ it.createdAt }).then(byDateDesc)
    }

    return filtered.sortedWith(comparator)
}

/* ------------------------------------------------------------------ */
/*  ViewModel                                                          */
/* ------------------------------------------------------------------ */

class ExpensesViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val expenseDao = db.expenseDao()

    private val _hostelId = MutableStateFlow("")

    val expenses: StateFlow<List<Expense>> = _hostelId.flatMapLatest { id ->
        if (id.isEmpty()) flowOf(emptyList())
        else expenseDao.getAllExpensesForHostel(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Lives in the ViewModel so search + filters survive tab switches and rotation.
    private val _filters = MutableStateFlow(ExpenseFilters())
    val filters: StateFlow<ExpenseFilters> = _filters

    fun setHostelId(id: String) {
        if (_hostelId.value != id) {
            _hostelId.value = id
            _filters.value = ExpenseFilters()
        }
    }

    fun setQuery(value: String) {
        _filters.value = _filters.value.copy(query = value.take(60))
    }

    fun setMonth(month: String?) {
        _filters.value = _filters.value.copy(month = month)
    }

    fun setCategory(category: String?) {
        _filters.value = _filters.value.copy(category = category)
    }

    fun setSort(value: ExpenseSort) {
        _filters.value = _filters.value.copy(sort = value)
    }

    /** Resets search + filters, keeps the chosen sort order. */
    fun clearFilters() {
        _filters.value = _filters.value.copy(query = "", month = null, category = null)
    }

    fun deleteExpense(expenseId: String) {
        viewModelScope.launch {
            expenseDao.deleteExpense(expenseId)
        }
    }

    fun updateExpense(
        id: String,
        hostelId: String,
        expenseDate: String,
        enteredDate: String,
        category: String,
        amountPaise: Long,
        note: String,
        receiptImagePath: String?
    ) {
        viewModelScope.launch {
            // Keep the original createdAt so "Recently added" stays truthful after an edit.
            val original = expenses.value.firstOrNull { it.id == id }
            expenseDao.insertExpense(
                Expense(
                    id = id,
                    hostelId = hostelId,
                    expenseDate = expenseDate,
                    enteredDate = enteredDate,
                    category = category,
                    amountPaise = amountPaise,
                    note = note,
                    receiptImagePath = receiptImagePath,
                    createdAt = original?.createdAt ?: System.currentTimeMillis()
                )
            )
        }
    }
}