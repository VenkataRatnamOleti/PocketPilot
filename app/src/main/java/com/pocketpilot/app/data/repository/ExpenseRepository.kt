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

    suspend fun delete(id: Int) = expenseDao.deleteExpense(id)

    fun expensesBetween(startInclusive: Long, endExclusive: Long): Flow<List<ExpenseEntity>> =
        expenseDao.getExpensesBetween(startInclusive, endExclusive)

    suspend fun expensesForDay(dayStart: Long, dayEnd: Long): List<ExpenseEntity> =
        expenseDao.getExpensesForDay(dayStart, dayEnd)

    suspend fun expenseTotalBetween(startInclusive: Long, endExclusive: Long): Double =
        expenseDao.getExpenseTotalBetween(startInclusive, endExclusive)

    suspend fun profitTotalBetween(startInclusive: Long, endExclusive: Long): Double =
        expenseDao.getProfitTotalBetween(startInclusive, endExclusive)
}
