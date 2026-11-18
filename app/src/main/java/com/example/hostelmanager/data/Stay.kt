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

    // NEW: this resident's own agreed monthly rent, in paise
    @ColumnInfo(defaultValue = "0")
    val monthlyRent: Long = 0,

    val openingBalance: Long = 0,
    val depositCollected: Long = 0,
    val depositRefunded: Long = 0,
    val depositRefundDate: String? = null,
    val depositRefundMode: String? = null,
    val isActive: Boolean = true,
    val returnOverlapDecision: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)