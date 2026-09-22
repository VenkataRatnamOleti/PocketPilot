package com.pocketpilot.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pocketpilot.app.ui.util.formatInr
import com.pocketpilot.app.viewmodel.Loan

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PeopleScreen(
    loans: List<Loan>,
    onAddLoan: (String, String, Double, String) -> Unit,
    onDeleteLoan: (Int) -> Unit,
    onSettleLoan: (Int, Boolean) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("Borrowed from") }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(modifier = Modifier.fillMaxWidth()) {
                Text("People", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.weight(1f))
                Button(onClick = { showDialog = true }) { Text("Add") }
            }
            Text(
                "Track who owes you and whom you owe, with dates and notes.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        items(loans, key = { it.id }) { loan ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(loan.personName, style = MaterialTheme.typography.titleMedium)
                    Text("${loan.type}: ${formatInr(loan.amount)}")
                    if (loan.phoneOrNote.isNotBlank()) Text(loan.phoneOrNote)
                    Text("Recorded: ${java.text.DateFormat.getDateInstance().format(java.util.Date(loan.date))}")
                    Row {
                        TextButton(onClick = { onSettleLoan(loan.id, !loan.isSettled) }) {
                            Text(if (loan.isSettled) "Mark open" else "Mark settled")
                        }
                        TextButton(onClick = { onDeleteLoan(loan.id) }) { Text("Delete") }
                    }
                }
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Add borrow or loan") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(name, { name = it }, label = { Text("Person") }, singleLine = true)
                    OutlinedTextField(amount, { amount = it }, label = { Text("Amount (₹)") }, singleLine = true)
                    OutlinedTextField(type, { type = it }, label = { Text("Type") }, singleLine = true)
                    OutlinedTextField(note, { note = it }, label = { Text("Who / note") })
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val parsed = amount.toDoubleOrNull()
                    if (name.isNotBlank() && parsed != null && parsed > 0) {
                        onAddLoan(name.trim(), note.trim(), parsed, type.trim().ifBlank { "Borrowed from" })
                        showDialog = false
                        name = ""; note = ""; amount = ""
                    }
                }) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { showDialog = false }) { Text("Cancel") } }
        )
    }
}
