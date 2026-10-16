package com.example.pw_02_8.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.pw_02_8.data.entity.Measurement
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MeasurementItem(
    measurement: Measurement,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
    val timeText = dateFormat.format(Date(measurement.time))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(timeText, fontWeight = FontWeight.Bold)
            InfoRow("Температура:", "${measurement.temperature} °C")
            InfoRow("pH:", "${measurement.ph}")
            InfoRow("Вихід газу:", "${measurement.gasFlow} м³/год")
            InfoRow("Метан:", "${measurement.methane} %")
            Row {
                TextButton(onClick = onEdit) { Text("Редагувати") }
                TextButton(onClick = onDelete) { Text("Видалити") }
            }
        }
    }
}