package com.pocketpilot.app.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pocketpilot.app.ui.util.formatInr
import com.pocketpilot.app.viewmodel.Expense

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseCard(
    expense: Expense,
    onDelete: (() -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {

    Card(
        onClick = { onClick?.invoke() },
        enabled = onClick != null,
        modifier = modifier.fillMaxWidth()
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    text = expense.description,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = expense.category,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = formatInr(expense.amount),
                fontWeight = FontWeight.Bold
            )

            onDelete?.let { delete ->
                TextButton(onClick = delete) { Text("Delete") }
            }
        }
    }
}
