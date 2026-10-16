package com.example.pw_02_8.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.pw_02_8.data.dao.MeasurementDao
import com.example.pw_02_8.data.dao.ReactorDao
import com.example.pw_02_8.data.entity.Measurement
import com.example.pw_02_8.data.entity.Reactor

@Database(
    entities = [Reactor::class, Measurement::class],
    version = 1,
    exportSchema = false
)
abstract class BiogasDatabase : RoomDatabase() {

    abstract fun reactorDao(): ReactorDao
    abstract fun measurementDao(): MeasurementDao

    companion object {
        @Volatile
        private var INSTANCE: BiogasDatabase? = null

        fun getDatabase(context: Context): BiogasDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BiogasDatabase::class.java,
                    "biogas_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}