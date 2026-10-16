package com.example.pw_02_8.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun MeasurementFields(
    temperature: String,
    onTemperatureChange: (String) -> Unit,
    ph: String,
    onPhChange: (String) -> Unit,
    gasFlow: String,
    onGasFlowChange: (String) -> Unit,
    methane: String,
    onMethaneChange: (String) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            InputField(temperature, onTemperatureChange, "Температура, °C", Modifier.weight(1f))
            InputField(ph, onPhChange, "pH", Modifier.weight(1f))
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            InputField(gasFlow, onGasFlowChange, "Вихід газу, м³/год", Modifier.weight(1f))
            InputField(methane, onMethaneChange, "Метан, %", Modifier.weight(1f))
        }
    }
}