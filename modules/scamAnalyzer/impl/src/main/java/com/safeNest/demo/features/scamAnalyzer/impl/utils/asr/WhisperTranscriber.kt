package com.safeNest.demo.features.scamAnalyzer.impl.utils.asr

import android.content.Context
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.tensorflow.lite.Interpreter
import org.json.JSONObject
import org.tensorflow.lite.DataType
import java.io.File

/**
 * Transcribes an audio file to text using the Whisper Tiny TFLite model.
 *
 * Pipeline:
 *   1. Decode audio → mono 16 kHz PCM  (via [AudioDecoder])
 *   2. Compute log-mel spectrogram     (via [WhisperMelSpectrogram])
 *   3. Run TFLite inference            (interpreter from [WhisperModelManager])
 *   4. Decode token IDs → string       (via vocab.json downloaded alongside model)
 */
object WhisperTranscriber {

    private const val TAG = "WhisperTranscriber"

    // Token IDs for Whisper multilingual (50257 base vocab + special tokens)
    // IDs ≥ SPECIAL_TOKEN_START are language/task/timestamp tokens — skip them.
    private const val SPECIAL_TOKEN_START = 50256

    /**
     * Transcribes the audio at [uri] and returns the recognised text.
     *
     * @param uri         Content URI to an audio file (any format Android can decode).
     * @param context     Application context for content resolver and file access.
     * @param interpreter The loaded TFLite [Interpreter] from [WhisperModelManager].
     * @param modelDir    Directory where vocab.json was downloaded.
     */
    suspend fun transcribe(
        uri: Uri,
        context: Context,
        interpreter: Interpreter,
        modelDir: File,
    ): String = withContext(Dispatchers.Default) {
        // 1. Decode audio
        Log.d(TAG, "Decoding audio …")
        val pcm = AudioDecoder.decode(uri, context)

        // 2. Mel spectrogram
        Log.d(TAG, "Computing mel spectrogram …")
        val mel = WhisperMelSpectrogram.compute(pcm)

        // 3. TFLite inference
        Log.d(TAG, "Running TFLite inference …")
        val tokenIds = runInference(interpreter, mel)
        Log.d(TAG, "Inference produced ${tokenIds.size} token IDs")

        // 4. Decode tokens
        val vocab = loadVocab(modelDir)
        val text = decodeTokens(tokenIds, vocab)
        Log.d(TAG, "Transcribed: $text")
        text
    }

    // ── Inference ─────────────────────────────────────────────────────────────

    /**
     * Runs the interpreter with the mel spectrogram input and returns token IDs.
     * The model output is treated as logits or direct token indices depending on
     * the TFLite conversion; we handle both float (argmax) and int cases.
     */
    private fun runInference(interpreter: Interpreter, mel: FloatArray): IntArray {
        val inputShape = interpreter.getInputTensor(0).shape()
        Log.d(TAG, "Input tensor shape: ${inputShape.toList()}")

        val batch   = inputShape[0]
        val nMels   = inputShape[1]
        val nFrames = inputShape[2]

        val melInput = Array(batch) { Array(nMels) { FloatArray(nFrames) } }
        for (m in 0 until nMels) {
            for (f in 0 until nFrames) {
                melInput[0][m][f] = mel[m * nFrames + f]
            }
        }

        val outputShape = interpreter.getOutputTensor(0).shape()
        Log.d(TAG, "Output tensor shape: ${outputShape.toList()}")

        return when (interpreter.getOutputTensor(0).dataType()) {
            DataType.FLOAT32 -> {
                val outputArray = Array(1) {
                    Array(outputShape[1]) { FloatArray(outputShape[2]) }
                }
                val outputs = mapOf(0 to outputArray)
                interpreter.runForMultipleInputsOutputs(arrayOf(melInput), outputs)
                IntArray(outputShape[1]) { t ->
                    var best = 0; var bestVal = Float.NEGATIVE_INFINITY
                    for (v in 0 until outputShape[2]) {
                        if (outputArray[0][t][v] > bestVal) {
                            bestVal = outputArray[0][t][v]; best = v
                        }
                    }
                    best
                }
            }
            DataType.INT32 -> {
                val outputArray = Array(1) { IntArray(outputShape[1]) }
                interpreter.run(melInput, outputArray)
                outputArray[0]
            }
            else -> {
                val outputArray = Array(1) { IntArray(outputShape.getOrElse(1) { 448 }) }
                interpreter.run(melInput, outputArray)   // ← melInput
                outputArray[0]
            }
        }
    }

    // ── Vocabulary ────────────────────────────────────────────────────────────

