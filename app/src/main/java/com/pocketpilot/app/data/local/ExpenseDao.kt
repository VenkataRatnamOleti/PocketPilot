package com.pocketpilot.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses ORDER BY date DESC")
    fun getAllExpenses(): Flow<List<ExpenseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity)

    @Query("DELETE FROM expenses WHERE id = :id")
    suspend fun deleteExpense(id: Int)

    @Query("SELECT COUNT(*) FROM expenses")
    suspend fun count(): Int

    /** Records whose transaction timestamp falls within [startInclusive, endExclusive]. */
    @Query("SELECT * FROM expenses WHERE date >= :startInclusive AND date < :endExclusive ORDER BY date DESC")
    fun getExpensesBetween(startInclusive: Long, endExclusive: Long): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE date >= :dayStart AND date < :dayEnd ORDER BY date DESC")
    suspend fun getExpensesForDay(dayStart: Long, dayEnd: Long): List<ExpenseEntity>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM expenses WHERE isProfit = 0 AND date >= :startInclusive AND date < :endExclusive")
    suspend fun getExpenseTotalBetween(startInclusive: Long, endExclusive: Long): Double

    @Query("SELECT COALESCE(SUM(amount), 0) FROM expenses WHERE isProfit = 1 AND date >= :startInclusive AND date < :endExclusive")
    suspend fun getProfitTotalBetween(startInclusive: Long, endExclusive: Long): Double
}
