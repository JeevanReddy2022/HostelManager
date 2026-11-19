package com.example.hostelmanager.data

import androidx.room.Dao
import androidx.room.Delete
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

    @Delete
    suspend fun deleteRoom(room: RoomEntity)

    @Query("""
        SELECT rooms.*, 
        (SELECT COUNT(*) FROM stays WHERE stays.roomId = rooms.id AND stays.isActive = 1) AS occupiedBeds
        FROM rooms 
        WHERE rooms.hostelId = :hostelId AND rooms.isArchived = 0
    """)
    fun getRoomsWithOccupancy(hostelId: String): Flow<List<RoomWithOccupancy>>

    @Query("SELECT * FROM rooms WHERE isArchived = 0 ORDER BY roomNumber ASC")
    fun getAllActiveRooms(): Flow<List<RoomEntity>>

    @Query("SELECT * FROM rooms WHERE id = :id")
    fun getRoomById(id: String): Flow<RoomEntity?>

    @Query("SELECT * FROM rooms WHERE hostelId = :hostelId AND isArchived = 1 ORDER BY roomNumber ASC")
    fun getArchivedRooms(hostelId: String): Flow<List<RoomEntity>>

    // NEW: counts rooms with this number in this hostel (active + archived)
    @Query("SELECT COUNT(*) FROM rooms WHERE hostelId = :hostelId AND roomNumber = :roomNumber")
    suspend fun countRoomsWithNumber(hostelId: String, roomNumber: String): Int
}