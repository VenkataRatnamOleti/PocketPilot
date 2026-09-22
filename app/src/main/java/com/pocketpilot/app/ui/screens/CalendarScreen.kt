package com.pocketpilot.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pocketpilot.app.ui.components.ExpenseCard
import com.pocketpilot.app.viewmodel.Expense
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CalendarScreen(
    expenses: List<Expense>,
    onExpenseSelected: (Expense) -> Unit
) {
    val grouped = expenses.groupBy {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(it.date))
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Expense calendar", style = MaterialTheme.typography.headlineSmall)
            Text(
                "Every expense and profit is recorded on its transaction date.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        grouped.toSortedMap().toList().sortedByDescending { it.first }.forEach { (day, dayExpenses) ->
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(day, style = MaterialTheme.typography.titleMedium)
                        Text(
                            "${dayExpenses.size} record(s)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            items(dayExpenses, key = { it.id }) { expense ->
                ExpenseCard(
                    expense = expense,
                    onClick = { onExpenseSelected(expense) }
                )
            }
        }
    }
}
