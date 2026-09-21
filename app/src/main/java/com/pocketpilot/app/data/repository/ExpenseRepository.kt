package com.pocketpilot.app.data.repository

import com.pocketpilot.app.data.local.ExpenseDao
import com.pocketpilot.app.data.local.ExpenseEntity
import kotlinx.coroutines.flow.Flow

class ExpenseRepository(private val expenseDao: ExpenseDao) {
    val allExpenses: Flow<List<ExpenseEntity>> = expenseDao.getAllExpenses()

    suspend fun insert(expense: ExpenseEntity) {
        expenseDao.insertExpense(expense)
    }

    suspend fun isEmpty(): Boolean = expenseDao.count() == 0
}
