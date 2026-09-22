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
import com.pocketpilot.app.data.local.LoanEntity
import com.pocketpilot.app.data.repository.ExpenseRepository
import com.pocketpilot.app.data.repository.LoanRepository
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

data class Expense(val id: Int, val category: String, val description: String, val amount: Double, val date: Long, val receiptImagePath: String? = null, val merchant: String = "", val isProfit: Boolean = false)
data class Loan(val id: Int, val personName: String, val phoneOrNote: String, val amount: Double, val type: String, val date: Long, val dueDate: Long?, val isSettled: Boolean)

class ExpenseViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val repository = ExpenseRepository(database.expenseDao())
    private val loanRepository = LoanRepository(database.loanDao())
    private val preferences = application.getSharedPreferences("pocket_pilot_settings", Context.MODE_PRIVATE)
    var monthlyIncome by mutableStateOf(preferences.getFloat("income", 15000f).toDouble()); private set
    var upcomingExpenses by mutableStateOf(preferences.getFloat("upcoming", 2000f).toDouble()); private set
    var expenses by mutableStateOf<List<Expense>>(emptyList()); private set
    var loans by mutableStateOf<List<Loan>>(emptyList()); private set
    val totalExpenses get() = expenses.filterNot { it.isProfit }.sumOf { it.amount }
    val totalProfit get() = expenses.filter { it.isProfit }.sumOf { it.amount }
    val availableToSpend get() = monthlyIncome + totalProfit - totalExpenses - upcomingExpenses

    init { viewModelScope.launch {
        if (repository.isEmpty()) seedDemoData()
        launch { repository.allExpenses.collect { expenses = it.map { e -> Expense(e.id,e.category,e.description,e.amount,e.date,e.receiptImagePath,e.merchant,e.isProfit) } } }
        launch { loanRepository.allLoans.collect { loans = it.map { l -> Loan(l.id,l.personName,l.phoneOrNote,l.amount,l.type,l.date,l.dueDate,l.isSettled) } } }
    } }
    fun updateBudget(income: Double, upcoming: Double) { if (income >= 0 && upcoming >= 0) { monthlyIncome = income; upcomingExpenses = upcoming; preferences.edit().putFloat("income",income.toFloat()).putFloat("upcoming",upcoming.toFloat()).apply() } }
    fun addExpense(amount: Double, category: String, description: String, receiptImagePath: String? = null, merchant: String = "", isProfit: Boolean = false, date: Long = System.currentTimeMillis()) { if (amount > 0) viewModelScope.launch { repository.insert(ExpenseEntity(amount = amount,category = category.trim().ifBlank { "Other" },description = description.trim().ifBlank { category },date = date,receiptImagePath = receiptImagePath,merchant = merchant,isProfit = isProfit)) } }
    fun deleteExpense(id: Int) = viewModelScope.launch { repository.delete(id) }
    fun addLoan(person: String, note: String, amount: Double, type: String, dueDate: Long? = null, date: Long = System.currentTimeMillis()) { if (person.isNotBlank() && amount > 0) viewModelScope.launch { loanRepository.insert(LoanEntity(personName=person.trim(), phoneOrNote=note.trim(), amount=amount,type=type.trim(),date=date,dueDate=dueDate)) } }
    fun deleteLoan(id: Int) = viewModelScope.launch { loanRepository.delete(id) }
    fun setLoanSettled(id: Int, settled: Boolean) = viewModelScope.launch { loanRepository.setSettled(id, settled) }
    private suspend fun seedDemoData() { val now=System.currentTimeMillis(); listOf(ExpenseEntity(amount=4000.0,category="Hostel",description="Monthly hostel fee",date=now-8*DAY),ExpenseEntity(amount=650.0,category="Food",description="Groceries",date=now-4*DAY),ExpenseEntity(amount=420.0,category="Travel",description="Bus and auto",date=now-2*DAY),ExpenseEntity(amount=800.0,category="Shopping",description="Personal shopping",date=now-DAY),ExpenseEntity(amount=249.0,category="Subscriptions",description="Music subscription",date=now)).forEach { repository.insert(it) } }
    private companion object { const val DAY=86_400_000L }
}
