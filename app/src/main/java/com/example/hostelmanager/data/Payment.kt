package com.example.hostelmanager.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "payments",
    foreignKeys = [
        ForeignKey(
            entity = Stay::class,
            parentColumns = ["id"],
            childColumns = ["stayId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("stayId"), Index("date")]
)
data class Payment(
    @PrimaryKey
    val id: String = java.util.UUID.randomUUID().toString(),
    val stayId: String,
    val personId: String,
    val hostelId: String,
    val amountPaise: Long,
    val mode: String,
    val paymentDate: String,   // NEW: when money was actually received
    val date: String,          // Now semantically = when payment was recorded in app
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis()
)