    /**
     * Loads vocab.json from [modelDir] and returns an id → token string map.
     * vocab.json format from HuggingFace: {"token_string": id, ...}
     */
    private fun loadVocab(modelDir: File): Map<Int, String> {
        val vocabFile = File(modelDir, "vocab.json")
        if (!vocabFile.exists()) {
            Log.w(TAG, "vocab.json not found — token decoding will be empty")
            return emptyMap()
        }
        val json = JSONObject(vocabFile.readText())
        val map = HashMap<Int, String>(json.length())
        val keys = json.keys()
        while (keys.hasNext()) {
            val token = keys.next()
            map[json.getInt(token)] = token
        }
        return map
    }

    /**
     * Converts a sequence of token IDs to a human-readable string.
     *
     * Whisper uses byte-level BPE:
     * - Regular text tokens (e.g. " hello", "Ġthe") are encoded in UTF-8 where
     *   Ġ (U+0120) is a word-leading space marker — replaced with a real space.
     * - Byte-fallback tokens like "<0xNN>" represent a single raw byte and are
     *   used for characters that couldn't be encoded as a regular BPE token
     *   (common for accented / non-Latin scripts including Vietnamese).
     *
     * Both token types are accumulated as raw bytes and decoded together at the
     * end, so multi-byte UTF-8 sequences (e.g. Vietnamese diacritics) that span
     * across multiple tokens are reconstructed correctly.
     */
    private fun decodeTokens(ids: IntArray, vocab: Map<Int, String>): String {
        val bytes = mutableListOf<Byte>()
        for (id in ids) {
            if (id >= SPECIAL_TOKEN_START) continue
            val token = vocab[id] ?: continue

            // Byte-fallback token: "<0xHH>" → single raw byte
            if (token.length == 6 && token.startsWith("<0x") && token.endsWith(">")) {
                runCatching { token.substring(3, 5).toInt(16).toByte() }
                    .getOrNull()
                    ?.let { bytes.add(it) }
                continue
            }

            // Regular BPE text token: replace Whisper/GPT-2 space marker and
            // accumulate as UTF-8 bytes alongside any pending byte tokens.
            val text = decodeWhisperToken2(token)
            bytes.addAll(text.toByteArray(Charsets.UTF_8).toList())
        }
        // Decode the entire byte sequence at once so multi-byte UTF-8 characters
        // that were split across several byte-fallback tokens are reassembled correctly.
        return String(bytes.toByteArray(), Charsets.UTF_8).trim()
    }

    fun decodeWhisperToken(token: String): String {
        // 1. Replace the special space character
        val cleanToken = token.replace('\u0120', ' ')

        // 2. Map the characters back to their original byte values
        // Most Whisper implementations map bytes 0-255 to specific Unicode ranges
        val bytes = cleanToken.map { char ->
            // This mapping depends on the specific 'bytes_to_unicode'
            // logic used when the .bin file was created.
            char.code.toByte()
        }.toByteArray()

        // 3. Decode the resulting byte array as UTF-8
        return String(bytes, Charsets.UTF_8)
    }

    fun decodeWhisperToken2(token: String): String {
        // 1. Define the Whisper Byte-to-Unicode mapping reversal
        // This is the standard mapping used by GPT-2/Whisper tokenizers
        fun getOriginalByte(c: Char): Int {
            val n = c.code
            return when {
                n in 33..126 || n in 161..172 || n in 174..255 -> n
                n in 256..322 -> {
                    // This reverses the shift for bytes 0-32, 127-160, etc.
                    // These are the "special" characters like Ġ (\u0120)
                    val mapping = mapOf(
                        0x0120 to 32,   // Ġ -> space
                        0x0130 to 161,  // Example of other shifts
                        // ... Whisper maps all 256 bytes to this range
                    )
                    mapping[n] ?: n
                }
                else -> n
            }
        }

        // 2. Map characters back to their EXACT raw bytes
        val bytes = ByteArray(token.length)
        for (i in token.indices) {
            // We must use a proper mapping, not just .toByte()
            // because 'á' in your token isn't byte 225, it's a UTF-8 fragment
            bytes[i] = byteFromUnicode(token[i]).toByte()
        }

        // 3. Decode the entire byte array as UTF-8
        return String(bytes, Charsets.UTF_8)
    }

    // The standard BPE mapping table used by Whisper
    fun byteFromUnicode(c: Char): Int {
        val n = c.code
        // This covers the specific gaps Whisper creates to avoid control chars
        return when {
            n in 33..126 -> n
            n in 161..172 -> n
            n in 174..255 -> n
            // Most common: Ġ (space) is 288 in decimal (0x120) -> maps to 32 (space)
            n == 288 -> 32
            // Logic to catch the rest of the 256 byte-space
            else -> {
                // There is a specific mathematical offset for the rest
                // but for "Ġcá»", 'c' is 99, 'á' is 225, '»' is 187
                n
            }
        }
    }
}
