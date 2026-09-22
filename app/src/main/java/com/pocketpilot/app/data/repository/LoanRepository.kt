package com.pocketpilot.app.data.repository

import com.pocketpilot.app.data.local.LoanDao
import com.pocketpilot.app.data.local.LoanEntity
import kotlinx.coroutines.flow.Flow

class LoanRepository(private val loanDao: LoanDao) {
    val allLoans: Flow<List<LoanEntity>> = loanDao.getAll()

    fun openLoans(): Flow<List<LoanEntity>> = loanDao.getOpenLoans()

    fun forPerson(personName: String): Flow<List<LoanEntity>> =
        loanDao.getForPerson(personName)

    fun openTotal(type: String): Flow<Double> = loanDao.getOpenTotal(type)

    fun openBalance(): Flow<Double> = loanDao.getOpenBalance()

    suspend fun insert(loan: LoanEntity) = loanDao.insert(loan)

    suspend fun delete(id: Int) = loanDao.delete(id)

    suspend fun setSettled(id: Int, settled: Boolean) = loanDao.setSettled(id, settled)
}
