package com.example.hostelmanager.data

import androidx.room.Embedded

data class RoomWithOccupancy(
    @Embedded val room: RoomEntity,
    val occupiedBeds: Int
)