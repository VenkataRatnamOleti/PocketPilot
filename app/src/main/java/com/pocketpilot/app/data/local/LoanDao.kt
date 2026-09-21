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
}
