package net.qualgo.safeNest.features.phishingDetection.impl.presentation.asr

import kotlin.math.cos
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.PI

/**
 * Computes the log-mel spectrogram expected by whisper-tiny.tflite.
 *
 * Parameters match OpenAI Whisper exactly:
 *   sample rate  = 16 000 Hz
 *   n_fft        = 400  (25 ms window)
 *   hop_length   = 160  (10 ms hop)
 *   n_mels       = 80
 *   n_frames     = 3000 (30 s of audio)
 *
 * Output shape:  FloatArray of size 1 × 80 × 3000  (row-major, CHW)
 */
object WhisperMelSpectrogram {

    // ── Whisper constants ────────────────────────────────────────────────────
    const val SAMPLE_RATE = 16_000
    const val N_FFT = 400
    const val FFT_SIZE = 512
    const val HOP_LENGTH = 160
    const val N_MELS = 80
    const val N_FRAMES = 3000

    // ── Pre-computed tables (computed once, then cached) ─────────────────────
    private val hannWindow: FloatArray by lazy { buildHannWindow(N_FFT) }
    private val melFilters: Array<FloatArray> by lazy { buildMelFilters(SAMPLE_RATE, N_FFT, N_MELS) }

    /**
     * Returns a [FloatArray] of shape [1 × N_MELS × N_FRAMES] ready to pass
     * into the TFLite Interpreter as the input tensor.
     */
    fun compute(pcm: ShortArray): FloatArray {
        require(pcm.size == AudioDecoder.MAX_SAMPLES) {
            "Expected exactly ${AudioDecoder.MAX_SAMPLES} samples, got ${pcm.size}"
        }

        // Convert to float in [-1, 1]
        val samples = FloatArray(pcm.size) { pcm[it] / 32768f }

        // ── STFT → magnitude spectrum ──────────────────────────────────────
        // We compute (N_FRAMES) frames.  Frame i starts at i * HOP_LENGTH.
        // Each frame is windowed with Hann, zero-padded to N_FFT if needed.
        val magnitudes = Array(N_FRAMES) { frame ->
            val start = frame * HOP_LENGTH
            stftMagnitude(samples, start)
        }

        // ── Mel filterbank ────────────────────────────────────────────────
        val melSpec = Array(N_MELS) { m ->
            FloatArray(N_FRAMES) { f ->
                var energy = 0f
                for (k in melFilters[m].indices) {
                    energy += melFilters[m][k] * magnitudes[f][k]
                }
                energy
            }
        }

        // ── Log-mel + normalise (Whisper style) ────────────────────────────
        // log10(max(mel, 1e-10))
        // clamp to (max - 8)
        // scale to [-1, 1]: (mel + 4) / 4
        var maxVal = Float.NEGATIVE_INFINITY
        for (m in 0 until N_MELS) {
            for (f in 0 until N_FRAMES) {
                val v = log10(max(melSpec[m][f], 1e-10f))
                melSpec[m][f] = v
                if (v > maxVal) maxVal = v
            }
        }

        val output = FloatArray(1 * N_MELS * N_FRAMES)
        for (m in 0 until N_MELS) {
            for (f in 0 until N_FRAMES) {
                val clamped = max(melSpec[m][f], maxVal - 8f)
                output[m * N_FRAMES + f] = (clamped + 4f) / 4f
            }
        }

        return output
    }

    // ── STFT helpers ─────────────────────────────────────────────────────────

    /**
     * Computes the power magnitude spectrum of one frame starting at [startSample].
     * Returns an array of size (N_FFT/2 + 1) = 201.
     */
    private fun stftMagnitude(samples: FloatArray, startSample: Int): FloatArray {
        val frame = FloatArray(N_FFT)
        for (i in 0 until N_FFT) {
            val idx = startSample + i
            val s = if (idx < samples.size) samples[idx] else 0f
            frame[i] = s * hannWindow[i]
        }

        // Zero-pad to FFT_SIZE = 512 (required for power-of-2 FFT)
        val re = frame.copyOf(FFT_SIZE)   // ← pads to 512 with zeros
        val im = FloatArray(FFT_SIZE)
        fftReal(re, im, FFT_SIZE)         // ← uses 512

        // Return (FFT_SIZE/2 + 1) = 257 bins
        val freqBins = FFT_SIZE / 2 + 1
        return FloatArray(freqBins) { k ->
            val r = re[k]; val i = im[k]
            r * r + i * i
        }
    }

