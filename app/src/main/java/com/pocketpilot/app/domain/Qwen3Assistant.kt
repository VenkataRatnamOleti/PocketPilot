package com.pocketpilot.app.domain

import android.content.Context
import android.net.Uri
import com.arm.aichat.internal.InferenceEngineImpl
import com.pocketpilot.app.viewmodel.Expense
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

/** Loads a user-selected Qwen3 GGUF and supplies it only verified financial facts. */
class Qwen3Assistant(private val context: Context) {
    private val engine by lazy { InferenceEngineImpl.create(context) }
    var modelName: String? = null; private set

    suspend fun downloadAndLoad(onProgress: suspend (downloadedBytes: Long, totalBytes: Long) -> Unit): String =
        withContext(Dispatchers.IO) {
        val modelsDirectory = File(context.filesDir, "models").apply { mkdirs() }
        val destination = File(modelsDirectory, "qwen3.gguf")
        val temporary = File(modelsDirectory, "qwen3.gguf.download")
        val connection = (URL(MODEL_URL).openConnection() as HttpURLConnection).apply {
            connectTimeout = 15_000
            readTimeout = 30_000
            instanceFollowRedirects = true
            requestMethod = "GET"
        }
        try {
            check(connection.responseCode in 200..299) {
                "Model download failed (HTTP ${connection.responseCode})."
            }
            val totalBytes = connection.contentLengthLong
            var downloadedBytes = 0L
            connection.inputStream.use { input ->
                temporary.outputStream().use { output ->
                    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                    while (true) {
                        val read = input.read(buffer)
                        if (read < 0) break
                        output.write(buffer, 0, read)
                        downloadedBytes += read
                        withContext(Dispatchers.Main.immediate) {
                            onProgress(downloadedBytes, totalBytes)
                        }
                    }
                }
            }
            check(downloadedBytes > 0) { "The model download was empty." }
            if (destination.exists()) check(destination.delete()) { "Unable to replace the existing model." }
            check(temporary.renameTo(destination)) { "Unable to save the downloaded model." }
            engine.loadModel(destination.absolutePath)
            engine.setSystemPrompt(SYSTEM_PROMPT)
            modelName = "Qwen3 on-device"
            return@withContext modelName!!
        } finally {
            connection.disconnect()
            if (temporary.exists()) temporary.delete()
        }
    }

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
        private const val MODEL_URL =
            "https://huggingface.co/Qwen/Qwen3-1.7B-GGUF/resolve/main/Qwen3-1.7B-Q4_K_M.gguf"
        private const val SYSTEM_PROMPT = """You are PocketPilot AI, an on-device money-decision assistant for India. Use only the supplied verified financial facts. Never invent a balance, transaction, merchant, price or policy. Do not give investment, lending, medical, legal, or guaranteed financial advice. Explain affordability, trade-offs, and a practical next step in 100 words or fewer."""
    }
}
