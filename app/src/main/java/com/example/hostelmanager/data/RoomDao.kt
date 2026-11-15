package com.example.hostelmanager.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface RoomDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoom(room: RoomEntity)

    @Update
    suspend fun updateRoom(room: RoomEntity)

    // THE SIMPLE FIX: Let SQL count the active stays for each room
    @Query("""
        SELECT rooms.*, 
        (SELECT COUNT(*) FROM stays WHERE stays.roomId = rooms.id AND stays.isActive = 1) AS occupiedBeds
        FROM rooms 
        WHERE rooms.hostelId = :hostelId AND rooms.isArchived = 0
    """)
    fun getRoomsWithOccupancy(hostelId: String): Flow<List<RoomWithOccupancy>>

    @Query("SELECT * FROM rooms WHERE isArchived = 0 ORDER BY roomNumber ASC")
    fun getAllActiveRooms(): Flow<List<RoomEntity>>
}