package com.example.hostelmanager.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface HostelDao {

    // 1. Save a new hostel
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHostel(hostel: Hostel)

    // 2. Update an existing hostel (e.g., changing the name or phone)
    @Update
    suspend fun updateHostel(hostel: Hostel)

    // 3. Hard delete (we rarely use this in our app, but good to have for mistakes)
    @Delete
    suspend fun deleteHostel(hostel: Hostel)

    // 4. Get all active (not archived) hostels, sorted alphabetically
    @Query("SELECT * FROM hostels WHERE isArchived = 0 ORDER BY name ASC")
    fun getAllActiveHostels(): Flow<List<Hostel>>

    // 5. Get all hostels, including archived ones (for the Archive screen)
    @Query("SELECT * FROM hostels ORDER BY name ASC")
    fun getAllHostels(): Flow<List<Hostel>>
}