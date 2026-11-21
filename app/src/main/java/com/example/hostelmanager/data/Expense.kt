package com.example.hostelmanager.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "expenses",
    foreignKeys = [
        ForeignKey(
            entity = Hostel::class,
            parentColumns = ["id"],
            childColumns = ["hostelId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("hostelId"), Index("expenseDate")]
)
data class Expense(
    @PrimaryKey
    val id: String = java.util.UUID.randomUUID().toString(),
    val hostelId: String,
    val expenseDate: String,      // When the expense actually occurred
    val enteredDate: String,      // When it was recorded in the app
    val category: String,         // "Electricity", "Water", "Maintenance", etc.
    val amountPaise: Long,
    val note: String = "",
    val receiptImagePath: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)