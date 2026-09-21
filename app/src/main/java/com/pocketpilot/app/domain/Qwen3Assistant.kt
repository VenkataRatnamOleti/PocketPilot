package com.pocketpilot.app.domain

import android.content.Context
import android.net.Uri
import com.arm.aichat.internal.InferenceEngineImpl
import com.pocketpilot.app.viewmodel.Expense
import kotlinx.coroutines.flow.Flow
import java.io.File

/** Loads a user-selected Qwen3 GGUF and supplies it only verified financial facts. */
class Qwen3Assistant(private val context: Context) {
    private val engine by lazy { InferenceEngineImpl.create(context) }
    var modelName: String? = null; private set

    suspend fun importAndLoad(uri: Uri): String {
        val destination = File(context.filesDir, "models/qwen3.gguf").apply { parentFile?.mkdirs() }
        context.contentResolver.openInputStream(uri)?.use { input -> destination.outputStream().use { input.copyTo(it) } }
            ?: error("Unable to open the selected GGUF file.")
        engine.loadModel(destination.absolutePath)
        engine.setSystemPrompt(SYSTEM_PROMPT)
        modelName = "Qwen3 on-device"
        return modelName!!
    }

    fun answer(question: String, available: Double, upcoming: Double, expenses: List<Expense>): Flow<String> =
        engine.generate(buildString {
            append("Verified financial facts only:\n")
            append("available_to_spend=₹$available\nupcoming_commitments=₹$upcoming\n")
            append("expenses=")
            append(expenses.take(12).joinToString { "${it.category}:₹${it.amount}" })
            append("\nUser question: $question")
        })

    companion object {
        private const val SYSTEM_PROMPT = """You are PocketPilot AI, an on-device money-decision assistant for India. Use only the supplied verified financial facts. Never invent a balance, transaction, merchant, price or policy. Do not give investment, lending, medical, legal, or guaranteed financial advice. Explain affordability, trade-offs, and a practical next step in 100 words or fewer."""
    }
}
