package com.example.hostelmanager.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "hostels")
data class Hostel(
    @PrimaryKey
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val address: String = "",
    val phone: String = "",
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)