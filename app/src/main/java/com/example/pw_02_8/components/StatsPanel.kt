package com.example.pw_02_8.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.pw_02_8.repository.ReactorStats

@Composable
fun StatsPanel(stats: ReactorStats?) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("Статистика реактора", fontWeight = FontWeight.Bold)
            if (stats == null) {
                Text("Завантаження...")
            } else {
                InfoRow("Кількість вимірювань:", "${stats.measurementCount}")
                InfoRow("Середня температура:", String.format("%.1f °C", stats.avgTemperature))
                InfoRow("Середній pH:", String.format("%.2f", stats.avgPh))
                InfoRow("Сумарний вихід газу:", String.format("%.1f м³", stats.totalGasFlow))
                InfoRow("Енергія з біогазу:", String.format("%.1f кВт·год", stats.totalEnergy))
            }
        }
    }
}