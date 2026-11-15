package com.example.hostelmanager.data

import androidx.room.Embedded

data class ResidentWithRoom(
    @Embedded val person: Person,
    val roomNumber: String
)