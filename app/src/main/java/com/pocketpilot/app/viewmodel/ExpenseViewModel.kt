package com.pocketpilot.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pocketpilot.app.data.local.AppDatabase
import com.pocketpilot.app.data.local.ExpenseEntity
import com.pocketpilot.app.data.repository.ExpenseRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ExpenseViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: ExpenseRepository

    val allExpenses: StateFlow<List<ExpenseEntity>>

    init {
        val expenseDao = AppDatabase.getDatabase(application).expenseDao()
        repository = ExpenseRepository(expenseDao)
        allExpenses = repository.allExpenses.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    fun addExpense(amount: Double, category: String, description: String) {
        viewModelScope.launch {
            repository.insert(
                ExpenseEntity(
                    amount = amount,
                    category = category,
                    description = description,
                    date = System.currentTimeMillis()
                )
            )
        }
    }
}