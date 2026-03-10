package net.qualgo.safeNest.features.phishingDetection.impl.presentation.urlChecker

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import net.qualgo.safeNest.features.phishingDetection.impl.presentation.models.WebsiteMetadata
import org.json.JSONObject

class WebsiteInspectorWebView(
    private val context: Context,
    private val container: ViewGroup
) {
    private var webView: WebView? = null
    private val mainHandler = Handler(Looper.getMainLooper())
    private var timeoutRunnable: Runnable? = null
    private var isInspecting = false

    @SuppressLint("SetJavaScriptEnabled")
    fun inspect(url: String, onResult: (Result<WebsiteMetadata>) -> Unit) {
        if (isInspecting) return
        isInspecting = true

        val wv = WebView(context)
        webView = wv

        wv.visibility = View.GONE
        wv.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            loadWithOverviewMode = true
            useWideViewPort = true
        }

        wv.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                view.evaluateJavascript(METADATA_EXTRACTION_JS) { result ->
                    cancelTimeout()
                    val metadata = parseMetadata(result)
                    Log.d(TAG, "Extracted metadata: $result")
                    cleanup()
                    onResult(Result.success(metadata))
                }
            }

            override fun onReceivedError(
                view: WebView,
                request: WebResourceRequest,
                error: WebResourceError
            ) {
                if (request.isForMainFrame) {
                    cancelTimeout()
                    val message = error.description?.toString() ?: "Failed to load page"
                    Log.e(TAG, "WebView error: $message")
                    cleanup()
                    mainHandler.post { onResult(Result.failure(RuntimeException(message))) }
                }
            }
        }

        container.addView(wv)
        wv.loadUrl(url)

        timeoutRunnable = Runnable {
            Log.w(TAG, "Inspection timed out for $url")
            cleanup()
            onResult(Result.failure(RuntimeException("Timed out loading $url")))
        }.also { mainHandler.postDelayed(it, TIMEOUT_MS) }
    }

    fun cleanup() {
        isInspecting = false
        cancelTimeout()
        webView?.let { wv ->
            try { container.removeView(wv) } catch (_: Exception) {}
            wv.stopLoading()
            mainHandler.post { wv.destroy() }
        }
        webView = null
    }

    private fun cancelTimeout() {
        timeoutRunnable?.let { mainHandler.removeCallbacks(it) }
        timeoutRunnable = null
    }

    private fun parseMetadata(jsonResult: String): WebsiteMetadata {
        return try {
            // evaluateJavascript wraps string results in quotes; JSONObject handles object literals
            val json = JSONObject(jsonResult)
            WebsiteMetadata(
                title = json.optString("title"),
                description = json.optString("description"),
                keywords = json.optString("keywords"),
                ogTitle = json.optString("ogTitle"),
                ogDescription = json.optString("ogDescription"),
                bodyText = json.optString("bodyText")
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse metadata JSON: $jsonResult", e)
            WebsiteMetadata("", "", "", "", "", "")
        }
    }

    companion object {
        private const val TAG = "WebsiteInspector"
        private const val TIMEOUT_MS = 15_000L

        private val METADATA_EXTRACTION_JS = """
            (function() {
                return {
                    title: document.title,
                    description: document.querySelector('meta[name="description"]')?.content ?? "",
                    keywords: document.querySelector('meta[name="keywords"]')?.content ?? "",
                    ogTitle: document.querySelector('meta[property="og:title"]')?.content ?? "",
                    ogDescription: document.querySelector('meta[property="og:description"]')?.content ?? "",
                    bodyText: (document.body?.innerText ?? "")
                };
            })()
        """.trimIndent()
    }
}