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

    fun getLogsForCounters(counterIds: List<Long>): Flow<List<CounterLog>> {
        return repository.getLogsForCounters(counterIds)
    }

    // Folder Actions
    fun createFolder(
        name: String,
        colorHex: String,
        iconName: String,
        isSmart: Boolean = false,
        defaultStepSize: Int = 1,
        defaultResetValue: Int = 0,
        defaultTargetValue: Int? = null,
        defaultQuickButtons: String = "",
        historyDividerThreshold: Float = 0f
    ) {
        viewModelScope.launch {
            repository.insertFolder(
                Folder(
                    name = name,
                    colorHex = colorHex,
                    iconName = iconName,
                    isSmart = isSmart,
                    defaultStepSize = defaultStepSize,
                    defaultResetValue = defaultResetValue,
                    defaultTargetValue = defaultTargetValue,
                    defaultQuickButtons = defaultQuickButtons,
                    historyDividerThreshold = historyDividerThreshold
                )
            )
        }
    }

    fun updateFolder(folder: Folder) {
        viewModelScope.launch {
            val oldFolder = db.counterDao().getFolderById(folder.id)
            if (oldFolder != null && oldFolder.isSmart) {
                val countersInFolder = db.counterDao().getCountersByFolder(folder.id).first()
                for (counter in countersInFolder) {
                    var updatedCounter = counter
                    var modified = false
                    
                    if (counter.stepSize == oldFolder.defaultStepSize) {
                        updatedCounter = updatedCounter.copy(stepSize = folder.defaultStepSize)
                        modified = true
                    }
                    if (counter.resetValue == oldFolder.defaultResetValue) {
                        updatedCounter = updatedCounter.copy(resetValue = folder.defaultResetValue)
                        modified = true
                    }
                    if (counter.targetValue == oldFolder.defaultTargetValue) {
                        updatedCounter = updatedCounter.copy(targetValue = folder.defaultTargetValue)
                        modified = true
                    }
                    if (counter.quickButtons == oldFolder.defaultQuickButtons) {
                        updatedCounter = updatedCounter.copy(quickButtons = folder.defaultQuickButtons)
                        modified = true
                    }
                    
                    if (modified) {
                        db.counterDao().updateCounter(updatedCounter.copy(lastModified = System.currentTimeMillis()))
                    }
                }
            }
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
        quickButtons: String,
        historyDividerThreshold: Float = 0f
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
                quickButtons = quickButtons,
                historyDividerThreshold = historyDividerThreshold
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

    fun duplicateCounter(counter: Counter) {
        viewModelScope.launch {
            val duplicated = Counter(
                folderId = counter.folderId,
                name = "${counter.name} (Copy)",
                currentValue = counter.currentValue,
                initialValue = counter.initialValue,
                stepSize = counter.stepSize,
                targetValue = counter.targetValue,
                resetValue = counter.resetValue,
                colorHex = counter.colorHex,
                note = counter.note,
                quickButtons = counter.quickButtons,
                historyDividerThreshold = counter.historyDividerThreshold
            )
            val newId = repository.insertCounter(duplicated)
            // Create initial creation log
            repository.incrementCounter(
                counter = duplicated.copy(id = newId),
                amount = 0,
                logNote = "Duplicated from '${counter.name}' with value ${counter.currentValue}"
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

    fun insertManualDivider(counterId: Long, isGlobal: Boolean) {
        viewModelScope.launch {
            val counter = db.counterDao().getCounterById(counterId)
            val currentVal = counter?.currentValue ?: 0
            val log = CounterLog(
                counterId = counterId,
                previousValue = currentVal,
                newValue = currentVal,
                changeValue = 0,
                note = if (isGlobal) "[MANUAL_DIVIDER_GLOBAL]" else "[MANUAL_DIVIDER_LOCAL]",
                timestamp = System.currentTimeMillis()
            )
            db.counterDao().insertLog(log)
        }
    }

    fun insertGlobalDivider(allCounters: List<Counter>) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            allCounters.forEach { counter ->
                val log = CounterLog(
                    counterId = counter.id,
                    previousValue = counter.currentValue,
                    newValue = counter.currentValue,
                    changeValue = 0,
                    note = "[MANUAL_DIVIDER_GLOBAL]",
                    timestamp = now
                )
                db.counterDao().insertLog(log)
            }
        }
    }
}
