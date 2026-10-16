package com.example.pw_02_8.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "measurements",
    foreignKeys = [ForeignKey(
        entity = Reactor::class,
        parentColumns = ["id"],
        childColumns = ["reactorId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("reactorId")]
)
data class Measurement(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val reactorId: Int,
    val temperature: Double,
    val ph: Double,
    val gasFlow: Double,
    val methane: Double,
    val time: Long
)