package com.safeNest.demo.features.scamAnalyzer.impl.utils.asr

import android.content.Context
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.ShortBuffer

/**
 * Decodes an audio file URI to a mono 16 kHz PCM [ShortArray].
 *
 * Supports any format Android's MediaCodec can handle (WAV, MP3, AAC, M4A, OGG, FLAC, …).
 * The output is capped at [MAX_SAMPLES] samples (30 seconds at 16 kHz); longer files
 * are truncated.  Shorter files are zero-padded to exactly [MAX_SAMPLES].
 */
object AudioDecoder {

    private const val TAG = "AudioDecoder"

    const val SAMPLE_RATE = 16_000
    const val MAX_DURATION_SECONDS = 30
    const val MAX_SAMPLES = SAMPLE_RATE * MAX_DURATION_SECONDS // 480 000

    private const val TIMEOUT_US = 10_000L

    /**
     * Decodes [uri] and returns exactly [MAX_SAMPLES] mono 16 kHz PCM samples.
     * Throws [IllegalArgumentException] if no supported audio track is found.
     */
    suspend fun decode(uri: Uri, context: Context): ShortArray = withContext(Dispatchers.IO) {
        val extractor = MediaExtractor()
        extractor.setDataSource(context, uri, null)

        val trackIndex = selectAudioTrack(extractor)
            ?: throw IllegalArgumentException("No supported audio track found in $uri")

        extractor.selectTrack(trackIndex)
        val inputFormat = extractor.getTrackFormat(trackIndex)
        val mime = inputFormat.getString(MediaFormat.KEY_MIME) ?: ""
        val srcSampleRate = if (inputFormat.containsKey(MediaFormat.KEY_SAMPLE_RATE))
            inputFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE) else SAMPLE_RATE
        val srcChannels = if (inputFormat.containsKey(MediaFormat.KEY_CHANNEL_COUNT))
            inputFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT) else 1

        Log.d(TAG, "Decoding: mime=$mime srcSampleRate=$srcSampleRate srcChannels=$srcChannels")

        val codec = MediaCodec.createDecoderByType(mime)
        codec.configure(inputFormat, null, null, 0)
        codec.start()

        val rawSamples = mutableListOf<Short>()
        val bufferInfo = MediaCodec.BufferInfo()
        var inputDone = false
        var outputDone = false

        while (!outputDone) {
            if (!inputDone) {
                val inIdx = codec.dequeueInputBuffer(TIMEOUT_US)
                if (inIdx >= 0) {
                    val inputBuffer = codec.getInputBuffer(inIdx)!!
                    val sampleSize = extractor.readSampleData(inputBuffer, 0)
                    if (sampleSize < 0) {
                        codec.queueInputBuffer(inIdx, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                        inputDone = true
                    } else {
                        codec.queueInputBuffer(inIdx, 0, sampleSize, extractor.sampleTime, 0)
                        extractor.advance()
                    }
                }
            }

            val outIdx = codec.dequeueOutputBuffer(bufferInfo, TIMEOUT_US)
            if (outIdx >= 0) {
                val outputBuffer = codec.getOutputBuffer(outIdx)!!
                if (bufferInfo.size > 0) {
                    outputBuffer.position(bufferInfo.offset)
                    outputBuffer.limit(bufferInfo.offset + bufferInfo.size)
                    readPcm16(outputBuffer, srcChannels, rawSamples)
                }
                codec.releaseOutputBuffer(outIdx, false)
                if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                    outputDone = true
                }
            }
        }

        codec.stop()
        codec.release()
        extractor.release()

        // Resample to 16 kHz if needed
        val monoSamples = if (srcSampleRate != SAMPLE_RATE) {
            resample(rawSamples.toShortArray(), srcSampleRate, SAMPLE_RATE)
        } else {
            rawSamples.toShortArray()
        }

        // Pad or truncate to exactly MAX_SAMPLES
        val result = ShortArray(MAX_SAMPLES)
        val copyLen = minOf(monoSamples.size, MAX_SAMPLES)
        System.arraycopy(monoSamples, 0, result, 0, copyLen)
        Log.d(TAG, "Decoded ${monoSamples.size} samples → padded/truncated to $MAX_SAMPLES")
        result
    }

    private fun selectAudioTrack(extractor: MediaExtractor): Int? {
        for (i in 0 until extractor.trackCount) {
            val format = extractor.getTrackFormat(i)
            val mime = format.getString(MediaFormat.KEY_MIME) ?: continue
            if (mime.startsWith("audio/")) return i
        }
        return null
    }

    /**
     * Reads 16-bit PCM samples from [buffer], down-mixes to mono if [srcChannels] > 1,
     * and appends each mono sample to [out].
     */
    private fun readPcm16(buffer: ByteBuffer, srcChannels: Int, out: MutableList<Short>) {
        val shorts: ShortBuffer = buffer.order(ByteOrder.LITTLE_ENDIAN).asShortBuffer()
        val total = shorts.remaining()
        val frames = total / srcChannels
        for (f in 0 until frames) {
            var sum = 0L
            for (c in 0 until srcChannels) {
                sum += shorts[f * srcChannels + c].toLong()
            }
            out.add((sum / srcChannels).toInt().toShort())
        }
    }

    /**
     * Linear resampling from [srcRate] to [dstRate].
     * Good enough quality for speech; avoids any external DSP dependency.
     */
    private fun resample(input: ShortArray, srcRate: Int, dstRate: Int): ShortArray {
        if (srcRate == dstRate) return input
        val ratio = srcRate.toDouble() / dstRate.toDouble()
        val outLen = (input.size / ratio).toInt()
        val output = ShortArray(outLen)
        for (i in 0 until outLen) {
            val srcPos = i * ratio
            val lo = srcPos.toInt().coerceIn(0, input.size - 1)
            val hi = (lo + 1).coerceIn(0, input.size - 1)
            val frac = srcPos - lo
            output[i] = (input[lo] * (1 - frac) + input[hi] * frac).toInt().toShort()
        }
        return output
    }
}
