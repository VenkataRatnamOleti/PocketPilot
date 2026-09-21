package com.pocketpilot.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "loans")
data class LoanEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val personName: String,
    val phoneOrNote: String,
    val amount: Double,
    val type: String,
    val date: Long,
    val dueDate: Long? = null,
    val isSettled: Boolean = false
)