    /** Iterative Cooley-Tukey FFT; modifies [re] and [im] in place. */
    private fun fftReal(re: FloatArray, im: FloatArray, n: Int) {
        // Bit-reversal permutation
        var j = 0
        for (i in 1 until n) {
            var bit = n shr 1
            while (j and bit != 0) {
                j = j xor bit
                bit = bit shr 1
            }
            j = j xor bit
            if (i < j) {
                var tmp = re[i]; re[i] = re[j]; re[j] = tmp
                tmp = im[i]; im[i] = im[j]; im[j] = tmp
            }
        }
        // FFT butterfly
        var len = 2
        while (len <= n) {
            val ang = -2.0 * PI / len
            val wRe = cos(ang).toFloat()
            val wIm = sin(ang).toFloat()
            var i = 0
            while (i < n) {
                var curRe = 1f; var curIm = 0f
                for (k in 0 until len / 2) {
                    val uRe = re[i + k];       val uIm = im[i + k]
                    val vRe = re[i + k + len/2]; val vIm = im[i + k + len/2]
                    val tRe = curRe * vRe - curIm * vIm
                    val tIm = curRe * vIm + curIm * vRe
                    re[i + k]         = uRe + tRe; im[i + k]         = uIm + tIm
                    re[i + k + len/2] = uRe - tRe; im[i + k + len/2] = uIm - tIm
                    val newRe = curRe * wRe - curIm * wIm
                    curIm = curRe * wIm + curIm * wRe
                    curRe = newRe
                }
                i += len
            }
            len = len shl 1
        }
    }

    private fun sin(x: Double) = kotlin.math.sin(x).toFloat()

    // ── Window & filter builders ──────────────────────────────────────────────

    private fun buildHannWindow(size: Int): FloatArray =
        FloatArray(size) { n ->
            (0.5 * (1 - cos(2.0 * PI * n / size))).toFloat()
        }

    /**
     * Builds an [nMels] × (nFft/2+1) mel filterbank matrix.
     * Mel scale: 2595 * log10(1 + f/700) — same formula as librosa.
     */
    private fun buildMelFilters(
        sampleRate: Int,
        nFft: Int,
        nMels: Int,
    ): Array<FloatArray> {
        val freqBins = FFT_SIZE / 2 + 1
        val fMin = 0.0
        val fMax = sampleRate / 2.0

        fun hzToMel(hz: Double) = 2595.0 * log10(1.0 + hz / 700.0)
        fun melToHz(mel: Double) = 700.0 * (10.0.pow(mel / 2595.0) - 1.0)

        val melMin = hzToMel(fMin)
        val melMax = hzToMel(fMax)

        // (nMels + 2) equally spaced mel points → convert back to Hz
        val melPoints = DoubleArray(nMels + 2) { i ->
            melToHz(melMin + i * (melMax - melMin) / (nMels + 1))
        }

        // Map Hz → FFT bin index
        val fftFreqs = DoubleArray(freqBins) { k -> k * sampleRate.toDouble() / FFT_SIZE }

        return Array(nMels) { m ->
            FloatArray(freqBins) { k ->
                val f = fftFreqs[k]
                val left  = melPoints[m]
                val center = melPoints[m + 1]
                val right = melPoints[m + 2]
                when {
                    f < left  || f > right -> 0f
                    f <= center -> ((f - left) / (center - left)).toFloat()
                    else        -> ((right - f) / (right - center)).toFloat()
                }
            }
        }
    }

    private fun Double.pow(exp: Double) = Math.pow(this, exp)
    private fun floor(x: Double) = kotlin.math.floor(x)
}
