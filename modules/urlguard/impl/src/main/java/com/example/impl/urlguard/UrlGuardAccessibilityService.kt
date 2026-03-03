package com.example.impl.urlguard

import android.Manifest
import android.accessibilityservice.AccessibilityService
import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.PackageManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentValues
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.MediaRecorder
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.view.Display
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.res.ResourcesCompat
import com.example.impl.urlguard.networking.ScamApiClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.qualgo.safeNest.urlguard.impl.R
import java.io.File
import java.io.FileOutputStream
import java.lang.ref.WeakReference

/**
 * Listens to browser windows, extracts the URL from the address bar via the accessibility tree,
 * runs it through the threat engine, and triggers blocking (Activity or overlay) when malicious.
 *
 * Also exposes a floating overlay button whose text can be updated dynamically via [showFloatingButton].
 */
class UrlGuardAccessibilityService : AccessibilityService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val threatEngine: ThreatEngine = DefaultThreatEngine()
    private val mainHandler = Handler(Looper.getMainLooper())

    /** Debounce: wait for content changes to settle before reading URL (TYPE_WINDOW_CONTENT_CHANGED fires very often). */
    private var pendingUrlCheck: Runnable? = null
    private val debounceDelayMs = 400L

    /** Last browser package that sent an event (so we can grant it content URI permission before navigating). */
    @Volatile
    private var lastBrowserPackage: String? = null

    /** Debounce for app-trust checks — avoids re-checking the same package on rapid events. */
    private var pendingAppCheck: Runnable? = null
    private val appCheckDebounceMs = 600L

    /**
     * Cache of packages already verified this session.
     * Key   = package name
     * Value = overlay text to show (so switching back to the same app restores it instantly)
     */
    private val appTrustCache = mutableMapOf<String, String>()

    /** Holds one cached API result for a URL. */
    private data class UrlCacheEntry(
        val overlayText: String,
        val isMalicious: Boolean,
        val cachedAt: Long = System.currentTimeMillis()
    )

    /**
     * LRU cache for URL scan results.
     * - Max 50 entries (eldest auto-evicted).
     * - Each entry is valid for [URL_CACHE_TTL_MS] (5 minutes).
     */
    private val urlCache = object : LinkedHashMap<String, UrlCacheEntry>(64, 0.75f, true) {
        override fun removeEldestEntry(eldest: Map.Entry<String, UrlCacheEntry>) = size > URL_CACHE_MAX_SIZE
    }


    // -------------------------------------------------------------------------
    // Floating overlay state
    // -------------------------------------------------------------------------
    private var windowManager: WindowManager? = null
    private var floatingContainer: View? = null
    private var floatingLabel: TextView? = null
    private var recordBtn: Button? = null

    // -------------------------------------------------------------------------
    // Screen-recording state
    // -------------------------------------------------------------------------
    private var isRecording = false
    private var mediaRecorder: MediaRecorder? = null
    private var recordingProjection: MediaProjection? = null
    private var recordingVirtualDisplay: VirtualDisplay? = null
    private var recordingFile: File? = null

    // -------------------------------------------------------------------------
    // Lifecycle
    // -------------------------------------------------------------------------

    override fun onServiceConnected() {
        Log.i(TAG, "UrlGuard accessibility service connected")
        weakInstance = WeakReference(this)
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        createNotificationChannel()
        startForeground(1, buildNotification())

        showFloatingButton("chrome")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG, "UrlGuard accessibility service disconnect")
        weakInstance = null
        if (isRecording) stopScreenRecording()
        hideFloatingButton()
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.i(TAG, "UrlGuard accessibility service unbind")
        return super.onUnbind(intent)
    }

    // -------------------------------------------------------------------------
    // Floating overlay – public API
    // -------------------------------------------------------------------------

    /**
     * Show a floating button on screen with [text].
     * If the button is already visible, only the text is updated.
     * Safe to call from any thread.
     */
    fun showFloatingButton(text: String) {
        mainHandler.post {
            if (floatingContainer == null) {
                createFloatingOverlay(text)
            } else {
                floatingLabel?.text = text
            }
        }
    }

    /**
     * Update the text shown in the floating button without recreating it.
     * No-op if the button is not currently shown.
     */
    fun updateFloatingText(text: String) {
        mainHandler.post {
            floatingLabel?.text = text
        }
    }

    /**
     * Remove the floating button from the screen.
     * Safe to call from any thread, no-op if already hidden.
     */
    fun hideFloatingButton() {
        mainHandler.post {
            floatingContainer?.let { view ->
                try {
                    windowManager?.removeView(view)
                } catch (e: Exception) {
                    Log.w(TAG, "hideFloatingButton: removeView failed", e)
                }
            }
            floatingContainer = null
            floatingLabel = null
            recordBtn = null
        }
    }

    // -------------------------------------------------------------------------
    // Floating overlay – internal construction
    // -------------------------------------------------------------------------

    @SuppressLint("ClickableViewAccessibility")
    private fun createFloatingOverlay(initialText: String) {
        val wm = windowManager ?: return

        // ── outer card ────────────────────────────────────────────────────────
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = ResourcesCompat.getDrawable(
                resources, R.drawable.floating_button_bg, theme
            )
            elevation = 8f
        }

        // ── info text ─────────────────────────────────────────────────────────
        val label = TextView(this).apply {
            text = initialText
            textSize = 13f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.WHITE)
            val hpad = dpToPx(12)
            setPadding(hpad, dpToPx(8), hpad, dpToPx(4))
            maxLines = 10
        }
        floatingLabel = label
        container.addView(label)

        // ── screenshot button ─────────────────────────────────────────────────
        val screenshotBtn = Button(this).apply {
            text = "📷 Screenshot"
            textSize = 11f
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.TRANSPARENT)
            val hpad = dpToPx(12)
            setPadding(hpad, dpToPx(2), hpad, dpToPx(6))
        }
        screenshotBtn.setOnClickListener { takeScreenshot() }
        container.addView(screenshotBtn)

        // ── record button ─────────────────────────────────────────────────────
        val recBtn = Button(this).apply {
            text = "🔴 Record"
            textSize = 11f
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.TRANSPARENT)
            val hpad = dpToPx(12)
            setPadding(hpad, dpToPx(2), hpad, dpToPx(6))
        }
        recBtn.setOnClickListener { toggleRecording() }
        recordBtn = recBtn
        container.addView(recBtn)

        floatingContainer = container

        // ── window params ─────────────────────────────────────────────────────
        val overlayType = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            overlayType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = dpToPx(16)
            y = dpToPx(100)
        }

        // ── drag support ──────────────────────────────────────────────────────
        var startParamX = 0
        var startParamY = 0
        var startRawX = 0f
        var startRawY = 0f
        var moved = false

        container.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    startParamX = params.x
                    startParamY = params.y
                    startRawX = event.rawX
                    startRawY = event.rawY
                    moved = false
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = (event.rawX - startRawX).toInt()
                    val dy = (event.rawY - startRawY).toInt()
                    if (Math.abs(dx) > 4 || Math.abs(dy) > 4) moved = true
                    params.x = startParamX + dx
                    params.y = startParamY + dy
                    wm.updateViewLayout(container, params)
                    true
                }
                MotionEvent.ACTION_UP -> {
                    // If it was a tap (not a drag), hide the overlay
                    if (!moved) hideFloatingButton()
                    true
                }
                else -> false
            }
        }

        wm.addView(container, params)
        Log.i(TAG, "Floating overlay shown: $initialText")
    }

    private fun dpToPx(dp: Int): Int =
        (dp * resources.displayMetrics.density + 0.5f).toInt()

    // -------------------------------------------------------------------------
    // Accessibility event handling (unchanged)
    // -------------------------------------------------------------------------

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        val pkg = event.packageName?.toString() ?: return
        if (pkg == packageName) return  // ignore our own overlay events
        Log.d("xxx", "Event : $event")
        when (event.eventType) {
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                if (BROWSER_PACKAGES.contains(pkg)) {
                    // Entering a browser — reset app-trust tracking and check URL
                    // (browser — no app-trust reset needed)
                    lastBrowserPackage = pkg
                    scheduleUrlCheck()
                } else {
                    // Entering any other app — check its trust level
                    scheduleAppTrustCheck(pkg)
                }
            }
            AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED,
            AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> {
                if (BROWSER_PACKAGES.contains(pkg)) {
                    lastBrowserPackage = pkg
                    scheduleUrlCheck()
                }
            }
            else -> { }
        }
    }

    private fun scheduleUrlCheck() {
        pendingUrlCheck?.let { mainHandler.removeCallbacks(it) }
        pendingUrlCheck = Runnable {
            pendingUrlCheck = null
            serviceScope.launch {
                val url = extractUrlFromRoot(rootInActiveWindow)
                if (!url.isNullOrBlank()) checkAndBlockIfNeeded(url)
            }
        }.also { mainHandler.postDelayed(it, debounceDelayMs) }
    }

    private fun scheduleAppTrustCheck(pkg: String) {
        // Skip system apps entirely — no overlay, no API call needed
        if (isSystemApp(pkg)) {
            Log.d(TAG, "AppTrust skipped — system app [$pkg]")
            return
        }

        // Already verified this session — restore cached overlay instantly, no API call
        val cached = appTrustCache[pkg]
        if (cached != null) {
            Log.d(TAG, "AppTrust cache hit [$pkg]")
            updateFloatingText(cached)
            return
        }
        pendingAppCheck?.let { mainHandler.removeCallbacks(it) }
        pendingAppCheck = Runnable {
            pendingAppCheck = null
            serviceScope.launch { checkAppTrust(pkg) }
        }.also { mainHandler.postDelayed(it, appCheckDebounceMs) }
    }

    override fun onInterrupt() {}

    // -------------------------------------------------------------------------
    // Notification helpers (unchanged)
    // -------------------------------------------------------------------------

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "url_guard_channel",
                "URL Guard Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        val pendingIntentFlags = PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT

        val contentPendingIntent = packageManager.getLaunchIntentForPackage(packageName)?.let {
            it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
            it.action = Intent.ACTION_MAIN
            it.addCategory(Intent.CATEGORY_LAUNCHER)
            PendingIntent.getActivity(this, 0, it, pendingIntentFlags)
        }

        return NotificationCompat.Builder(this, "url_guard_channel")
            .setContentTitle("SafeBrowsing")
            .setContentText("this is tittle")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setContentIntent(contentPendingIntent)
            .build()
    }

    // -------------------------------------------------------------------------
    // URL extraction (unchanged)
    // -------------------------------------------------------------------------

    private fun extractUrlFromRoot(root: AccessibilityNodeInfo?): String? {
        if (root == null) return null
        return try {
            extractByViewId(root) ?: extractByTraversing(root)
        } finally {
            root.recycle()
        }
    }

    private fun extractByViewId(root: AccessibilityNodeInfo): String? {
        val chromeNodes = root.findAccessibilityNodeInfosByViewId("com.android.chrome:id/url_bar")
        val chromeText = chromeNodes.firstOrNull()?.text?.toString()?.trim()
        chromeNodes.forEach { it.recycle() }
        if (!chromeText.isNullOrBlank() && looksLikeUrl(chromeText)) return chromeText
        val ffNodes = root.findAccessibilityNodeInfosByViewId("org.mozilla.fenix:id/mozac_browser_toolbar_url_view")
        val ffText = ffNodes.firstOrNull()?.text?.toString()?.trim()
        ffNodes.forEach { it.recycle() }
        if (!ffText.isNullOrBlank() && looksLikeUrl(ffText)) return ffText
        return null
    }

    private fun extractByTraversing(node: AccessibilityNodeInfo): String? {
        val text = node.text?.toString()?.trim()
        if (!text.isNullOrBlank() && looksLikeUrl(text)) return text
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val found = extractByTraversing(child)
            child.recycle()
            if (found != null) return found
        }
        return null
    }

    private fun looksLikeUrl(s: String): Boolean =
        s.startsWith("http://") || s.startsWith("https://") || s.contains(".")

    private fun normalizeUrl(raw: String): String {
        return try {
            var s = raw.trim()
            if (!s.startsWith("http://") && !s.startsWith("https://")) s = "https://$s"
            s
        } catch (_: Exception) {
            raw
        }
    }
    private suspend fun checkAndBlockIfNeeded(url: String) {
        val malicious = withContext(Dispatchers.Default) { threatEngine.isMalicious(url) }
        val normalUrl = normalizeUrl(url)

        // ── Cache look-up ─────────────────────────────────────────────────────
        val cached = urlCache[normalUrl]
        if (cached != null && System.currentTimeMillis() - cached.cachedAt < URL_CACHE_TTL_MS) {
            Log.d(TAG, "URL cache hit [$normalUrl]")
            updateFloatingText(cached.overlayText)
            if (cached.isMalicious) {
                Log.w(TAG, "Blocking malicious URL (cached): $url")
                triggerBlocking(blockedUrl = url, browserPackage = lastBrowserPackage)
            }
            return
        }

        // ── Cache miss — call API ─────────────────────────────────────────────
        updateFloatingText("🔍 Checking…")
        Log.d(TAG, "URL cache miss, calling API [$normalUrl]")
        val reputation = withContext(Dispatchers.IO) { ScamApiClient.checkUrl(normalUrl) }

        val overlayText = if (reputation != null) {
            val isScam      = reputation.isScam || malicious
            val riskLevel   = reputation.riskLevel.replaceFirstChar { it.uppercase() }
            val confidence  = (reputation.confidence * 100).toInt()
            val domain      = Uri.parse(url).host ?: url
            val category    = reputation.scamCategories
                .firstOrNull()
                ?.replace("_", " ")
                ?.replaceFirstChar { it.uppercase() }
            val topEvidence = reputation.evidence.firstOrNull()

            val statusIcon = when {
                isScam                           -> "🚫 Scam Detected"
                reputation.riskLevel == "high"   -> "⚠️ High Risk"
                reputation.riskLevel == "medium" -> "⚠️ Medium Risk"
                else                             -> "✅ Safe"
            }

            buildString {
                appendLine(statusIcon)
                appendLine("→ $domain")
                appendLine("Risk       : ${riskLevel.ifBlank { "Unknown" }}")
                appendLine("Confidence : $confidence%")
                if (!category.isNullOrBlank())    appendLine("Category   : $category")
                if (!topEvidence.isNullOrBlank()) appendLine("Evidence   : $topEvidence")
            }.trimEnd()
        } else {
            if (malicious) "🚫 Blocked\n$url" else "🔗 ${Uri.parse(url).host ?: url}"
        }

        // ── Store in cache ────────────────────────────────────────────────────
        urlCache[normalUrl] = UrlCacheEntry(
            overlayText = overlayText,
            isMalicious = malicious || reputation?.isScam == true
        )

        Log.d(TAG, "Overlay [${if (malicious) "MALICIOUS" else "safe"}]: $overlayText")
        updateFloatingText(overlayText)

        if (malicious) {
            Log.w(TAG, "Blocking malicious URL: $url")
            triggerBlocking(blockedUrl = url, browserPackage = lastBrowserPackage)
        }
    }

    // -------------------------------------------------------------------------
    // App trust check
    // -------------------------------------------------------------------------

    private suspend fun checkAppTrust(pkg: String) {
        val appName     = getAppName(pkg)
        val installSrc  = withContext(Dispatchers.Default) { getInstallSource(pkg) }
        Log.d(TAG, "installSrc $installSrc")
        val isPlayStore = installSrc == "com.android.vending"
        val isDangerous = DANGEROUS_PACKAGES.contains(pkg)
        val isKnownSafe = TRUSTED_PACKAGES.contains(pkg)

        val trust = when {
            isDangerous              -> TrustLevel.DANGEROUS
            isKnownSafe  -> TrustLevel.TRUSTED
            isPlayStore              -> TrustLevel.VERIFIED
            else                     -> TrustLevel.UNKNOWN
        }

        val srcLabel = when {
            isPlayStore        -> "Google Play Store"
            installSrc != null -> installSrc
            else               -> "Unknown source"
        }

        // Helper to build the overlay text with an optional category line
        fun buildOverlay(category: String?) = buildString {
            appendLine("${trust.icon} ${trust.label}")
            appendLine(appName)
            if (!category.isNullOrBlank()) appendLine("🏷 $category")
            appendLine("Src : $srcLabel")
        }.trimEnd()

        // ── Show basic info immediately while category is being fetched ───────
        updateFloatingText(buildOverlay(null))

        // ── Fetch Play Store category (only for Play Store apps) ──────────────
        val category = if (isPlayStore) {
            withContext(Dispatchers.IO) { ScamApiClient.fetchPlayStoreCategory(pkg) }
        } else null

        val finalText = buildOverlay(category)
        appTrustCache[pkg] = finalText    // cache full result for instant restore on revisit
        Log.d(TAG, "AppTrust [$pkg] → $trust | src=$srcLabel | category=$category")
        updateFloatingText(finalText)
    }

    private fun getAppName(pkg: String): String = try {
        packageManager.getApplicationLabel(
            packageManager.getApplicationInfo(pkg, 0)
        ).toString()
    } catch (_: Exception) { pkg }

    private fun isSystemApp(pkg: String): Boolean = try {
        (packageManager.getApplicationInfo(pkg, 0).flags and ApplicationInfo.FLAG_SYSTEM) != 0
    } catch (_: Exception) { false }

    private fun getInstallSource(pkg: String): String? = try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            packageManager.getInstallSourceInfo(pkg).installingPackageName
        } else {
            @Suppress("DEPRECATION")
            packageManager.getInstallerPackageName(pkg)
        }
    } catch (_: Exception) { null }

    private enum class TrustLevel(val icon: String, val label: String) {
        TRUSTED("✅", "Trusted"),
        VERIFIED("🔵", "Verified (Play Store)"),
        UNKNOWN("⚠️", "Unknown source"),
        DANGEROUS("🚫", "Dangerous app")
    }

    // -------------------------------------------------------------------------
    // Screenshot
    // -------------------------------------------------------------------------

    private fun takeScreenshot() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            takeScreenshotViaA11y()
        } else {
            takeScreenshotViaMediaProjection()
        }
    }

    /** API 30+: use the built-in AccessibilityService screenshot API — no extra permissions needed. */
    @RequiresApi(Build.VERSION_CODES.R)
    private fun takeScreenshotViaA11y() {
        takeScreenshot(Display.DEFAULT_DISPLAY, mainExecutor,
            object : TakeScreenshotCallback {
                override fun onSuccess(result: ScreenshotResult) {
                    val hardware = Bitmap.wrapHardwareBuffer(
                        result.hardwareBuffer, result.colorSpace
                    )
                    result.hardwareBuffer.close()
                    // Hardware bitmaps can't be compressed directly — copy to software first
                    val bitmap = hardware?.copy(Bitmap.Config.ARGB_8888, false)
                    hardware?.recycle()
                    if (bitmap != null) {
                        serviceScope.launch(Dispatchers.IO) { saveToGallery(bitmap) }
                    }
                }
                override fun onFailure(errorCode: Int) {
                    Log.e(TAG, "takeScreenshotViaA11y failed: errorCode=$errorCode")
                    updateFloatingText("📷 Failed ($errorCode)")
                }
            }
        )
    }

    /** Stored when the user grants screen-capture permission in MainActivity. */
    var mediaProjectionResultCode: Int = Activity.RESULT_CANCELED
    var mediaProjectionData: Intent? = null

    /** API 24-29: use MediaProjection with the token obtained in MainActivity. */
    private fun takeScreenshotViaMediaProjection() {

        val metrics  = resources.displayMetrics
        val width    = metrics.widthPixels
        val height   = metrics.heightPixels
        val density  = metrics.densityDpi

        val projMgr = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        val projection = try {
            projMgr.getMediaProjection(mediaProjectionResultCode, mediaProjectionData!!)
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "getMediaProjection failed: ${e.message}")
            updateFloatingText("📷 Permission expired\nReopen app")
            return
        }

        val imageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 2)
        val vDisplay   = projection?.createVirtualDisplay(
            "safenest_screenshot", width, height, density,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            imageReader.surface, null, null
        )

        // Allow one frame to render before reading
        mainHandler.postDelayed({
            val image = imageReader.acquireLatestImage()
            if (image != null) {
                val plane      = image.planes[0]
                val rowPadding = plane.rowStride - plane.pixelStride * width
                val raw = Bitmap.createBitmap(
                    width + rowPadding / plane.pixelStride, height, Bitmap.Config.ARGB_8888
                )
                raw.copyPixelsFromBuffer(plane.buffer)
                image.close()
                vDisplay?.release()
                projection?.stop()
                imageReader.close()
                val bitmap = Bitmap.createBitmap(raw, 0, 0, width, height)
                raw.recycle()
                serviceScope.launch(Dispatchers.IO) { saveToGallery(bitmap) }
            } else {
                vDisplay?.release()
                projection?.stop()
                imageReader.close()
                updateFloatingText("📷 Capture failed")
            }
        }, 300L)
    }

    /** Saves [bitmap] to the Pictures/SafeNest gallery folder. Must be called on an IO thread. */
    private fun saveToGallery(bitmap: Bitmap) {
        val filename = "SafeNest_${System.currentTimeMillis()}.png"
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, filename)
                    put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                    put(MediaStore.Images.Media.RELATIVE_PATH,
                        "${Environment.DIRECTORY_PICTURES}/SafeNest")
                }
                val uri = contentResolver.insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values
                )
                uri?.let { contentResolver.openOutputStream(it)?.use { out ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                } }
            } else {
                val dir = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                    "SafeNest"
                )
                dir.mkdirs()
                FileOutputStream(File(dir, filename)).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                }
                // Notify the gallery scanner
                sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                    Uri.fromFile(File(dir, filename))))
            }
            bitmap.recycle()
            Log.i(TAG, "Screenshot saved: $filename")
            mainHandler.post { updateFloatingText("📷 Saved to gallery!") }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "saveToGallery failed: ${e.message}", e)
            mainHandler.post { updateFloatingText("📷 Save failed") }
        }
    }

    private fun triggerBlocking(blockedUrl: String, browserPackage: String?) {
        val contentUri = BlockedPageContentProvider.buildBlockedUri(blockedUrl)
        val pkg = browserPackage?.takeIf { it in BROWSER_PACKAGES } ?: "com.android.chrome"
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(contentUri, "text/html")
            setPackage(pkg)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        Log.d(TAG, "triggerBlocking: launching browser with content:// uri=$contentUri pkg=$pkg")
        try {
            startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "triggerBlocking: startActivity failed", e)
        }
    }

    private fun findUrlBarNode(root: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        val chromeNodes = root.findAccessibilityNodeInfosByViewId("com.android.chrome:id/url_bar")
        val node = chromeNodes.firstOrNull()
        chromeNodes.filter { it !== node }.forEach { it.recycle() }
        if (node != null) return node
        val ffNodes = root.findAccessibilityNodeInfosByViewId("org.mozilla.fenix:id/mozac_browser_toolbar_url_view")
        val ffNode = ffNodes.firstOrNull()
        ffNodes.filter { it !== ffNode }.forEach { it.recycle() }
        return ffNode
    }

    private fun setUrlBarText(urlBar: AccessibilityNodeInfo, text: String) {
        val args = Bundle().apply {
            putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text)
        }
        urlBar.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args)
    }

    private fun findAndClickGoButton(root: AccessibilityNodeInfo) {
        val goDescriptions = listOf("Go", "Load", "Submit", "Navigate", "Open")
        findClickableWithContentDescription(root, goDescriptions)?.let {
            it.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            it.recycle()
        }
    }

    private fun findClickableWithContentDescription(root: AccessibilityNodeInfo, descriptions: List<String>): AccessibilityNodeInfo? {
        val desc = root.contentDescription?.toString()?.trim()
        if (desc != null && descriptions.any { desc.equals(it, ignoreCase = true) } && root.isClickable) {
            return AccessibilityNodeInfo.obtain(root)
        }
        val text = root.text?.toString()?.trim()
        if (text != null && descriptions.any { text.equals(it, ignoreCase = true) } && root.isClickable) {
            return AccessibilityNodeInfo.obtain(root)
        }
        for (i in 0 until root.childCount) {
            val child = root.getChild(i) ?: continue
            val found = findClickableWithContentDescription(child, descriptions)
            child.recycle()
            if (found != null) return found
        }
        return null
    }

    // -------------------------------------------------------------------------
    // Companion
    // -------------------------------------------------------------------------

    companion object {
        private const val TAG = "UrlGuardA11y"

        // ── Live service reference (used by ScreenCapturePermissionActivity) ──
        @Volatile private var weakInstance: WeakReference<UrlGuardAccessibilityService>? = null
        fun getInstance(): UrlGuardAccessibilityService? = weakInstance?.get()

        // ── URL cache settings ────────────────────────────────────────────────
        private const val URL_CACHE_TTL_MS   = 5 * 60 * 1000L  // 5 minutes
        private const val URL_CACHE_MAX_SIZE = 50

        private val BROWSER_PACKAGES = setOf(
            "com.android.chrome",
            "org.mozilla.firefox",
            "org.mozilla.fenix",
            "com.opera.browser",
            "com.microsoft.emmx"
        )

        /** Well-known apps treated as trusted regardless of install source. */
        private val TRUSTED_PACKAGES = setOf(
            // Google
            "com.google.android.gm",
            "com.google.android.apps.maps",
            "com.google.android.youtube",
            "com.google.android.apps.docs",
            "com.google.android.keep",
            // Social / messaging
            "com.whatsapp",
            "com.facebook.katana",
            "com.instagram.android",
            "com.twitter.android",
            "com.telegram.messenger",
            "org.telegram.messenger",
            // Entertainment
            "com.netflix.mediaclient",
            "com.spotify.music",
            // Shopping / finance
            "com.amazon.mShop.android.shopping",
            "com.paypal.android.p2pmobile",
            // Vietnamese apps
            "vn.momo.party",
            "com.vnpay.hdbank",
            "com.zalopay.wallet",
            "vn.tiki.app.tikishopping"
        )

        /**
         * Packages known to be malicious or high-risk.
         * Extend this list with threat intelligence data as needed.
         */
        private val DANGEROUS_PACKAGES = setOf<String>(
            // Add known malicious package names here
        )
    }
}
