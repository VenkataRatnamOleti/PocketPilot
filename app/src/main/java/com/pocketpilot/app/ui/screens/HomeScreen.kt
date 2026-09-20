package com.pocketpilot.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pocketpilot.app.ui.components.BalanceCard
import com.pocketpilot.app.ui.components.ExpenseCard
import com.pocketpilot.app.ui.components.SummaryCard
import com.pocketpilot.app.ui.util.formatInr
import com.pocketpilot.app.viewmodel.Expense

@Composable
fun HomeScreen(
    availableToSpend: Double,
    totalExpenses: Double,
    upcomingExpenses: Double,
    expenses: List<Expense>,
    onAddExpense: () -> Unit,
    onAskPocketPilot: () -> Unit
) {

    // Newest expense first. Reading the list here makes Home recompose when it changes.
    val recentFirst = expenses.reversed()

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

                    Button(
                        onClick = onAskPocketPilot,
                        modifier = Modifier.fillMaxWidth()
                    ) {

                        Icon(
                            imageVector = Icons.Default.QuestionAnswer,
                            contentDescription = null
                        )

                        Spacer(modifier = Modifier.width(8.dp))

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
                            expense = expense
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
        }
    }
}
