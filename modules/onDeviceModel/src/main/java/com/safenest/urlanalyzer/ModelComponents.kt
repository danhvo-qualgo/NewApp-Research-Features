package com.safenest.urlanalyzer

import com.safenest.urlanalyzer.audio.AudioAnalyzer
import com.safenest.urlanalyzer.image.ImageAnalyzer
import com.safenest.urlanalyzer.shared.LMClient
import com.safenest.urlanalyzer.shared.LlamaEngine
import com.safenest.urlanalyzer.text.SMSAnalyzerOrchestrator
import com.safenest.urlanalyzer.url.URLAnalyzerOrchestrator
import com.safenest.urlanalyzer.url.gate1.Gate1Classifier

data class ModelComponents(
    val engine: LlamaEngine,
    val lmClient: LMClient,
    val gate1: Gate1Classifier,
    val gate2: URLAnalyzerOrchestrator,
    val smsClassifier: SMSAnalyzerOrchestrator,
    val imageAnalyzer: ImageAnalyzer,
    val audioAnalyzer: AudioAnalyzer,
)

data class ModelConfig(
    val modelName: String = "Qwen3.5-0.8B-Q5_K_M.gguf",
    val downloadUrl: String = "https://huggingface.co/bartowski/Qwen_Qwen3.5-0.8B-GGUF/resolve/main/Qwen_Qwen3.5-0.8B-Q5_K_M.gguf",
    val nCtx: Int = 4096,
    val nGpuLayers: Int = 99,
    val maxTokens: Int = 512,
)

sealed interface ModelState {
    data object Uninitialized : ModelState

    data class Loading(val progress: Int) : ModelState

    data class Ready(val components: ModelComponents) : ModelState

    data class Error(val cause: Throwable) : ModelState
}