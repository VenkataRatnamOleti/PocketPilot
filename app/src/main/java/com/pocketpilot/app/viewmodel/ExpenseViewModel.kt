package com.pocketpilot.app.viewmodel

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pocketpilot.app.data.local.AppDatabase
import com.pocketpilot.app.data.local.ExpenseEntity
import com.pocketpilot.app.data.repository.ExpenseRepository
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

data class Expense(val id: Int, val category: String, val description: String, val amount: Double, val date: Long)

class ExpenseViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ExpenseRepository(AppDatabase.getDatabase(application).expenseDao())
    private val preferences = application.getSharedPreferences("pocket_pilot_settings", Context.MODE_PRIVATE)

    var monthlyIncome by mutableStateOf(preferences.getFloat("income", 15000f).toDouble())
        private set
    var upcomingExpenses by mutableStateOf(preferences.getFloat("upcoming", 2000f).toDouble())
        private set
    var expenses by mutableStateOf<List<Expense>>(emptyList())
        private set

    val totalExpenses: Double get() = expenses.sumOf { it.amount }
    val availableToSpend: Double get() = monthlyIncome - totalExpenses - upcomingExpenses

    init {
        viewModelScope.launch {
            if (repository.isEmpty()) seedDemoData()
            repository.allExpenses.collect { records ->
                expenses = records.map { Expense(it.id, it.category, it.description, it.amount, it.date) }
            }
        }
    }

    fun updateBudget(income: Double, upcoming: Double) {
        if (income < 0 || upcoming < 0) return
        monthlyIncome = income
        upcomingExpenses = upcoming
        preferences.edit().putFloat("income", income.toFloat()).putFloat("upcoming", upcoming.toFloat()).apply()
    }

    fun addExpense(amount: Double, category: String, description: String, date: Long = System.currentTimeMillis()) {
        if (amount <= 0) return
        viewModelScope.launch {
            repository.insert(ExpenseEntity(amount = amount, category = category.trim().ifBlank { "Other" }, description = description.trim().ifBlank { category }, date = date))
        }
    }

    private suspend fun seedDemoData() {
        val now = System.currentTimeMillis()
        listOf(
            ExpenseEntity(amount = 4000.0, category = "Hostel", description = "Monthly hostel fee", date = now - 8 * DAY),
            ExpenseEntity(amount = 650.0, category = "Food", description = "Groceries", date = now - 4 * DAY),
            ExpenseEntity(amount = 420.0, category = "Travel", description = "Bus and auto", date = now - 2 * DAY),
            ExpenseEntity(amount = 800.0, category = "Shopping", description = "Personal shopping", date = now - DAY),
            ExpenseEntity(amount = 249.0, category = "Subscriptions", description = "Music subscription", date = now)
        ).forEach { repository.insert(it) }
    }

    private companion object { const val DAY = 24L * 60L * 60L * 1000L }
}
