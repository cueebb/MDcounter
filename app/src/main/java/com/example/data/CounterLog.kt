package com.example.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "counter_logs",
    foreignKeys = [
        ForeignKey(
            entity = Counter::class,
            parentColumns = ["id"],
            childColumns = ["counterId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["counterId"])]
)
data class CounterLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val counterId: Long,
    val timestamp: Long = System.currentTimeMillis(),
    val previousValue: Int,
    val newValue: Int,
    val changeValue: Int,
    val note: String = ""
)
