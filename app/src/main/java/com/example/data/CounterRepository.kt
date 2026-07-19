package com.example.data

import kotlinx.coroutines.flow.Flow

class CounterRepository(private val counterDao: CounterDao) {

    val allFolders: Flow<List<Folder>> = counterDao.getAllFolders()
    val allCounters: Flow<List<Counter>> = counterDao.getAllCounters()

    fun getCountersInFolder(folderId: Long): Flow<List<Counter>> =
        counterDao.getCountersByFolder(folderId)

    fun getLogsForCounter(counterId: Long): Flow<List<CounterLog>> =
        counterDao.getLogsForCounter(counterId)

    fun getLogsForCounters(counterIds: List<Long>): Flow<List<CounterLog>> =
        counterDao.getLogsForCounters(counterIds)

    fun getCounterFlowById(id: Long): Flow<Counter?> =
        counterDao.getCounterFlowById(id)

    suspend fun insertFolder(folder: Folder): Long =
        counterDao.insertFolder(folder)

    suspend fun updateFolder(folder: Folder) =
        counterDao.updateFolder(folder)

    suspend fun deleteFolder(folder: Folder) =
        counterDao.deleteFolder(folder)

    suspend fun insertCounter(counter: Counter): Long =
        counterDao.insertCounter(counter)

    suspend fun updateCounter(counter: Counter) =
        counterDao.updateCounter(counter)

    suspend fun deleteCounter(counter: Counter) =
        counterDao.deleteCounter(counter)

    suspend fun incrementCounter(counter: Counter, amount: Int, logNote: String = "") {
        val previousValue = counter.currentValue
        val newValue = previousValue + amount
        val updatedCounter = counter.copy(
            currentValue = newValue,
            lastModified = System.currentTimeMillis()
        )
        counterDao.updateCounter(updatedCounter)
        
        val log = CounterLog(
            counterId = counter.id,
            previousValue = previousValue,
            newValue = newValue,
            changeValue = amount,
            note = logNote.ifEmpty { if (amount >= 0) "+$amount" else "$amount" }
        )
        counterDao.insertLog(log)
    }

    suspend fun resetCounter(counter: Counter, logNote: String = "") {
        val previousValue = counter.currentValue
        val newValue = counter.resetValue
        val updatedCounter = counter.copy(
            currentValue = newValue,
            lastModified = System.currentTimeMillis()
        )
        counterDao.updateCounter(updatedCounter)
        
        val log = CounterLog(
            counterId = counter.id,
            previousValue = previousValue,
            newValue = newValue,
            changeValue = newValue - previousValue,
            note = logNote.ifEmpty { "Reset to $newValue" }
        )
        counterDao.insertLog(log)
    }
}
