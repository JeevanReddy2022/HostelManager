package com.example.hostelmanager.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PersonDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPerson(person: Person)

    @Update
    suspend fun updatePerson(person: Person)

    @Query("SELECT * FROM persons ORDER BY name ASC")
    fun getAllPersons(): Flow<List<Person>>

    @Query("SELECT * FROM persons WHERE id = :id")
    suspend fun getPersonById(id: String): Person?

    @Query("""
        SELECT p.id, p.name, p.phone, p.govtIdNumber, p.emergencyContact, p.dateOfBirth, 
               p.address, p.gender, p.guardianName, p.govtIdPhotoPath, p.profilePhotoPath, 
               p.createdAt, p.updatedAt, r.roomNumber 
        FROM persons p 
        INNER JOIN stays s ON p.id = s.personId 
        INNER JOIN rooms r ON s.roomId = r.id 
        WHERE s.hostelId = :hostelId AND s.isActive = 1 
        ORDER BY p.name ASC
    """)
    fun getActiveResidentsByHostel(hostelId: String): Flow<List<ResidentWithRoom>>

    // NEW: Three separate checks to see exactly which field is a duplicate
    @Query("SELECT COUNT(*) FROM persons p INNER JOIN stays s ON p.id = s.personId WHERE s.isActive = 1 AND p.phone = :phone")
    suspend fun isPhoneDuplicate(phone: String): Boolean

    @Query("SELECT COUNT(*) FROM persons p INNER JOIN stays s ON p.id = s.personId WHERE s.isActive = 1 AND p.govtIdNumber = :govtId")
    suspend fun isGovtIdDuplicate(govtId: String): Boolean

    @Query("SELECT COUNT(*) FROM persons p INNER JOIN stays s ON p.id = s.personId WHERE s.isActive = 1 AND p.emergencyContact = :emergency")
    suspend fun isEmergencyDuplicate(emergency: String): Boolean
}