package com.example.impl.urlguard

import android.graphics.Bitmap
import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

/**
 * Streams screen frames to a backend WebSocket server.
 *
 * Each frame is JPEG-compressed before transmission to keep bandwidth low.
 * If the server is unreachable the client retries automatically every
 * [retryDelayMs] milliseconds. Frames produced faster than the network
 * can absorb are silently dropped (newest-wins) so the live view always
 * shows the most recent state.
 *
 * Usage:
 * ```
 * val client = FrameStreamClient("ws://192.168.1.100:8080/stream")
 * client.start()
 * // … inside onFrameCaptured:
 * client.enqueueFrame(bitmap)   // bitmap is recycled internally
 * // … on stop:
 * client.stop()
 * ```
 *
 * @param serverUrl  Full WebSocket URL, e.g. `ws://10.0.2.2:8080/stream`
 * @param jpegQuality JPEG compression quality 0–100 (default 50 balances size vs clarity)
 * @param retryDelayMs How long to wait before reconnecting after a disconnect (default 3 s)
 */
class FrameStreamClient(
    private val serverUrl: String,
    private val jpegQuality: Int = 50,
    private val retryDelayMs: Long = 3_000L,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val httpClient = HttpClient(Android) {
        install(WebSockets)
    }

    /**
     * Bounded channel that holds at most 2 compressed frames.
     * When the sender is faster than the network, the oldest frame is dropped
     * so the receiver always sees the freshest data.
     */
    private val frameQueue = Channel<ByteArray>(
        capacity = 2,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    @Volatile private var running = false

    // ── Public API ────────────────────────────────────────────────────────────

    /** Open the WebSocket connection and start the send loop. */
    fun start() {
        running = true
        scope.launch { connectLoop() }
        Log.i(TAG, "FrameStreamClient started → $serverUrl")
    }

    /**
     * Compress [bitmap] to JPEG and enqueue it for sending.
     *
     * This call is non-blocking and takes ownership of [bitmap]
     * (it will be recycled here after compression).
     */
    fun enqueueFrame(bitmap: Bitmap) {
        if (!running) {
            bitmap.recycle()
            return
        }
        val bytes = ByteArrayOutputStream().use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, jpegQuality, out)
            bitmap.recycle()
            out.toByteArray()
        }
        // trySend drops the frame if the channel is already full (DROP_OLDEST handles queue)
        frameQueue.trySend(bytes)
    }

    /** Close the WebSocket and release all resources. */
    fun stop() {
        running = false
        frameQueue.close()
        scope.cancel()
        httpClient.close()
        Log.i(TAG, "FrameStreamClient stopped")
    }

    // ── Internal ──────────────────────────────────────────────────────────────

    private suspend fun connectLoop() {
        while (running) {
            try {
                httpClient.webSocket(urlString = serverUrl) {
                    Log.i(TAG, "WebSocket connected → $serverUrl")
                    for (bytes in frameQueue) {          // suspends until next frame arrives
                        if (!running) break
                        send(Frame.Binary(fin = true, data = bytes))
                        Log.v(TAG, "Frame sent (${bytes.size} bytes)")
                    }
                    close()
                }
            } catch (e: CancellationException) {
                break   // scope cancelled — exit cleanly
            } catch (e: Exception) {
                Log.w(TAG, "WebSocket error: ${e.message}. Reconnecting in ${retryDelayMs}ms…")
                if (running) delay(retryDelayMs)
            }
        }
    }

    companion object {
        private const val TAG = "FrameStreamClient"

        /** Change this to point at your backend WebSocket endpoint. */
        const val DEFAULT_URL = "ws://10.0.2.2:8080/stream"
    }
}
