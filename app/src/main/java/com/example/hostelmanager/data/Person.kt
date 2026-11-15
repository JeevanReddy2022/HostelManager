package com.example.hostelmanager.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "persons")
data class Person(
    @PrimaryKey
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val phone: String,
    val govtIdNumber: String,
    val emergencyContact: String,
    val dateOfBirth: String = "",        // NEW: For ID verification
    val address: String = "",
    val gender: String = "",
    val guardianName: String = "",
    val govtIdPhotoPath: String = "",    // NEW: Will store the photo file location in v2
    val profilePhotoPath: String = "",   // NEW: Will store the photo file location in v2
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)