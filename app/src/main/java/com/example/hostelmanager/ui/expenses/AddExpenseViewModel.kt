package com.example.hostelmanager.ui.expenses

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.hostelmanager.data.AppDatabase
import com.example.hostelmanager.data.Expense
import kotlinx.coroutines.launch

class AddExpenseViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val expenseDao = db.expenseDao()

    suspend fun addExpense(
        hostelId: String,
        expenseDate: String,
        enteredDate: String,
        category: String,
        amountPaise: Long,
        note: String,
        receiptImagePath: String?
    ): Boolean {
        if (hostelId.isEmpty() || expenseDate.isEmpty() || enteredDate.isEmpty() ||
            category.isEmpty() || amountPaise <= 0) return false

        expenseDao.insertExpense(
            Expense(
                hostelId = hostelId,
                expenseDate = expenseDate,
                enteredDate = enteredDate,
                category = category,
                amountPaise = amountPaise,
                note = note,
                receiptImagePath = receiptImagePath
            )
        )
        return true
    }
}