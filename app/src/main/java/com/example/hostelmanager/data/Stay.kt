package com.example.hostelmanager.data

import androidx.room.ColumnInfo
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
    val joinDate: String,
    val checkoutDate: String? = null,
    @ColumnInfo(defaultValue = "0")
    val monthlyRent: Long = 0,
    val openingBalance: Long = 0,
    val depositCollected: Long = 0,
    val depositRefunded: Long = 0,
    val depositRefundDate: String? = null,
    val depositRefundMode: String? = null,
    val isActive: Boolean = true,
    val returnOverlapDecision: String? = null,
    // NEW: Day of month (1-31) that starts this resident's billing cycle.
    // Set once when they join, carried forward across room transfers.
    @ColumnInfo(defaultValue = "1")
    val billingAnchorDay: Int = 1,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)