package com.pocketpilot.app.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

data class Expense(
    val id: Int,
    val category: String,
    val description: String,
    val amount: Double
)

class ExpenseViewModel : ViewModel() {

    var monthlyIncome by mutableStateOf(15000.0)

    var upcomingExpenses by mutableStateOf(2000.0)

    private var nextId = 6

    private val _expenses = mutableStateListOf(
        Expense(1, "Hostel", "Monthly hostel fee", 4000.0),
        Expense(2, "Food", "Food & groceries", 2500.0),
        Expense(3, "Travel", "Bus and auto", 1200.0),
        Expense(4, "Shopping", "Personal shopping", 800.0),
        Expense(5, "Subscriptions", "OTT / subscriptions", 500.0)
    )

    val expenses: List<Expense>
        get() = _expenses

    val totalExpenses: Double
        get() = _expenses.sumOf { it.amount }

    val availableToSpend: Double
        get() = monthlyIncome - totalExpenses - upcomingExpenses

    fun addExpense(
        amount: Double,
        category: String,
        description: String
    ) {

        if (amount <= 0) return

        _expenses.add(
            Expense(
                id = nextId++,
                category = category,
                description = description,
                amount = amount
            )
        )
    }

    fun calculatePurchaseImpact(amount: Double): Double {

        if (availableToSpend <= 0) return 100.0

        return (amount / availableToSpend) * 100
    }

    fun canAfford(amount: Double): Boolean {
        return amount <= availableToSpend
    }
}