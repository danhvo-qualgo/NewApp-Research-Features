package com.safeNest.demo.features.urlGuard.impl.urlGuard

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PixelFormat
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.WindowManager
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient

class FormInspectorWebView(
    private val context: Context,
    private val windowManager: WindowManager
) {
    private var webView: WebView? = null
    private val mainHandler = Handler(Looper.getMainLooper())
    private var timeoutRunnable: Runnable? = null
    private var isInspecting = false

    @SuppressLint("SetJavaScriptEnabled")
    fun inspect(
        url: String,
        onResult: (hasSensitiveForm: Boolean, detectedFields: List<String>) -> Unit
    ) {
        if (isInspecting) return
        isInspecting = true
        val wv = WebView(context)
        webView = wv

        wv.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            loadWithOverviewMode = true
            useWideViewPort = true
        }

        wv.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                view.evaluateJavascript(FORM_DETECTION_JS) { result ->
                    val hasSensitiveForm = result.contains("\"hasSensitiveForm\":true")
                    val fields = parseFields(result)
                    cancelTimeout()

                    Log.d(TAG, result)
                    onResult(hasSensitiveForm, fields)
                    cleanup()
                }
            }

            override fun onReceivedError(
                view: WebView,
                request: WebResourceRequest,
                error: WebResourceError
            ) {
                if (request.isForMainFrame) {
                    cancelTimeout()
                    mainHandler.post {
                        cleanup()
                        onResult(false, emptyList())
                    }
                }
            }
        }

        val params = WindowManager.LayoutParams(
            1, 1,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            PixelFormat.TRANSLUCENT
        ).apply { alpha = 0f }

        try {
            windowManager.addView(wv, params)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to add WebView to WindowManager", e)
            onResult(false, emptyList())
            return
        }

        wv.loadUrl(url)

        timeoutRunnable = Runnable {
            cleanup()
            onResult(false, emptyList())
        }.also { mainHandler.postDelayed(it, TIMEOUT_MS) }
    }

    fun cleanup() {
        isInspecting = false
        cancelTimeout()
        webView?.let { wv ->
            try {
                windowManager.removeView(wv)
            } catch (_: Exception) {
            }
            wv.stopLoading()
            mainHandler.post { wv.destroy() }
        }
        webView = null
    }

    private fun cancelTimeout() {
        timeoutRunnable?.let { mainHandler.removeCallbacks(it) }
        timeoutRunnable = null
    }

    private fun parseFields(jsonResult: String): List<String> {
        val regex = Regex("\"fields\":\\[([^]]*)]")
        val match = regex.find(jsonResult) ?: return emptyList()
        return match.groupValues[1]
            .split(",")
            .map { it.trim().removeSurrounding("\"") }
            .filter { it.isNotBlank() }
    }

    companion object {
        private const val TAG = "FormInspector"
        private const val TIMEOUT_MS = 10_000L

        private val FORM_DETECTION_JS = """
            (function() {
                var inputs = document.querySelectorAll('input, select, textarea');
                var sensitiveTypes = ['password', 'tel', 'date'];
                var sensitiveKeywords = ['phone','birth','dob',
                    'passport','ssn','identity','mobile','pin'];
                var sensitiveAC = ['bday','bday-day','bday-month','bday-year','tel',
                    'tel-national','new-password','current-password',
                    'one-time-code','cc-number'];
                var found = [];
                inputs.forEach(function(el) {
                    var type = (el.type || '').toLowerCase();
                    var combined = [el.name, el.id, el.placeholder,
                        el.autocomplete, el.getAttribute('aria-label')]
                        .join(' ').toLowerCase();
                    if (sensitiveTypes.indexOf(type) !== -1 && found.indexOf(type) === -1)
                        found.push(type);
                    sensitiveKeywords.forEach(function(kw) {
                        if (combined.indexOf(kw) !== -1 && found.indexOf(kw) === -1)
                            found.push(kw);
                    });
                    sensitiveAC.forEach(function(ac) {
                        if ((el.autocomplete||'').toLowerCase() === ac && found.indexOf(ac) === -1)
                            found.push(ac);
                    });
                });
                return { 
                    hasSensitiveForm: found.length > 0, 
                    fields: found 
                };
            })()
        """.trimIndent()
    }
}