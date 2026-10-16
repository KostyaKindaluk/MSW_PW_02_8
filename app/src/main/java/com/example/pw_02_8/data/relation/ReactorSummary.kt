package com.example.pw_02_8.data.relation

data class ReactorSummary(
    val reactorId: Int,
    val reactorName: String,
    val measurementCount: Int,
    val avgTemperature: Double?,
    val avgPh: Double?,
    val totalGasFlow: Double?
)