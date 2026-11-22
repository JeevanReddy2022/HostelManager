package com.example.hostelmanager.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: Expense)

    @Query("DELETE FROM expenses WHERE id = :id")
    suspend fun deleteExpense(id: String)

    @Query("SELECT * FROM expenses WHERE hostelId = :hostelId ORDER BY expenseDate DESC")
    fun getAllExpensesForHostel(hostelId: String): Flow<List<Expense>>

    @Query("SELECT * FROM expenses WHERE hostelId = :hostelId AND category = :category ORDER BY expenseDate DESC")
    fun getExpensesByCategory(hostelId: String, category: String): Flow<List<Expense>>

    @Query("SELECT * FROM expenses WHERE hostelId = :hostelId AND expenseDate BETWEEN :startDate AND :endDate ORDER BY expenseDate DESC")
    fun getExpensesBetween(hostelId: String, startDate: String, endDate: String): Flow<List<Expense>>

    @Query("SELECT COALESCE(SUM(amountPaise), 0) FROM expenses WHERE hostelId = :hostelId")
    fun getTotalExpensesForHostel(hostelId: String): Flow<Long>
    @Query("""
    SELECT COALESCE(SUM(amountPaise), 0) FROM expenses 
    WHERE hostelId = :hostelId 
    AND expenseDate = :date
""")
    fun getTotalExpensesForDate(hostelId: String, date: String): Flow<Long>

    @Query("""
    SELECT COALESCE(SUM(amountPaise), 0) FROM expenses 
    WHERE hostelId = :hostelId 
    AND expenseDate LIKE :monthPattern
""")
    fun getTotalExpensesForMonth(hostelId: String, monthPattern: String): Flow<Long>

    @Query("""
    SELECT COALESCE(SUM(amountPaise), 0) FROM expenses 
    WHERE hostelId = :hostelId 
    AND expenseDate BETWEEN :startDate AND :endDate
""")
    fun getTotalExpensesBetween(hostelId: String, startDate: String, endDate: String): Flow<Long>
}