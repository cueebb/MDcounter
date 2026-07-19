package com.example

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class CounterViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = CounterRepository(db.counterDao())

    val folders: StateFlow<List<Folder>> = repository.allFolders
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _selectedFolderId = MutableStateFlow<Long?>(null)
    val selectedFolderId: StateFlow<Long?> = _selectedFolderId.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val counters: StateFlow<List<Counter>> = _selectedFolderId
        .flatMapLatest { folderId ->
            if (folderId == null) {
                repository.allCounters
            } else {
                repository.getCountersInFolder(folderId)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun selectFolder(folderId: Long?) {
        _selectedFolderId.value = folderId
    }

    fun getLogsForCounter(counterId: Long): Flow<List<CounterLog>> {
        return repository.getLogsForCounter(counterId)
    }

    // Folder Actions
    fun createFolder(name: String, colorHex: String, iconName: String) {
        viewModelScope.launch {
            repository.insertFolder(Folder(name = name, colorHex = colorHex, iconName = iconName))
        }
    }

    fun updateFolder(folder: Folder) {
        viewModelScope.launch {
            repository.updateFolder(folder)
        }
    }

    fun deleteFolder(folder: Folder) {
        viewModelScope.launch {
            if (_selectedFolderId.value == folder.id) {
                _selectedFolderId.value = null
            }
            repository.deleteFolder(folder)
        }
    }

    // Counter Actions
    fun createCounter(
        name: String,
        folderId: Long?,
        initialValue: Int,
        stepSize: Int,
        targetValue: Int?,
        resetValue: Int,
        colorHex: String,
        note: String,
        quickButtons: String
    ) {
        viewModelScope.launch {
            val counter = Counter(
                folderId = folderId,
                name = name,
                currentValue = initialValue,
                initialValue = initialValue,
                stepSize = stepSize,
                targetValue = targetValue,
                resetValue = resetValue,
                colorHex = colorHex,
                note = note,
                quickButtons = quickButtons
            )
            val newId = repository.insertCounter(counter)
            // Create initial creation log
            repository.incrementCounter(
                counter = counter.copy(id = newId),
                amount = 0,
                logNote = "Created counter with initial value $initialValue"
            )
        }
    }

    fun updateCounter(counter: Counter) {
        viewModelScope.launch {
            repository.updateCounter(counter.copy(lastModified = System.currentTimeMillis()))
        }
    }

    fun deleteCounter(counter: Counter) {
        viewModelScope.launch {
            repository.deleteCounter(counter)
        }
    }

    fun increment(counter: Counter, customStep: Int? = null) {
        viewModelScope.launch {
            val step = customStep ?: counter.stepSize
            repository.incrementCounter(counter, step)
        }
    }

    fun decrement(counter: Counter, customStep: Int? = null) {
        viewModelScope.launch {
            val step = customStep ?: counter.stepSize
            repository.incrementCounter(counter, -step)
        }
    }

    fun reset(counter: Counter) {
        viewModelScope.launch {
            repository.resetCounter(counter)
        }
    }
}
