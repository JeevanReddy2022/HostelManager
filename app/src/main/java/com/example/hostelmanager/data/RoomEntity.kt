package com.example.hostelmanager.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "rooms",
    foreignKeys = [
        ForeignKey(
            entity = Hostel::class,
            parentColumns = ["id"],
            childColumns = ["hostelId"],
            onDelete = ForeignKey.CASCADE // If a hostel is deleted, its rooms are deleted too
        )
    ],
    indices = [Index("hostelId")]
)
data class RoomEntity(
    @PrimaryKey
    val id: String = java.util.UUID.randomUUID().toString(),
    val hostelId: String, // Links this room to a specific hostel
    val roomNumber: String,
    val floor: Int = 0,
    val bedCount: Int = 1,
    val defaultRentPerBed: Long = 0, // Stored in paise (e.g., 800000 for ₹8,000)
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)