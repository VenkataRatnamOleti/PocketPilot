package com.pocketpilot.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import com.pocketpilot.app.domain.Qwen3Assistant
import kotlinx.coroutines.launch
import com.pocketpilot.app.domain.FinancialEngine
import com.pocketpilot.app.ui.util.formatInr
import com.pocketpilot.app.viewmodel.Expense
import java.util.Locale

private data class ChatMessage(val text: String, val fromUser: Boolean, val expense: Expense? = null)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AskPocketPilotScreen(availableToSpend: Double, upcomingExpenses: Double, expenses: List<Expense>, onBack: () -> Unit, onAddExpense: (Expense) -> Unit) {
    var text by remember { mutableStateOf("") }
    var modelStatus by remember { mutableStateOf("No Qwen3 model selected") }
    var downloadProgress by remember { mutableStateOf<Float?>(null) }
    var downloadError by remember { mutableStateOf<String?>(null) }
    var generating by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val assistant = remember { Qwen3Assistant(context) }
    val scope = rememberCoroutineScope()
    val messages = remember { mutableStateListOf(ChatMessage("Hi, I’m PocketPilot. Ask about a purchase, spending, or send an expense into your plan.", false)) }
    fun reply(question: String): String { val amount=Regex("(?:₹|rs\\.?\\s*)?\\s*(\\d+(?:\\.\\d{1,2})?)",RegexOption.IGNORE_CASE).find(question)?.groupValues?.getOrNull(1)?.toDoubleOrNull(); val lower=question.lowercase(Locale.getDefault()); return when { amount != null && listOf("afford","buy","purchase","what if").any(lower::contains) -> FinancialEngine.analyzePurchase(amount,availableToSpend,upcomingExpenses).let { "${it.message}\n\n${it.recommendation}" }; lower.contains("food") -> FinancialEngine.foodInsight(expenses); else -> "Your verified balance is ${formatInr(availableToSpend)} after ${formatInr(upcomingExpenses)} of upcoming commitments. Qwen3 local-model responses will use this same verified context." } }
    val modelPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri -> if (uri != null) scope.launch { modelStatus = "Loading Qwen3 model…"; try { modelStatus = assistant.importAndLoad(uri) } catch (error: Exception) { modelStatus = error.message ?: "Could not load model." } } }
    fun downloadModel() {
        if (downloadProgress != null) return
        scope.launch {
            downloadError = null
            downloadProgress = 0f
            modelStatus = "Downloading Qwen3 model…"
            try {
                modelStatus = assistant.downloadAndLoad { downloaded, total ->
                    downloadProgress = if (total > 0) downloaded.toFloat() / total else -1f
                }
            } catch (error: Exception) {
                downloadError = error.message ?: "Could not download the Qwen3 model."
                modelStatus = "Qwen3 model is not ready"
            } finally {
                downloadProgress = null
            }
        }
    }
    Scaffold(topBar={TopAppBar(title={Text("Ask PocketPilot")},navigationIcon={TextButton(onClick=onBack){Text("Back")}},actions={TextButton(onClick={modelPicker.launch("application/octet-stream")}){Text("Model")}})}) { padding -> Column(Modifier.fillMaxSize().padding(padding)) {
        Text(modelStatus, Modifier.padding(horizontal=16.dp, vertical=6.dp), style=MaterialTheme.typography.bodySmall, color=MaterialTheme.colorScheme.onSurfaceVariant)
        downloadProgress?.let { progress ->
            if (progress < 0) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp))
            } else {
                LinearProgressIndicator(progress = progress, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp))
                Text("${(progress * 100).toInt()}% downloaded", Modifier.padding(horizontal = 16.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall)
            }
        }
        downloadError?.let { error ->
            Text(error, Modifier.padding(horizontal = 16.dp, vertical = 4.dp), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }
        Row(Modifier.padding(horizontal = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(enabled = downloadProgress == null, onClick = ::downloadModel) { Text("Download Qwen3") }
            OutlinedButton(enabled = downloadProgress == null, onClick = { modelPicker.launch("application/octet-stream") }) { Text("Choose file") }
        }
        LazyColumn(Modifier.weight(1f).fillMaxWidth().padding(16.dp),verticalArrangement=Arrangement.spacedBy(10.dp)) { items(messages) { message -> Column(Modifier.fillMaxWidth(),horizontalAlignment=if(message.fromUser) Alignment.End else Alignment.Start) { Card(colors=CardDefaults.cardColors(containerColor=if(message.fromUser) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant),modifier=Modifier.widthIn(max=320.dp)){Text(message.text,Modifier.padding(12.dp))}; message.expense?.let { expense -> TextButton(onClick={onAddExpense(expense)}){Text("Add ${expense.description} to expenses")}} } } }
        Row(Modifier.fillMaxWidth().padding(12.dp),verticalAlignment=Alignment.CenterVertically) { OutlinedTextField(text,{text=it},Modifier.weight(1f),placeholder={Text("Can I afford ₹1,800?")},singleLine=true); Spacer(Modifier.width(8.dp)); Button(enabled=!generating,onClick={if(text.isNotBlank()){ val question=text; messages+=ChatMessage(question,true); text=""; if(assistant.modelName == null) messages+=ChatMessage(reply(question),false) else scope.launch { generating=true; val response=StringBuilder(); try { assistant.answer(question,availableToSpend,upcomingExpenses,expenses).collect { token -> response.append(token); if(messages.lastOrNull()?.fromUser == false) messages[messages.lastIndex]=ChatMessage(response.toString(),false) else messages+=ChatMessage(response.toString(),false) } } catch(error:Exception) { messages+=ChatMessage(error.message ?: "Qwen3 could not respond.",false) } finally { generating=false } } }}){Text(if(generating) "…" else "Send")} }
        if(expenses.isNotEmpty()) TextButton(onClick={messages+=ChatMessage("Review this recent transaction:",false,expenses.first())},Modifier.padding(start=12.dp,bottom=8.dp)){Text("Review latest expense")}
    } }
}
