package com.example.pw_02_8.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.example.pw_02_8.data.entity.Measurement
import com.example.pw_02_8.data.entity.Reactor
import com.example.pw_02_8.data.relation.ReactorSummary
import com.example.pw_02_8.data.relation.ReactorWithMeasurements
import kotlinx.coroutines.flow.Flow

@Dao
abstract class ReactorDao {
    @Insert
    abstract suspend fun insertReactor(reactor: Reactor): Long

    @Insert
    abstract suspend fun insertFirstMeasurement(measurement: Measurement)

    @Transaction
    open suspend fun insertReactorWithMeasurement(reactor: Reactor, measurement: Measurement) {
        val newId = insertReactor(reactor)
        insertFirstMeasurement(measurement.copy(reactorId = newId.toInt()))
    }

    @Query("DELETE FROM reactors WHERE id = :reactorId")
    abstract suspend fun deleteReactor(reactorId: Int)

    @Query(
        """
        SELECT reactors.id AS reactorId,
               reactors.name AS reactorName,
               COUNT(measurements.id) AS measurementCount,
               AVG(measurements.temperature) AS avgTemperature,
               AVG(measurements.ph) AS avgPh,
               SUM(measurements.gasFlow) AS totalGasFlow
        FROM reactors
        LEFT JOIN measurements ON measurements.reactorId = reactors.id
        WHERE reactors.name LIKE '%' || :search || '%'
        GROUP BY reactors.id
        ORDER BY reactors.name
        """
    )
    abstract fun searchReactorSummaries(search: String): Flow<List<ReactorSummary>>

    @Query(
        """
        SELECT reactors.id AS reactorId,
               reactors.name AS reactorName,
               COUNT(measurements.id) AS measurementCount,
               AVG(measurements.temperature) AS avgTemperature,
               AVG(measurements.ph) AS avgPh,
               SUM(measurements.gasFlow) AS totalGasFlow
        FROM reactors
        LEFT JOIN measurements ON measurements.reactorId = reactors.id
        WHERE reactors.id = :reactorId
        GROUP BY reactors.id
        """
    )
    abstract suspend fun getReactorSummary(reactorId: Int): ReactorSummary?

    @Transaction
    @Query("SELECT * FROM reactors WHERE id = :reactorId")
    abstract fun getReactorWithMeasurements(reactorId: Int): Flow<ReactorWithMeasurements?>
}