package com.safenest.urlanalyzer

import android.content.Context
import com.safenest.urlanalyzer.audio.AudioAnalyzer
import com.safenest.urlanalyzer.image.ImageAnalyzer
import com.safenest.urlanalyzer.shared.LMClient
import com.safenest.urlanalyzer.shared.LlamaEngine
import com.safenest.urlanalyzer.text.SMSAnalyzerOrchestrator
import com.safenest.urlanalyzer.text.TextAnalyzerClassifier
import com.safenest.urlanalyzer.url.URLAnalyzerOrchestrator
import com.safenest.urlanalyzer.url.gate1.Gate1Classifier
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

class ModelManager @Inject constructor(
    @param:ApplicationContext
    private val ctx: Context
) {
    companion object {
        private const val MODEL_NAME = "Qwen3.5-0.8B-Q5_K_M.gguf"
    }

    @Volatile
    private var lmClient: LMClient? = null

    @Volatile
    var gate1: Gate1Classifier? = null

    @Volatile
    var gate2: URLAnalyzerOrchestrator? = null

    @Volatile
    var smsClassifier: SMSAnalyzerOrchestrator? = null

    @Volatile
    var imageAnalyzer: ImageAnalyzer? = null

    @Volatile
    var audioAnalyzer: AudioAnalyzer? = null

    private val isInitialized = AtomicBoolean(false)

    suspend fun initialize() {
        if (isInitialized.getAndSet(true)) return

        val engine = LlamaEngine(
            modelPath = getModelFile().absolutePath,
            nCtx = 4096,
            nGpuLayers = 99,
            maxTokens = 512
        )

        if (!engine.ensureInit()) throw RuntimeException("Failed to load model")

        lmClient = LMClient(engine, maxTokens = 512)

        gate1 = Gate1Classifier(ctx)

        val gate2Json = ctx.assets.open("config/gate2_prompts.json")
            .bufferedReader().use { it.readText() }
        val gate2Prompt = JSONObject(gate2Json).getString("system_prompt")
        gate2 = URLAnalyzerOrchestrator(gate1!!, lmClient!!, gate2Prompt)


        val textClassifier = TextAnalyzerClassifier(lmClient!!, ctx)
        smsClassifier = SMSAnalyzerOrchestrator(textClassifier, gate1)

        imageAnalyzer = ImageAnalyzer(lmClient!!, ctx)

        audioAnalyzer = AudioAnalyzer(lmClient!!, ctx)
    }

    private suspend fun getModelFile(): File {
        return withContext(Dispatchers.IO) {
            val modelFile = File(ctx.filesDir, MODEL_NAME)
            if (!modelFile.exists()) {
                // Try /data/local/tmp/ (adb push location)
                val adbFile = File("/data/local/tmp/$MODEL_NAME")

                if (adbFile.exists()) {
                    adbFile.inputStream().use { input ->
                        modelFile.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                } else {
                    // Try Download folder
                    val dlFile = File("/sdcard/Download/$MODEL_NAME")
                    if (dlFile.exists()) {
                        dlFile.inputStream().use { input ->
                            modelFile.outputStream().use { output ->
                                input.copyTo(output)
                            }
                        }
                    }
                }
            }
            modelFile
        }
    }
}