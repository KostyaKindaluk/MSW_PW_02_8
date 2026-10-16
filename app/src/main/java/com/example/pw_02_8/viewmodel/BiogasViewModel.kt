package com.example.pw_02_8.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pw_02_8.data.entity.Measurement
import com.example.pw_02_8.data.relation.ReactorSummary
import com.example.pw_02_8.data.relation.ReactorWithMeasurements
import com.example.pw_02_8.repository.BiogasRepository
import com.example.pw_02_8.repository.ReactorStats
import com.example.pw_02_8.repository.SortType
import com.example.pw_02_8.repository.checkMeasurement
import com.example.pw_02_8.repository.checkReactorName
import com.example.pw_02_8.repository.toNumber
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class BiogasViewModel(private val repository: BiogasRepository) : ViewModel() {
    private val _search = MutableStateFlow("")
    val search: StateFlow<String> = _search.asStateFlow()

    val reactors: StateFlow<List<ReactorSummary>> = _search
        .flatMapLatest { text -> repository.getReactorSummaries(text) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedReactorId = MutableStateFlow<Int?>(null)
    val selectedReactorId: StateFlow<Int?> = _selectedReactorId.asStateFlow()

    private val selectedReactor: Flow<ReactorWithMeasurements?> = _selectedReactorId
        .flatMapLatest { id ->
            if (id == null) flowOf(null) else repository.getReactorWithMeasurements(id)
        }

    val selectedReactorName: StateFlow<String?> = selectedReactor
        .map { it?.reactor?.name }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _onlyDeviations = MutableStateFlow(false)
    val onlyDeviations: StateFlow<Boolean> = _onlyDeviations.asStateFlow()

    private val _sortType = MutableStateFlow(SortType.TIME)
    val sortType: StateFlow<SortType> = _sortType.asStateFlow()

    private val _sortAscending = MutableStateFlow(false)
    val sortAscending: StateFlow<Boolean> = _sortAscending.asStateFlow()

    val measurements: StateFlow<List<Measurement>> = combine(
        selectedReactor, _onlyDeviations, _sortType, _sortAscending
    ) { reactor, onlyDeviations, sortType, ascending ->
        repository.filterAndSort(reactor?.measurements ?: emptyList(), onlyDeviations, sortType, ascending)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _stats = MutableStateFlow<ReactorStats?>(null)
    val stats: StateFlow<ReactorStats?> = _stats.asStateFlow()

    private val _editingMeasurement = MutableStateFlow<Measurement?>(null)
    val editingMeasurement: StateFlow<Measurement?> = _editingMeasurement.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    // ---------- Дії користувача ----------

    fun onSearchChange(text: String) {
        _search.value = text
    }

    fun selectReactor(reactorId: Int) {
        _selectedReactorId.value = reactorId
        _editingMeasurement.value = null
        _message.value = null
        viewModelScope.launch { updateStats() }
    }

    fun onOnlyDeviationsChange(value: Boolean) {
        _onlyDeviations.value = value
    }

    fun onSortTypeChange(type: SortType) {
        _sortType.value = type
    }

    fun onSortDirectionChange() {
        _sortAscending.value = !_sortAscending.value
    }

    fun addReactor(name: String, temperature: String, ph: String, gasFlow: String, methane: String): Boolean {
        _message.value = null
        val error = checkReactorName(name) ?: checkMeasurement(temperature, ph, gasFlow, methane)
        if (error != null) {
            _message.value = error
            return false
        }
        viewModelScope.launch {
            val result = repository.addReactorWithMeasurement(
                name, toNumber(temperature), toNumber(ph), toNumber(gasFlow), toNumber(methane)
            )
            if (result != null) _message.value = result
        }
        return true
    }

    fun deleteReactor(reactorId: Int) {
        _message.value = null
        viewModelScope.launch {
            val result = repository.deleteReactor(reactorId)
            if (result != null) {
                _message.value = result
            } else if (_selectedReactorId.value == reactorId) {
                _selectedReactorId.value = null
                _editingMeasurement.value = null
                _stats.value = null
            }
        }
    }

    fun startEdit(measurement: Measurement) {
        _editingMeasurement.value = measurement
    }

    fun cancelEdit() {
        _editingMeasurement.value = null
    }

    fun saveMeasurement(temperature: String, ph: String, gasFlow: String, methane: String): Boolean {
        _message.value = null
        val reactorId = _selectedReactorId.value ?: return false
        val error = checkMeasurement(temperature, ph, gasFlow, methane)
        if (error != null) {
            _message.value = error
            return false
        }
        val editing = _editingMeasurement.value
        viewModelScope.launch {
            val result = if (editing == null) {
                repository.addMeasurement(
                    reactorId, toNumber(temperature), toNumber(ph), toNumber(gasFlow), toNumber(methane)
                )
            } else {
                repository.updateMeasurement(
                    editing.copy(
                        temperature = toNumber(temperature),
                        ph = toNumber(ph),
                        gasFlow = toNumber(gasFlow),
                        methane = toNumber(methane)
                    )
                )
            }
            if (result != null) _message.value = result
            updateStats()
        }
        _editingMeasurement.value = null
        return true
    }

    fun deleteMeasurement(measurement: Measurement) {
        _message.value = null
        viewModelScope.launch {
            val result = repository.deleteMeasurement(measurement)
            if (result != null) _message.value = result
            updateStats()
        }
    }

    private suspend fun updateStats() {
        val reactorId = _selectedReactorId.value
        if (reactorId == null) {
            _stats.value = null
            return
        }
        try {
            _stats.value = repository.getStats(reactorId)
        } catch (e: Exception) {
            _message.value = "Не вдалося завантажити статистику"
        }
    }
}