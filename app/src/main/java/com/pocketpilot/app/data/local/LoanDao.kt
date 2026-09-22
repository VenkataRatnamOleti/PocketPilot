package com.pocketpilot.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LoanDao {
    @Query("SELECT * FROM loans ORDER BY date DESC") fun getAll(): Flow<List<LoanEntity>>
    @Insert suspend fun insert(loan: LoanEntity)
    @Query("DELETE FROM loans WHERE id = :id") suspend fun delete(id: Int)
    @Query("UPDATE loans SET isSettled = :settled WHERE id = :id") suspend fun setSettled(id: Int, settled: Boolean)

    @Query("SELECT * FROM loans WHERE isSettled = 0 ORDER BY dueDate IS NULL, dueDate ASC, date DESC")
    fun getOpenLoans(): Flow<List<LoanEntity>>

    @Query("SELECT * FROM loans WHERE personName = :personName ORDER BY date DESC")
    fun getForPerson(personName: String): Flow<List<LoanEntity>>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM loans WHERE type = :type AND isSettled = 0")
    fun getOpenTotal(type: String): Flow<Double>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM loans WHERE isSettled = 0")
    fun getOpenBalance(): Flow<Double>
}
