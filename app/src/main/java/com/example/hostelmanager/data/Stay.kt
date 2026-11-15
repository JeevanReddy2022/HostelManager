package com.example.hostelmanager.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "stays",
    foreignKeys = [
        ForeignKey(entity = Person::class, parentColumns = ["id"], childColumns = ["personId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = Hostel::class, parentColumns = ["id"], childColumns = ["hostelId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = RoomEntity::class, parentColumns = ["id"], childColumns = ["roomId"], onDelete = ForeignKey.SET_NULL)
    ],
    indices = [Index("personId"), Index("hostelId"), Index("roomId")]
)
data class Stay(
    @PrimaryKey
    val id: String = java.util.UUID.randomUUID().toString(),
    val personId: String,
    val hostelId: String,
    val roomId: String,
    val joinDate: String, // ISO format: YYYY-MM-DD
    val checkoutDate: String? = null, // null means currently active
    val openingBalance: Long = 0, // in paise
    val depositCollected: Long = 0, // in paise
    val depositRefunded: Long = 0, // in paise
    val depositRefundDate: String? = null,
    val depositRefundMode: String? = null, // "CASH" or "UPI"
    val isActive: Boolean = true,
    val returnOverlapDecision: String? = null, // "CHARGE_FRESH" or "CREDIT_ADDED"
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)