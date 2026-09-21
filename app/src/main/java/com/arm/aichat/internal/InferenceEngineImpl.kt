package com.arm.aichat.internal

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.File

/** Kotlin binding for the llama.cpp Android JNI sample used by PocketPilot. */
class InferenceEngineImpl private constructor(context: Context) {
    private val nativeLibDir = context.applicationInfo.nativeLibraryDir
    @Volatile private var ready = false

    private external fun init(nativeLibDir: String)
    private external fun load(modelPath: String): Int
    private external fun prepare(): Int
    private external fun processSystemPrompt(systemPrompt: String): Int
    private external fun processUserPrompt(userPrompt: String, predictLength: Int): Int
    private external fun generateNextToken(): String?
    private external fun unload()

    init {
        System.loadLibrary("ai-chat")
        init(nativeLibDir)
    }

    suspend fun loadModel(path: String) = withContext(Dispatchers.IO) {
        require(File(path).isFile) { "The selected model file could not be read." }
        check(load(path) == 0) { "llama.cpp could not load this GGUF. Select Qwen3-1.7B Q4_K_M." }
        check(prepare() == 0) { "Unable to prepare Qwen3 inference." }
        ready = true
    }

    suspend fun setSystemPrompt(prompt: String) = withContext(Dispatchers.IO) {
        check(ready) { "Select a Qwen3 GGUF model first." }
        check(processSystemPrompt(prompt) == 0) { "Unable to prepare the Qwen3 prompt." }
    }

    fun generate(prompt: String, maxTokens: Int = 220): Flow<String> = flow {
        check(ready) { "Select a Qwen3 GGUF model first." }
        check(processUserPrompt(prompt, maxTokens) == 0) { "Qwen3 rejected the question." }
        while (true) emit(generateNextToken() ?: break)
    }.flowOn(Dispatchers.IO)

    fun closeModel() {
        if (ready) {
            unload()
            ready = false
        }
    }

    companion object {
        fun create(context: Context) = InferenceEngineImpl(context.applicationContext)
    }
}
