package com.pocketpilot.app.ui.screens

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pocketpilot.app.ui.components.BalanceCard
import com.pocketpilot.app.ui.components.ExpenseCard
import com.pocketpilot.app.ui.components.SummaryCard
import com.pocketpilot.app.ui.util.formatInr
import com.pocketpilot.app.viewmodel.Expense

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    availableToSpend: Double,
    totalExpenses: Double,
    totalProfit: Double,
    upcomingExpenses: Double,
    expenses: List<Expense>,
    onAddExpense: () -> Unit,
    onAskPocketPilot: () -> Unit,
    monthlyIncome: Double,
    onBudgetUpdated: (Double, Double) -> Unit,
    onDeleteExpense: (Int) -> Unit,
    onExpenseSelected: (Expense) -> Unit
) {

    // Newest expense first. Reading the list here makes Home recompose when it changes.
    val recentFirst = expenses.reversed()
    var showBudgetEditor by remember { mutableStateOf(false) }
    var incomeInput by remember { mutableStateOf(monthlyIncome.toInt().toString()) }
    var upcomingInput by remember { mutableStateOf(upcomingExpenses.toInt().toString()) }

    Surface(
        modifier = Modifier.fillMaxSize()
    ) {

        Box(
            modifier = Modifier.fillMaxSize()
        ) {

            LazyColumn(
                modifier = Modifier.fillMaxSize(),

                // Extra bottom padding so the floating button never covers the last item
                contentPadding = PaddingValues(
                    start = 20.dp,
                    top = 20.dp,
                    end = 20.dp,
                    bottom = 96.dp
                ),

                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                item {

                    Column {

                        Text(
                            text = "PocketPilot",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "Your monthly budget at a glance",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                item {
                    SpendingChart(
                        spent = totalExpenses,
                        saved = (monthlyIncome - totalExpenses).coerceAtLeast(0.0),
                        income = monthlyIncome
                    )
                }

                item {
                    OutlinedButton(onClick = { showBudgetEditor = true }, modifier = Modifier.fillMaxWidth()) {
                        Text("Update monthly plan")
                    }
                }

                item {

                    BalanceCard(
                        availableToSpend = availableToSpend
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {

                        SummaryCard(
                            title = "Spent so far",
                            value = formatInr(totalExpenses),
                            modifier = Modifier.weight(1f)
                        )

                        SummaryCard(
                            title = "Upcoming",
                            value = formatInr(upcomingExpenses),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    SummaryCard(
                        title = "Profit / income",
                        value = formatInr(totalProfit),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {

                    Button(
                        onClick = onAskPocketPilot,
                        modifier = Modifier.fillMaxWidth()
                    ) {

                        Text("Can I afford this?")
                    }
                }

                item {

                    Text(
                        text = "Recent expenses",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                if (recentFirst.isEmpty()) {

                    item {

                        Text(
                            text = "No expenses yet. Tap + to add your first one.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                } else {

                    items(
                        items = recentFirst,
                        key = { expense -> expense.id }
                    ) { expense ->

                        ExpenseCard(
                            expense = expense,
                            onDelete = { onDeleteExpense(expense.id) },
                            onClick = { onExpenseSelected(expense) }
                        )
                    }
                }
            }

            FloatingActionButton(
                onClick = onAddExpense,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(20.dp)
            ) {

                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add expense"
                )
            }

            if (showBudgetEditor) {
                AlertDialog(
                    onDismissRequest = { showBudgetEditor = false },
                    title = { Text("Monthly plan") },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(incomeInput, { incomeInput = it }, label = { Text("Monthly income (₹)") }, singleLine = true)
                            OutlinedTextField(upcomingInput, { upcomingInput = it }, label = { Text("Upcoming commitments (₹)") }, singleLine = true)
                        }
                    },
                    confirmButton = { TextButton(onClick = {
                        val income = incomeInput.toDoubleOrNull()
                        val upcoming = upcomingInput.toDoubleOrNull()
                        if (income != null && upcoming != null && income >= 0 && upcoming >= 0) {
                            onBudgetUpdated(income, upcoming)
                            showBudgetEditor = false
                        }
                    }) { Text("Save") } },
                    dismissButton = { TextButton(onClick = { showBudgetEditor = false }) { Text("Cancel") } }
                )
            }
        }
    }
}

@Composable
private fun SpendingChart(spent: Double, saved: Double, income: Double) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("This month", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text("Spending vs saving", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(12.dp))
            Canvas(modifier = Modifier.fillMaxWidth().height(150.dp)) {
                val maximum = income.coerceAtLeast(1.0).toFloat()
                val values = listOf(spent.toFloat(), saved.toFloat())
                val colors = listOf(Color(0xFFE76F51), Color(0xFF2A9D8F))
                val barWidth = size.width / 4f
                values.forEachIndexed { index, value ->
                    val height = (value / maximum) * (size.height - 12f)
                    drawRect(colors[index], topLeft = androidx.compose.ui.geometry.Offset(size.width * (0.2f + index * 0.42f), size.height - height), size = androidx.compose.ui.geometry.Size(barWidth, height))
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                Text("Spent ${formatInr(spent)}", color = Color(0xFFE76F51), style = MaterialTheme.typography.bodySmall)
                Text("Saved ${formatInr(saved)}", color = Color(0xFF2A9D8F), style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
