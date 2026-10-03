package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.CalibrationProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface CalibrationDao {
    @Query("SELECT * FROM calibration_profiles")
    fun getAllProfiles(): Flow<List<CalibrationProfile>>

    @Query("SELECT * FROM calibration_profiles WHERE id = :id")
    suspend fun getProfileById(id: String): CalibrationProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfiles(profiles: List<CalibrationProfile>)

    @Update
    suspend fun updateProfile(profile: CalibrationProfile)

    @Query("DELETE FROM calibration_profiles WHERE id = :id")
    suspend fun resetProfile(id: String)
}
