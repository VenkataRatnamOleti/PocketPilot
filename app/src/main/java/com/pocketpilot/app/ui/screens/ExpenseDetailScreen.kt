package com.pocketpilot.app.ui.screens

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pocketpilot.app.ui.util.formatInr
import com.pocketpilot.app.viewmodel.Expense
import java.text.DateFormat
import java.util.Date

@Composable
fun ExpenseDetailScreen(
    expense: Expense,
    onBack: () -> Unit,
    onDelete: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        TextButton(onClick = onBack) { Text("Back") }
        Text(expense.description, style = MaterialTheme.typography.headlineSmall)
        Text(
            if (expense.isProfit) "Profit / income" else "Expense",
            color = if (expense.isProfit) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.error
        )
        Text(formatInr(expense.amount), style = MaterialTheme.typography.displaySmall)
        Text("Category: ${expense.category}")
        if (expense.merchant.isNotBlank()) Text("Merchant: ${expense.merchant}")
        Text("Recorded: ${DateFormat.getDateTimeInstance().format(Date(expense.date))}")
        if (!expense.receiptImagePath.isNullOrBlank()) {
            val receipt = remember(expense.receiptImagePath) {
                BitmapFactory.decodeFile(expense.receiptImagePath)
            }
            if (receipt != null) {
                Image(
                    bitmap = receipt.asImageBitmap(),
                    contentDescription = "Receipt image",
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Text("Receipt image is unavailable: ${expense.receiptImagePath}")
            }
        } else {
            Text(
                "No receipt image attached.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Button(onClick = onDelete, modifier = Modifier.fillMaxWidth()) {
            Text("Delete record")
        }
    }
}
