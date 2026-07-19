package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CounterDao {
    // folders
    @Query("SELECT * FROM folders ORDER BY name ASC")
    fun getAllFolders(): Flow<List<Folder>>

    @Query("SELECT * FROM folders WHERE id = :id")
    suspend fun getFolderById(id: Long): Folder?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFolder(folder: Folder): Long

    @Update
    suspend fun updateFolder(folder: Folder)

    @Delete
    suspend fun deleteFolder(folder: Folder)

    // counters
    @Query("SELECT * FROM counters ORDER BY lastModified DESC, name ASC")
    fun getAllCounters(): Flow<List<Counter>>

    @Query("SELECT * FROM counters WHERE folderId = :folderId ORDER BY lastModified DESC, name ASC")
    fun getCountersByFolder(folderId: Long): Flow<List<Counter>>

    @Query("SELECT * FROM counters WHERE id = :id")
    suspend fun getCounterById(id: Long): Counter?

    @Query("SELECT * FROM counters WHERE id = :id")
    fun getCounterFlowById(id: Long): Flow<Counter?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCounter(counter: Counter): Long

    @Update
    suspend fun updateCounter(counter: Counter)

    @Delete
    suspend fun deleteCounter(counter: Counter)

    // logs
    @Query("SELECT * FROM counter_logs WHERE counterId = :counterId ORDER BY timestamp DESC")
    fun getLogsForCounter(counterId: Long): Flow<List<CounterLog>>

    @Query("SELECT * FROM counter_logs WHERE counterId IN (:counterIds) ORDER BY timestamp DESC")
    fun getLogsForCounters(counterIds: List<Long>): Flow<List<CounterLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: CounterLog)

    @Query("DELETE FROM counter_logs WHERE counterId = :counterId")
    suspend fun deleteLogsForCounter(counterId: Long)
}
