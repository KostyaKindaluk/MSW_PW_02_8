package com.example.pw_02_8.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pw_02_8.repository.SortType

@Composable
fun FilterSortBar(
    onlyDeviations: Boolean,
    onOnlyDeviationsChange: (Boolean) -> Unit,
    sortType: SortType,
    onSortTypeChange: (SortType) -> Unit,
    ascending: Boolean,
    onDirectionChange: () -> Unit
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = onlyDeviations, onCheckedChange = onOnlyDeviationsChange)
            Text("Тільки з відхиленням від норми")
        }

        Text("Сортування:", fontWeight = FontWeight.Medium)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            for (type in SortType.entries) {
                if (type == sortType) {
                    Button(
                        onClick = { onSortTypeChange(type) },
                        contentPadding = PaddingValues(4.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(type.title, fontSize = 12.sp)
                    }
                } else {
                    OutlinedButton(
                        onClick = { onSortTypeChange(type) },
                        contentPadding = PaddingValues(4.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(type.title, fontSize = 12.sp)
                    }
                }
            }
        }

        TextButton(onClick = onDirectionChange) {
            Text(if (ascending) "За зростанням ↑" else "За спаданням ↓")
        }
    }
}