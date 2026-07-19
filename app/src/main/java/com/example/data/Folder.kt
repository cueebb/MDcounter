package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "folders")
data class Folder(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val colorHex: String,
    val iconName: String,
    val isSmart: Boolean = false,
    val defaultStepSize: Int = 1,
    val defaultResetValue: Int = 0,
    val defaultTargetValue: Int? = null,
    val defaultQuickButtons: String = "",
    val historyDividerThreshold: Float = 0f,
    val createdAt: Long = System.currentTimeMillis()
)
