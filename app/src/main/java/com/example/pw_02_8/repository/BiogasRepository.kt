package com.example.pw_02_8.repository

import android.database.sqlite.SQLiteConstraintException
import com.example.pw_02_8.data.dao.MeasurementDao
import com.example.pw_02_8.data.dao.ReactorDao
import com.example.pw_02_8.data.entity.Measurement
import com.example.pw_02_8.data.entity.Reactor
import com.example.pw_02_8.data.relation.ReactorSummary
import com.example.pw_02_8.data.relation.ReactorWithMeasurements
import kotlinx.coroutines.flow.Flow

const val METHANE_ENERGY = 9.97

const val NORMAL_TEMP_MIN = 35.0
const val NORMAL_TEMP_MAX = 40.0
const val NORMAL_PH_MIN = 6.8
const val NORMAL_PH_MAX = 7.5

enum class SortType(val title: String) {
    TIME("Час"),
    TEMPERATURE("Температура"),
    GAS_FLOW("Вихід газу")
}

data class ReactorStats(
    val measurementCount: Int,
    val avgTemperature: Double,
    val avgPh: Double,
    val totalGasFlow: Double,
    val totalEnergy: Double
)

fun checkReactorName(name: String): String? {
    if (name.isBlank()) {
        return "Введіть назву реактора"
    }
    return null
}

fun checkValue(text: String, name: String, min: Double, max: Double): String? {
    if (text.isBlank()) {
        return "Поле \"$name\" не заповнене"
    }
    val value = text.replace(',', '.').toDoubleOrNull()
    if (value == null) {
        return "Поле \"$name\" має містити число"
    }
    if (value < min || value > max) {
        return "Поле \"$name\" має бути від ${min.toInt()} до ${max.toInt()}"
    }
    return null
}

fun checkMeasurement(temperature: String, ph: String, gasFlow: String, methane: String): String? {
    checkValue(temperature, "Температура", 0.0, 80.0)?.let { return it }
    checkValue(ph, "pH", 0.0, 14.0)?.let { return it }
    checkValue(gasFlow, "Вихід газу", 0.0, 1000.0)?.let { return it }
    checkValue(methane, "Метан", 0.0, 100.0)?.let { return it }
    return null
}

fun toNumber(text: String): Double {
    return text.replace(',', '.').toDouble()
}

fun calculateEnergy(gasFlow: Double, methane: Double): Double {
    return gasFlow * methane / 100 * METHANE_ENERGY
}

fun isDeviation(measurement: Measurement): Boolean {
    val temperatureOk = measurement.temperature in NORMAL_TEMP_MIN..NORMAL_TEMP_MAX
    val phOk = measurement.ph in NORMAL_PH_MIN..NORMAL_PH_MAX
    return !(temperatureOk && phOk)
}

class BiogasRepository(
    private val reactorDao: ReactorDao,
    private val measurementDao: MeasurementDao
) {
    private val statsCache = mutableMapOf<Int, ReactorStats>()

    fun getReactorSummaries(search: String): Flow<List<ReactorSummary>> {
        return reactorDao.searchReactorSummaries(search.trim())
    }

    fun getReactorWithMeasurements(reactorId: Int): Flow<ReactorWithMeasurements?> {
        return reactorDao.getReactorWithMeasurements(reactorId)
    }

    suspend fun addReactorWithMeasurement(
        name: String, temperature: Double, ph: Double, gasFlow: Double, methane: Double
    ): String? {
        return try {
            reactorDao.insertReactorWithMeasurement(
                Reactor(name = name.trim()),
                Measurement(
                    reactorId = 0,
                    temperature = temperature,
                    ph = ph,
                    gasFlow = gasFlow,
                    methane = methane,
                    time = System.currentTimeMillis()
                )
            )
            null
        } catch (e: SQLiteConstraintException) {
            "Реактор з такою назвою вже існує"
        } catch (e: Exception) {
            "Помилка бази даних: ${e.message}"
        }
    }

    suspend fun deleteReactor(reactorId: Int): String? {
        return try {
            reactorDao.deleteReactor(reactorId)
            statsCache.remove(reactorId)
            null
        } catch (e: Exception) {
            "Не вдалося видалити реактор: ${e.message}"
        }
    }

    suspend fun addMeasurement(
        reactorId: Int, temperature: Double, ph: Double, gasFlow: Double, methane: Double
    ): String? {
        return try {
            measurementDao.insertMeasurement(
                Measurement(
                    reactorId = reactorId,
                    temperature = temperature,
                    ph = ph,
                    gasFlow = gasFlow,
                    methane = methane,
                    time = System.currentTimeMillis()
                )
            )
            statsCache.remove(reactorId)
            null
        } catch (e: Exception) {
            "Не вдалося додати вимірювання: ${e.message}"
        }
    }

    suspend fun updateMeasurement(measurement: Measurement): String? {
        return try {
            measurementDao.updateMeasurement(measurement)
            statsCache.remove(measurement.reactorId)
            null
        } catch (e: Exception) {
            "Не вдалося оновити вимірювання: ${e.message}"
        }
    }

    suspend fun deleteMeasurement(measurement: Measurement): String? {
        return try {
            measurementDao.deleteMeasurement(measurement)
            statsCache.remove(measurement.reactorId)
            null
        } catch (e: Exception) {
            "Не вдалося видалити вимірювання: ${e.message}"
        }
    }

    suspend fun getStats(reactorId: Int): ReactorStats? {
        statsCache[reactorId]?.let { return it }

        val summary = reactorDao.getReactorSummary(reactorId) ?: return null
        val measurements = measurementDao.getMeasurementsForReactor(reactorId)

        var totalEnergy = 0.0
        for (m in measurements) {
            totalEnergy += calculateEnergy(m.gasFlow, m.methane)
        }

        val stats = ReactorStats(
            measurementCount = summary.measurementCount,
            avgTemperature = summary.avgTemperature ?: 0.0,
            avgPh = summary.avgPh ?: 0.0,
            totalGasFlow = summary.totalGasFlow ?: 0.0,
            totalEnergy = totalEnergy
        )

        statsCache[reactorId] = stats
        return stats
    }

    fun filterAndSort(
        measurements: List<Measurement>,
        onlyDeviations: Boolean,
        sortType: SortType,
        ascending: Boolean
    ): List<Measurement> {
        val filtered = if (onlyDeviations) {
            measurements.filter { isDeviation(it) }
        } else {
            measurements
        }

        val sorted = when (sortType) {
            SortType.TIME -> filtered.sortedBy { it.time }
            SortType.TEMPERATURE -> filtered.sortedBy { it.temperature }
            SortType.GAS_FLOW -> filtered.sortedBy { it.gasFlow }
        }

        return if (ascending) sorted else sorted.reversed()
    }
}