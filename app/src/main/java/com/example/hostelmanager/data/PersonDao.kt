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

    @Query("SELECT * FROM persons WHERE id = :id")
    fun getPersonByIdFlow(id: String): Flow<Person?>

    @Query("""
        SELECT p.id, p.name, p.phone, p.govtIdNumber, p.emergencyContact, p.dateOfBirth,
               p.address, p.gender, p.guardianName, p.govtIdPhotoPath, p.govtIdBackPath, p.profilePhotoPath,
               p.createdAt, p.updatedAt, r.roomNumber, s.monthlyRent
        FROM persons p
        INNER JOIN stays s ON p.id = s.personId
        INNER JOIN rooms r ON s.roomId = r.id
        WHERE s.hostelId = :hostelId AND s.isActive = 1
        ORDER BY p.name ASC
    """)
    fun getActiveResidentsByHostel(hostelId: String): Flow<List<ResidentWithRoom>>

    @Query("""
        SELECT p.id, p.name, p.phone, p.govtIdNumber, p.emergencyContact, p.dateOfBirth,
               p.address, p.gender, p.guardianName, p.govtIdPhotoPath, p.govtIdBackPath, p.profilePhotoPath,
               p.createdAt, p.updatedAt, r.roomNumber, s.monthlyRent
        FROM persons p
        INNER JOIN stays s ON p.id = s.personId
        INNER JOIN rooms r ON s.roomId = r.id
        WHERE s.roomId = :roomId AND s.isActive = 1
        ORDER BY p.name ASC
    """)
    fun getActiveResidentsByRoom(roomId: String): Flow<List<ResidentWithRoom>>

    // People who have checked out (no active stay in this hostel)
    @Query("""
        SELECT p.id, p.name, p.phone, p.govtIdNumber, p.emergencyContact, p.dateOfBirth,
               p.address, p.gender, p.guardianName, p.govtIdPhotoPath, p.govtIdBackPath, p.profilePhotoPath,
               p.createdAt, p.updatedAt,
               (SELECT r.roomNumber FROM stays s2 INNER JOIN rooms r ON s2.roomId = r.id
                WHERE s2.personId = p.id AND s2.hostelId = :hostelId
                ORDER BY s2.joinDate DESC LIMIT 1) AS roomNumber,
               (SELECT s3.monthlyRent FROM stays s3
                WHERE s3.personId = p.id AND s3.hostelId = :hostelId
                ORDER BY s3.joinDate DESC LIMIT 1) AS monthlyRent
        FROM persons p
        WHERE p.id IN (
            SELECT personId FROM stays WHERE hostelId = :hostelId
        )
        AND p.id NOT IN (
            SELECT personId FROM stays WHERE hostelId = :hostelId AND isActive = 1
        )
        ORDER BY p.name ASC
    """)
    fun getPastResidentsByHostel(hostelId: String): Flow<List<ResidentWithRoom>>

    @Query("SELECT COUNT(*) FROM persons p INNER JOIN stays s ON p.id = s.personId WHERE s.isActive = 1 AND p.phone = :phone")
    suspend fun isPhoneDuplicate(phone: String): Boolean

    @Query("SELECT COUNT(*) FROM persons p INNER JOIN stays s ON p.id = s.personId WHERE s.isActive = 1 AND p.govtIdNumber = :govtId")
    suspend fun isGovtIdDuplicate(govtId: String): Boolean

    @Query("SELECT COUNT(*) FROM persons p INNER JOIN stays s ON p.id = s.personId WHERE s.isActive = 1 AND p.emergencyContact = :emergency")
    suspend fun isEmergencyDuplicate(emergency: String): Boolean

    @Query("SELECT COUNT(*) FROM persons p INNER JOIN stays s ON p.id = s.personId WHERE s.isActive = 1 AND p.phone = :phone AND p.id != :excludePersonId")
    suspend fun isPhoneDuplicateExcluding(phone: String, excludePersonId: String): Boolean

    @Query("SELECT COUNT(*) FROM persons p INNER JOIN stays s ON p.id = s.personId WHERE s.isActive = 1 AND p.govtIdNumber = :govtId AND p.id != :excludePersonId")
    suspend fun isGovtIdDuplicateExcluding(govtId: String, excludePersonId: String): Boolean

    @Query("SELECT COUNT(*) FROM persons p INNER JOIN stays s ON p.id = s.personId WHERE s.isActive = 1 AND p.emergencyContact = :emergency AND p.id != :excludePersonId")
    suspend fun isEmergencyDuplicateExcluding(emergency: String, excludePersonId: String): Boolean

    // All residents (active + checked out) with their most recent stay in this hostel
    @Query("""
        SELECT p.id AS personId, p.name, p.phone, p.profilePhotoPath,
               r.roomNumber, s.monthlyRent, s.id AS stayId,
               s.isActive, s.checkoutDate, s.billingAnchorDay
        FROM persons p
        INNER JOIN stays s ON p.id = s.personId
        INNER JOIN rooms r ON s.roomId = r.id
        WHERE s.hostelId = :hostelId
          AND s.id = (
              SELECT s2.id FROM stays s2
              WHERE s2.personId = p.id AND s2.hostelId = :hostelId
              ORDER BY s2.joinDate DESC LIMIT 1
          )
        ORDER BY p.name ASC
    """)
    fun getAllResidentsWithLatestStay(hostelId: String): Flow<List<ResidentWithStayInfo>>

    // Active resident names per room (for the room picker sharing view)
    @Query("""
        SELECT s.roomId AS roomId, p.name AS name
        FROM persons p
        INNER JOIN stays s ON p.id = s.personId
        WHERE s.hostelId = :hostelId AND s.isActive = 1
        ORDER BY p.name ASC
    """)
    fun getActiveResidentNamesByRoom(hostelId: String): Flow<List<RoomOccupant>>
}

data class RoomOccupant(val roomId: String, val name: String)