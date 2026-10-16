package com.example.pw_02_8.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.pw_02_8.data.entity.Measurement

@Dao
interface MeasurementDao {
    @Insert
    suspend fun insertMeasurement(measurement: Measurement)

    @Update
    suspend fun updateMeasurement(measurement: Measurement)

    @Delete
    suspend fun deleteMeasurement(measurement: Measurement)

    @Query("SELECT * FROM measurements WHERE reactorId = :reactorId")
    suspend fun getMeasurementsForReactor(reactorId: Int): List<Measurement>
}