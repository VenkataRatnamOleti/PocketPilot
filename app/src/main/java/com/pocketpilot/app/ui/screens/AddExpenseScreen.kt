package com.pocketpilot.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen(
    onBack: () -> Unit,
    onExpenseAdded: (
        amount: Double,
        category: String,
        description: String
    ) -> Unit
) {

    var amount by remember {
        mutableStateOf("")
    }

    var category by remember {
        mutableStateOf("")
    }

    var description by remember {
        mutableStateOf("")
    }

    var showAmountError by remember {
        mutableStateOf(false)
    }

    Surface(
        modifier = Modifier.fillMaxSize()
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),

            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                TextButton(
                    onClick = onBack
                ) {
                    Text("← Back")
                }

                Spacer(
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = "Add Expense",
                    style = MaterialTheme.typography.titleLarge
                )
            }

            Spacer(
                modifier = Modifier.height(10.dp)
            )

            OutlinedTextField(
                value = amount,

                onValueChange = { input ->

                    // Allow only digits and one decimal point
                    val isValid =
                        input.all { it.isDigit() || it == '.' } &&
                                input.count { it == '.' } <= 1

                    if (isValid) {
                        amount = input
                        showAmountError = false
                    }
                },

                modifier = Modifier.fillMaxWidth(),

                label = {
                    Text("Amount (₹)")
                },

                isError = showAmountError,

                singleLine = true,

                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal
                )
            )

            if (showAmountError) {

                Text(
                    text = "Enter an amount greater than 0",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            OutlinedTextField(
                value = category,

                onValueChange = { newValue ->
                    category = newValue
                },

                modifier = Modifier.fillMaxWidth(),

                label = {
                    Text("Category")
                },

                singleLine = true
            )

            OutlinedTextField(
                value = description,

                onValueChange = { newValue ->
                    description = newValue
                },

                modifier = Modifier.fillMaxWidth(),

                label = {
                    Text("Description")
                },

                singleLine = true
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Button(
                onClick = {

                    val parsedAmount =
                        amount.toDoubleOrNull()

                    if (parsedAmount != null && parsedAmount > 0) {

                        val finalCategory =
                            if (category.isBlank()) {
                                "Other"
                            } else {
                                category.trim()
                            }

                        val finalDescription =
                            if (description.isBlank()) {
                                finalCategory
                            } else {
                                description.trim()
                            }

                        onExpenseAdded(
                            parsedAmount,
                            finalCategory,
                            finalDescription
                        )

                        onBack()

                    } else {

                        showAmountError = true
                    }
                },

                modifier = Modifier.fillMaxWidth()
            ) {

                Text("Save Expense")
            }
        }
    }
}
