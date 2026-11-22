package com.example.hostelmanager.data

import androidx.room.ColumnInfo
import androidx.room.Embedded

data class PaymentWithDetails(
    @Embedded
    val payment: Payment,
    @ColumnInfo(name = "personName")
    val personName: String,
    @ColumnInfo(name = "roomNumber")
    val roomNumber: String,
    @ColumnInfo(name = "monthlyRent")
    val monthlyRent: Long
)