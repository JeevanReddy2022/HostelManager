package com.example.hostelmanager.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface StayDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStay(stay: Stay)

    @Update
    suspend fun updateStay(stay: Stay)

    // Get all stays for a specific person (to show their history)
    @Query("SELECT * FROM stays WHERE personId = :personId ORDER BY joinDate DESC")
    fun getStaysByPerson(personId: String): Flow<List<Stay>>

    // Get all currently active stays in a specific hostel
    @Query("SELECT * FROM stays WHERE hostelId = :hostelId AND isActive = 1 ORDER BY joinDate DESC")
    fun getActiveStaysByHostel(hostelId: String): Flow<List<Stay>>

    @Query("SELECT * FROM stays WHERE id = :id")
    suspend fun getStayById(id: String): Stay?
}