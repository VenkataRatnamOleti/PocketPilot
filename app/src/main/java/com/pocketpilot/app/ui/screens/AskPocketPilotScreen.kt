package com.pocketpilot.app.ui.screens

import android.content.ActivityNotFoundException
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.unit.dp
import com.pocketpilot.app.domain.FinancialEngine
import com.pocketpilot.app.ui.util.formatInr
import com.pocketpilot.app.viewmodel.Expense
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AskPocketPilotScreen(
    availableToSpend: Double,
    upcomingExpenses: Double,
    expenses: List<Expense>,
    onBack: () -> Unit
) {
    var question by remember {
        mutableStateOf("")
    }

    var answer by remember {
        mutableStateOf<String?>(null)
    }

    var voiceUnavailable by remember {
        mutableStateOf(false)
    }

    val voice = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->

        val words = result.data
            ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            ?.firstOrNull()

        if (words != null) {
            question = words
        }
    }

    fun ask() {
        val amount = Regex(
            """(?:₹|rs\.?\s*)?\s*(\d+(?:\.\d{1,2})?)""",
            RegexOption.IGNORE_CASE
        )
            .find(question)
            ?.groupValues
            ?.getOrNull(1)
            ?.toDoubleOrNull()

        val lower = question.lowercase(Locale.getDefault())

        answer = when {

            amount != null &&
                    (
                            lower.contains("afford") ||
                                    lower.contains("buy") ||
                                    lower.contains("purchase") ||
                                    lower.contains("what if")
                            ) -> {

                val analysis = FinancialEngine.analyzePurchase(
                    amount,
                    availableToSpend,
                    upcomingExpenses
                )

                "${analysis.message}\n\n" +
                        "${analysis.recommendation}\n\n" +
                        "Budget impact: ${
                            String.format(
                                Locale.US,
                                "%.1f",
                                analysis.impactPercent
                            )
                        }%."
            }

            lower.contains("food") -> {
                FinancialEngine.foodInsight(expenses)
            }

            lower.contains("spend") ||
                    lower.contains("expense") -> {

                "You have spent ${
                    formatInr(expenses.sumOf { it.amount })
                } so far. ${
                    formatInr(availableToSpend)
                } remains after planned commitments."
            }

            else -> {
                "Try asking: “Can I afford ₹1,800?”, " +
                        "“How much did I spend on food?”, or " +
                        "“What if I buy this for ₹5,000?”"
            }
        }
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
                text = "Your money decision co-pilot",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor =
                    MaterialTheme.colorScheme.primaryContainer
                )
            ) {

                Column(
                    modifier = Modifier.padding(18.dp)
                ) {

                    Text("Available to spend")

                    Text(
                        text = formatInr(availableToSpend),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            OutlinedTextField(
                value = question,

                onValueChange = {
                    question = it
                    answer = null
                },

                modifier = Modifier.fillMaxWidth(),

                label = {
                    Text("Ask a question")
                },

                placeholder = {
                    Text("Can I afford ₹1,800?")
                },

                singleLine = false,
                maxLines = 3,

                trailingIcon = {

                    IconButton(
                        onClick = {

                            try {

                                val intent = Intent(
                                    RecognizerIntent.ACTION_RECOGNIZE_SPEECH
                                )
                                    .putExtra(
                                        RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                                    )
                                    .putExtra(
                                        RecognizerIntent.EXTRA_PROMPT,
                                        "Ask PocketPilot about your budget"
                                    )

                                voice.launch(intent)

                            } catch (
                                _: ActivityNotFoundException
                            ) {

                                voiceUnavailable = true
                            }
                        }
                    ) {

                        Text("Voice")
                    }
                }
            )

            if (voiceUnavailable) {

                Text(
                    text = "Speech recognition is unavailable on this device. " +
                            "You can type your question instead.",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Button(
                onClick = {
                    ask()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = question.isNotBlank()
            ) {

                Text("Ask PocketPilot")
            }

            answer?.let { result ->

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

                        Text(result)
                    }
                }
            }

            Text(
                text = "PocketPilot provides budgeting guidance, " +
                        "not financial or investment advice.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
