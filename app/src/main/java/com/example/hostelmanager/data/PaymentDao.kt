package com.example.hostelmanager.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: Payment)

    @Query("DELETE FROM payments WHERE id = :id")
    suspend fun deletePayment(id: String)

    @Query("SELECT * FROM payments WHERE stayId = :stayId ORDER BY paymentDate DESC")
    fun getPaymentsForStay(stayId: String): Flow<List<Payment>>

    @Query("SELECT * FROM payments WHERE personId = :personId ORDER BY paymentDate DESC")
    fun getPaymentsForPerson(personId: String): Flow<List<Payment>>

    @Query("SELECT * FROM payments WHERE hostelId = :hostelId ORDER BY paymentDate DESC")
    fun getAllPaymentsForHostel(hostelId: String): Flow<List<Payment>>

    @Query("SELECT * FROM payments WHERE paymentDate BETWEEN :startDate AND :endDate ORDER BY paymentDate DESC")
    fun getPaymentsBetween(startDate: String, endDate: String): Flow<List<Payment>>

    @Query("""
        SELECT COALESCE(SUM(amountPaise), 0) FROM payments WHERE hostelId = :hostelId
    """)
    fun getTotalPaymentsForHostel(hostelId: String): Flow<Long>

    @Query("""
        SELECT COUNT(*) FROM payments 
        WHERE hostelId = :hostelId 
        AND paymentDate LIKE :monthPattern
    """)
    fun getPaymentsCountForMonth(hostelId: String, monthPattern: String): Flow<Int>

    @Query("""
        SELECT COALESCE(SUM(amountPaise), 0) FROM payments 
        WHERE hostelId = :hostelId 
        AND paymentDate LIKE :monthPattern
    """)
    fun getTotalRentCollected(hostelId: String, monthPattern: String): Flow<Long>

    @Query("""
        SELECT COALESCE(SUM(amountPaise), 0) FROM payments 
        WHERE hostelId = :hostelId 
        AND paymentDate = :date
    """)
    fun getTotalPaymentsForDate(hostelId: String, date: String): Flow<Long>

    @Query("""
        SELECT COALESCE(SUM(amountPaise), 0) FROM payments 
        WHERE hostelId = :hostelId 
        AND paymentDate BETWEEN :startDate AND :endDate
    """)
    fun getTotalPaymentsBetween(hostelId: String, startDate: String, endDate: String): Flow<Long>

    @Query("""
        SELECT p.*, pers.name as personName, r.roomNumber as roomNumber, s.monthlyRent as monthlyRent
        FROM payments p
        INNER JOIN persons pers ON p.personId = pers.id
        INNER JOIN stays s ON p.stayId = s.id
        INNER JOIN rooms r ON s.roomId = r.id
        WHERE p.hostelId = :hostelId
        ORDER BY p.paymentDate DESC
    """)
    fun getAllPaymentsWithDetailsForHostel(hostelId: String): Flow<List<PaymentWithDetails>>

    @Query("""
        SELECT * FROM payments 
        WHERE stayId = :stayId 
          AND paymentDate >= :cycleStart 
          AND paymentDate <= :cycleEnd
        ORDER BY paymentDate DESC
    """)
    suspend fun getPaymentsInCycle(stayId: String, cycleStart: String, cycleEnd: String): List<Payment>
}