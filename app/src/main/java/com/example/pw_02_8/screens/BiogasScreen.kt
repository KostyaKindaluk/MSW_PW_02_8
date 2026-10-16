package com.example.pw_02_8.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.pw_02_8.data.entity.Measurement
import com.example.pw_02_8.data.relation.ReactorSummary
import com.example.pw_02_8.components.ErrorMessage
import com.example.pw_02_8.components.FilterSortBar
import com.example.pw_02_8.components.InputField
import com.example.pw_02_8.components.MeasurementFields
import com.example.pw_02_8.components.MeasurementItem
import com.example.pw_02_8.components.ReactorItem
import com.example.pw_02_8.components.ScreenTitle
import com.example.pw_02_8.components.SectionTitle
import com.example.pw_02_8.components.StatsPanel
import com.example.pw_02_8.viewmodel.BiogasViewModel

@Composable
fun BiogasScreen(viewModel: BiogasViewModel, modifier: Modifier = Modifier) {
    val search by viewModel.search.collectAsState()
    val reactors by viewModel.reactors.collectAsState()
    val selectedReactorId by viewModel.selectedReactorId.collectAsState()
    val selectedReactorName by viewModel.selectedReactorName.collectAsState()
    val measurements by viewModel.measurements.collectAsState()
    val stats by viewModel.stats.collectAsState()
    val editingMeasurement by viewModel.editingMeasurement.collectAsState()
    val onlyDeviations by viewModel.onlyDeviations.collectAsState()
    val sortType by viewModel.sortType.collectAsState()
    val sortAscending by viewModel.sortAscending.collectAsState()
    val message by viewModel.message.collectAsState()

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isLargeScreen = maxWidth > 600.dp

        if (isLargeScreen) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                ScreenTitle("Моніторинг біогазової установки")
                message?.let { ErrorMessage(it) }

                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .verticalScroll(rememberScrollState())
                    ) {
                        ReactorsPanel(reactors, search, selectedReactorId, viewModel)
                    }
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .verticalScroll(rememberScrollState())
                    ) {
                        MeasurementsPanel(
                            selectedReactorName, measurements, editingMeasurement,
                            onlyDeviations, sortType, sortAscending, stats, viewModel
                        )
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                ScreenTitle("Моніторинг біогазової установки")
                message?.let { ErrorMessage(it) }
                ReactorsPanel(reactors, search, selectedReactorId, viewModel)
                Spacer(modifier = Modifier.height(16.dp))
                MeasurementsPanel(
                    selectedReactorName, measurements, editingMeasurement,
                    onlyDeviations, sortType, sortAscending, stats, viewModel
                )
            }
        }
    }
}

@Composable
private fun ReactorsPanel(
    reactors: List<ReactorSummary>,
    search: String,
    selectedReactorId: Int?,
    viewModel: BiogasViewModel
) {
    SectionTitle("Реактори")

    InputField(
        value = search,
        onValueChange = { viewModel.onSearchChange(it) },
        label = "Пошук за назвою",
        isNumber = false
    )

    if (reactors.isEmpty()) {
        Text("Реакторів не знайдено", modifier = Modifier.padding(vertical = 8.dp))
    } else {
        for (reactor in reactors) {
            ReactorItem(
                summary = reactor,
                isSelected = reactor.reactorId == selectedReactorId,
                onClick = { viewModel.selectReactor(reactor.reactorId) },
                onDelete = { viewModel.deleteReactor(reactor.reactorId) }
            )
        }
    }

    Spacer(modifier = Modifier.height(8.dp))
    AddReactorForm(viewModel)
}

@Composable
private fun AddReactorForm(viewModel: BiogasViewModel) {
    var name by remember { mutableStateOf("") }
    var temperature by remember { mutableStateOf("") }
    var ph by remember { mutableStateOf("") }
    var gasFlow by remember { mutableStateOf("") }
    var methane by remember { mutableStateOf("") }

    SectionTitle("Новий реактор")
    InputField(value = name, onValueChange = { name = it }, label = "Назва реактора", isNumber = false)
    Text("Перше вимірювання:", modifier = Modifier.padding(top = 8.dp))
    MeasurementFields(
        temperature, { temperature = it },
        ph, { ph = it },
        gasFlow, { gasFlow = it },
        methane, { methane = it }
    )
    Button(
        onClick = {
            if (viewModel.addReactor(name, temperature, ph, gasFlow, methane)) {
                name = ""
                temperature = ""
                ph = ""
                gasFlow = ""
                methane = ""
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
    ) {
        Text("Додати реактор")
    }
}

@Composable
private fun MeasurementsPanel(
    selectedReactorName: String?,
    measurements: List<Measurement>,
    editingMeasurement: Measurement?,
    onlyDeviations: Boolean,
    sortType: com.example.pw_02_8.repository.SortType,
    sortAscending: Boolean,
    stats: com.example.pw_02_8.repository.ReactorStats?,
    viewModel: BiogasViewModel
) {
    if (selectedReactorName == null) {
        SectionTitle("Вимірювання")
        Text("Оберіть реактор зі списку")
        return
    }

    SectionTitle("Реактор: $selectedReactorName")

    StatsPanel(stats)

    MeasurementForm(editingMeasurement, viewModel)

    FilterSortBar(
        onlyDeviations = onlyDeviations,
        onOnlyDeviationsChange = { viewModel.onOnlyDeviationsChange(it) },
        sortType = sortType,
        onSortTypeChange = { viewModel.onSortTypeChange(it) },
        ascending = sortAscending,
        onDirectionChange = { viewModel.onSortDirectionChange() }
    )

    if (measurements.isEmpty()) {
        Text("Вимірювань немає", modifier = Modifier.padding(vertical = 8.dp))
    } else {
        for (measurement in measurements) {
            MeasurementItem(
                measurement = measurement,
                onEdit = { viewModel.startEdit(measurement) },
                onDelete = { viewModel.deleteMeasurement(measurement) }
            )
        }
    }
}

@Composable
private fun MeasurementForm(editing: Measurement?, viewModel: BiogasViewModel) {
    var temperature by remember(editing) { mutableStateOf(editing?.temperature?.toString() ?: "") }
    var ph by remember(editing) { mutableStateOf(editing?.ph?.toString() ?: "") }
    var gasFlow by remember(editing) { mutableStateOf(editing?.gasFlow?.toString() ?: "") }
    var methane by remember(editing) { mutableStateOf(editing?.methane?.toString() ?: "") }

    Text(
        text = if (editing == null) "Нове вимірювання:" else "Редагування вимірювання:",
        modifier = Modifier.padding(top = 8.dp)
    )
    MeasurementFields(
        temperature, { temperature = it },
        ph, { ph = it },
        gasFlow, { gasFlow = it },
        methane, { methane = it }
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            onClick = {
                if (viewModel.saveMeasurement(temperature, ph, gasFlow, methane)) {
                    temperature = ""
                    ph = ""
                    gasFlow = ""
                    methane = ""
                }
            },
            modifier = Modifier.weight(1f)
        ) {
            Text(if (editing == null) "Додати вимірювання" else "Зберегти зміни")
        }
        if (editing != null) {
            OutlinedButton(
                onClick = { viewModel.cancelEdit() },
                modifier = Modifier.weight(1f)
            ) {
                Text("Скасувати")
            }
        }
    }
}