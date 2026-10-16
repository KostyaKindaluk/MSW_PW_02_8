package com.example.pw_02_8.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "reactors",
    indices = [Index(value = ["name"], unique = true)]
)
data class Reactor(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String
)