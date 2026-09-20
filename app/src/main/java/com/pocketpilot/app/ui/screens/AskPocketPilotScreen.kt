package com.pocketpilot.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.pocketpilot.app.ui.util.formatInr
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AskPocketPilotScreen(
    availableToSpend: Double,
    onBack: () -> Unit
) {

    var amount by remember {
        mutableStateOf("")
    }

    var result by remember {
        mutableStateOf<String?>(null)
    }

    var impact by remember {
        mutableStateOf(0.0)
    }

    Scaffold(

        topBar = {

            TopAppBar(

                title = {
                    Text("Ask PocketPilot")
                },

                navigationIcon = {

                    IconButton(
                        onClick = onBack
                    ) {

                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }

    ) { paddingValues ->

        Column(

            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),

            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Text(
                text = "Can I afford this?",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Enter the amount of the purchase and PocketPilot will check it against your current discretionary budget."
            )

            Card(
                modifier = Modifier.fillMaxWidth(),

                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {

                Column(
                    modifier = Modifier.padding(18.dp)
                ) {

                    Text(
                        text = "Available to spend"
                    )

                    Text(
                        text = formatInr(availableToSpend),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            OutlinedTextField(
                value = amount,

                onValueChange = { input ->

                    // Allow only digits and one decimal point
                    val isValid =
                        input.all { it.isDigit() || it == '.' } &&
                                input.count { it == '.' } <= 1

                    if (isValid) {
                        amount = input

                        // The old answer no longer matches the amount being typed
                        result = null
                    }
                },

                modifier = Modifier.fillMaxWidth(),

                label = {
                    Text("Purchase amount (₹)")
                },

                singleLine = true,

                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal
                )
            )

            Button(

                onClick = {

                    val purchaseAmount =
                        amount.toDoubleOrNull() ?: 0.0

                    if (purchaseAmount > 0) {

                        impact =
                            if (availableToSpend > 0) {
                                (purchaseAmount / availableToSpend) * 100
                            } else {
                                100.0
                            }

                        result =
                            if (purchaseAmount <= availableToSpend) {

                                "You can afford this purchase based on your current budget. You would have approximately ${
                                    formatInr(availableToSpend - purchaseAmount)
                                } left."

                            } else {

                                "This purchase would exceed your current discretionary budget by ${
                                    formatInr(purchaseAmount - availableToSpend)
                                }. Consider postponing it or reducing another expense."
                            }
                    }
                },

                modifier = Modifier.fillMaxWidth()
            ) {

                Text("Analyze Purchase")
            }

            result?.let { message ->

                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {

                    Column(
                        modifier = Modifier.padding(18.dp)
                    ) {

                        Text(
                            text = "PocketPilot says",
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(
                            modifier = Modifier.height(8.dp)
                        )

                        Text(message)

                        Spacer(
                            modifier = Modifier.height(12.dp)
                        )

                        Text(
                            text = "Budget impact: ${
                                String.format(
                                    Locale.US,
                                    "%.1f",
                                    impact
                                )
                            }%",
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}
