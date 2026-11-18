package com.example.hostelmanager.data

import androidx.room.ColumnInfo
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
    val dateOfBirth: String,
    val address: String,
    val gender: String,
    val guardianName: String,

    val govtIdPhotoPath: String = "",          // Govt ID — FRONT side image path
    @ColumnInfo(defaultValue = "")
    val govtIdBackPath: String = "",           // Govt ID — BACK side image path (new)
    val profilePhotoPath: String = "",         // Person photo (optional)

    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)