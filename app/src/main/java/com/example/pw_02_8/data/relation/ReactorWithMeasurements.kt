package com.example.pw_02_8.data.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.example.pw_02_8.data.entity.Measurement
import com.example.pw_02_8.data.entity.Reactor

data class ReactorWithMeasurements(
    @Embedded val reactor: Reactor,
    @Relation(
        parentColumn = "id",
        entityColumn = "reactorId"
    )
    val measurements: List<Measurement>
)