package com.safeNest.demo.features.urlGuard.impl.urlGuard

import android.accessibilityservice.AccessibilityService
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.core.app.NotificationCompat
import com.safeNest.demo.features.urlGuard.impl.R
import com.safeNest.demo.features.urlGuard.impl.detection.UrlDetection
import com.safeNest.demo.features.urlGuard.impl.detection.model.ModelDetectStatus
import com.safeNest.demo.features.urlGuard.impl.urlGuard.mapper.toModelDetectionStatus
import com.safeNest.demo.features.urlGuard.impl.urlGuard.util.UserAllowedDomainGuard
import com.safeNest.demo.features.urlGuard.impl.urlGuard.view.SecureView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Accessibility service responsible for detecting what is on screen and reacting accordingly.
 *
 * ── What it detects ──────────────────────────────────────────────────────────
 *   2.1  Browser is in the foreground  → [ScreenSurface.Browser]  + URL scan
 *   2.2  Phone call is active          → [ScreenSurface.ActiveCall]
 *   2.3  A notification was posted     → [ScreenSurface.Notification]
 *   2.4  Any other user-installed app  → [ScreenSurface.App]       + trust check
 */
@AndroidEntryPoint
class UrlGuardAccessibilityService : AccessibilityService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val threatEngine: ThreatEngine = DefaultThreatEngine()
    private val mainHandler = Handler(Looper.getMainLooper())

    // ── Helpers ───────────────────────────────────────────────────────────────
    private lateinit var screenshotHelper: ScreenshotHelper
    private lateinit var formInspector: FormInspectorWebView

    @Inject
    lateinit var urlDetection: UrlDetection
    @Inject
    lateinit var appTrustChecker: AppTrustChecker
    // ── UI layer ──────────────────────────────────────────────────────────────
    private lateinit var secureView: SecureView

    // ── Debounce handles ──────────────────────────────────────────────────────
    private var pendingUrlCheck: Runnable? = null
    private var pendingAppCheck: Runnable? = null
    private var pendingCallCheck: Runnable? = null

    // ── URL result cache ──────────────────────────────────────────────────────
    private data class UrlCacheEntry(
        val status: DetectionStatus,
        val isMalicious: Boolean,
        val cachedAt: Long = System.currentTimeMillis()
    )

    private val urlCache = object : LinkedHashMap<String, UrlCacheEntry>(64, 0.75f, true) {
        override fun removeEldestEntry(eldest: Map.Entry<String, UrlCacheEntry>) =
            size > URL_CACHE_MAX_SIZE
    }

    // ── Form scan ─────────────────────────────────────────────────────────────
    private val userAllowedUrls = mutableSetOf<String>()
    private val formScanCache = object : LinkedHashMap<String, Boolean>(32, 0.75f, true) {
        override fun removeEldestEntry(eldest: Map.Entry<String, Boolean>) = size > 30
    }

    // ── User-allowed domain guard ─────────────────────────────────────────────

    /**
     * Tracks domains the user has explicitly proceeded through on the blocking
     * page. [shouldBlock] returns false for those domains for the rest of the
     * current session so the page never re-appears on the same site.
     */
    private val allowedDomainGuard = UserAllowedDomainGuard()

    /** URL that triggered the most recent blocking-page display. */
    private var lastBlockedUrl: String? = null

    @Volatile
    private var lastCheckedUrl: String? = null

    // ── Misc state ────────────────────────────────────────────────────────────
    @Volatile
    private var lastBrowserPackage: String? = null

    /** True once [secureView] has been added to WindowManager for the first time. */
    private var isOverlayShown = false

    /**
     * All launcher/home packages on this device.
     * Resolved lazily once so we don't hit PackageManager on every event.
     */
    private val launcherPackages: Set<String> by lazy {
        val homeIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
        packageManager.queryIntentActivities(homeIntent, 0)
            .mapNotNull { it.activityInfo?.packageName }
            .toSet()
            .also { Log.d(TAG, "Launcher packages: $it") }
    }

    // =========================================================================
    // Lifecycle
    // =========================================================================

    override fun onServiceConnected() {
        Log.i(TAG, "UrlGuard accessibility service connected")

        // Initialise helpers
        screenshotHelper = ScreenshotHelper(this, serviceScope, mainHandler)
        formInspector = FormInspectorWebView(
            this, getSystemService(WINDOW_SERVICE)
                    as android.view.WindowManager
        )

        // Initialise UI overlay — not shown yet.
        // Shown lazily via ACTION_SHOW_FLOATING sent from UrlGuardActivity.
        secureView = SecureView(this).apply {
            onGoBackClick = { hideBlockingPage() }
            onProceedAnywayClick = {
                // User consciously chose to proceed → whitelist the domain for
                // this session so the blocking page won't re-trigger on the same site.
                lastBlockedUrl
                    ?.let { url -> UrlExtractor.extractDomain(url) }
                    ?.let { domain -> allowedDomainGuard.allow(domain) }
                hideBlockingPage()
            }
        }

        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification())

        // Start in Idle state
        SurfaceDetector.update(ScreenSurface.Idle)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG, "UrlGuard accessibility service disconnected")
        SurfaceDetector.update(ScreenSurface.Idle)
        if (isOverlayShown) secureView.dismiss()
        formInspector.cleanup()
        allowedDomainGuard.clear()
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.i(TAG, "UrlGuard accessibility service unbound")
        return super.onUnbind(intent)
    }

    override fun onInterrupt() {}

    /**
     * Called when an external component sends:
     *   startService(Intent(context, UrlGuardAccessibilityService::class.java)
     *       .apply { action = ACTION_SHOW_FLOATING / ACTION_HIDE_FLOATING })
     *
     * Because the service is already running (bound by the system as an
     * AccessibilityService), Android routes this call to the **same** instance,
     * so we can safely touch [secureView] here.
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_SHOW_FLOATING -> {
                Log.d(TAG, "onStartCommand: SHOW_FLOATING")
                if (!isOverlayShown) {
                    secureView.showFirstTime()
                    isOverlayShown = true
                }
            }

            ACTION_HIDE_FLOATING -> {
                Log.d(TAG, "onStartCommand: HIDE_FLOATING")
                if (isOverlayShown) {
                    secureView.dismiss()
                    isOverlayShown = false
                }
            }
        }
        return START_NOT_STICKY
    }

    // =========================================================================
    // Accessibility events — dispatch
    // =========================================================================

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        val pkg = event.packageName?.toString() ?: return
        if (pkg == packageName) return  // ignore our own overlay events
        Log.d(TAG, "current event: $event")

        when (event.eventType) {
            // ── A new window / screen came to the foreground ──────────────────
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED,
            AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> {
                when (pkg) {
                    in AppTrustChecker.BROWSER_PACKAGES -> onBrowserForeground(pkg)
                    in AppTrustChecker.CALL_PACKAGES -> onCallForeground(pkg, event)
                    else -> onAppForeground(pkg)
                }
            }

            // ── Content inside an already-visible window changed ──────────────
            //AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED,
//            AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> {
//                when (pkg) {
//                    in AppTrustChecker.BROWSER_PACKAGES -> {
////                        lastBrowserPackage = pkg
////                        scheduleUrlCheck()
//                        onBrowserForeground(pkg)
//                    }
//                    in AppTrustChecker.CALL_PACKAGES -> onCallForeground(pkg, event)
//                    else -> {
//                        onAppForeground(pkg)
//                    }
//                }
//            }

            // ── A notification appeared in the status bar ─────────────────────
            AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED -> {
                onNotificationPosted(pkg, event)
            }

            else -> Unit
        }
    }

    // =========================================================================
    // 2.1 Browser detection
    // =========================================================================

    private fun onBrowserForeground(pkg: String) {
        // Blocking page is a fullscreen overlay on top of the browser — while it is
        // visible the browser is still technically "in the foreground" from the A11y
        // perspective and keeps firing events. Ignore those so the blocking page state
        // (button colour, scan result) is not overwritten by a re-scan of the same URL.
        if (secureView.isBlockingPageVisible) return

        if (pkg != lastBrowserPackage) lastCheckedUrl = null
        lastBrowserPackage = pkg
        SurfaceDetector.update(ScreenSurface.Browser(pkg, null, DetectionStatus.UNKNOWN))
//        secureView.updateButton(FloatingButtonFeature.SAFE_BROWSING, DetectionStatus.UNKNOWN)
//        secureView.updateActionCard(FloatingButtonFeature.SAFE_BROWSING, DetectionStatus.UNKNOWN)
        if(isAddressBarFocused(pkg)) return
        scheduleUrlCheck()
    }

    private fun scheduleUrlCheck() {
        // While the blocking page is shown, rootInActiveWindow points to the bloking
        // page's own view tree. UlExtractor would find the displayed blocked-URL textr
        // inside tv_blocked_url and re-scan it, potentially flipping the floating
        // button to SAFE. Drop all pending and incoming checks until the page is gone.
        if (secureView.isBlockingPageVisible) {
            pendingUrlCheck?.let { mainHandler.removeCallbacks(it) }
            pendingUrlCheck = null
            return
        }

        pendingUrlCheck?.let { mainHandler.removeCallbacks(it) }
        pendingUrlCheck = Runnable {
            pendingUrlCheck = null
            serviceScope.launch {
                val url = UrlExtractor.extract(rootInActiveWindow)
                if (!url.isNullOrBlank() && url != lastCheckedUrl) {
                    lastCheckedUrl = url
                    checkAndBlockIfNeeded(url)
                }
            }
        }.also { mainHandler.postDelayed(it, URL_DEBOUNCE_MS) }
    }

    private suspend fun checkAndBlockIfNeeded(url: String) {
        val browserPkg = lastBrowserPackage ?: return
        val normalUrl = UrlExtractor.normalize(url)

        // Publish scanning state
        SurfaceDetector.update(
            ScreenSurface.Browser(
                browserPkg,
                normalUrl,
                DetectionStatus.UNKNOWN
            )
        )
        val detectedStatus: DetectionStatus
        val isMalicious: Boolean

        // Cache hit
        val cached = urlCache[normalUrl]
        if (cached != null && System.currentTimeMillis() - cached.cachedAt < URL_CACHE_TTL_MS) {
            Log.d(TAG, "URL cache hit [$normalUrl] -> detectStatus ${cached.status}")
            applyUrlResult(normalUrl, cached.status,  browserPkg)
            return
        }
        // Cache miss — API call
        Log.d(TAG, "URL cache miss [$normalUrl]")

        val isHasSensitiveForm = triggerFormInspection(normalUrl).first()
        if (isHasSensitiveForm) {
            detectedStatus = DetectionStatus.WARNING
            isMalicious = false
        } else {

            //val reputation = withContext(Dispatchers.IO) { ScamApiClient.checkUrl(normalUrl) }

            val  modelDetectionStatus = urlDetection.detect(normalUrl)

            detectedStatus = modelDetectionStatus.toModelDetectionStatus()
            isMalicious = modelDetectionStatus == ModelDetectStatus.Scam

        }
        Log.d(TAG, "urlCache: $normalUrl -> $detectedStatus")

        urlCache[normalUrl] = UrlCacheEntry(status = detectedStatus, isMalicious = isMalicious)
        applyUrlResult(normalUrl, detectedStatus, browserPkg)

    }

    /**
     * Applies the URL scan result to [SurfaceDetector] and [SecureView].
     */
    private fun applyUrlResult(
        normalUrl: String,
        status: DetectionStatus,
        browserPkg: String
    ) {

        SurfaceDetector.update(ScreenSurface.Browser(browserPkg, normalUrl, status))
        secureView.updateButton(FloatingButtonFeature.SAFE_BROWSING, status)
        secureView.updateActionCard(FloatingButtonFeature.SAFE_BROWSING, status)

        when (status) {
            DetectionStatus.DANGEROUS,
            DetectionStatus.WARNING -> {
                val domain = UrlExtractor.extractDomain(normalUrl)
                if (domain == null || !allowedDomainGuard.isAllowed(domain)) {
                    lastBlockedUrl = normalUrl
                    secureView.updateBLockingPage(FloatingButtonFeature.SAFE_BROWSING, status, normalUrl)
                    secureView.showBlockingPage()
                } else {
                    Log.d(TAG, "Blocking page suppressed — domain user-allowed: $domain")
                }
            }
            DetectionStatus.SAFE    -> { /* no action */ }
            DetectionStatus.UNKNOWN -> { /* no action */ }
        }
    }

    // =========================================================================
    // 2.2 Call detection
    // =========================================================================

    private fun onCallForeground(pkg: String, event: AccessibilityEvent) {
        val quickNumber = event.text
            .mapNotNull { it?.toString() }
            .firstNotNullOfOrNull { extractPhoneNumber(it) }

        SurfaceDetector.update(ScreenSurface.ActiveCall(quickNumber, DetectionStatus.UNKNOWN))
        secureView.updateButton(FloatingButtonFeature.CALL_PROTECTION, DetectionStatus.UNKNOWN)
        secureView.updateActionCard(FloatingButtonFeature.CALL_PROTECTION, DetectionStatus.UNKNOWN)

        if (quickNumber == null) scheduleCallInfoExtraction(pkg)
    }

    private fun scheduleCallInfoExtraction(pkg: String) {
        pendingCallCheck?.let { mainHandler.removeCallbacks(it) }
        pendingCallCheck = Runnable {
            pendingCallCheck = null
            val number = extractPhoneNumberFromTree(rootInActiveWindow)
            Log.d(TAG, "Incomming phone number: $number")
            val current = SurfaceDetector.getCurrent()
            if (current is ScreenSurface.ActiveCall) {
                SurfaceDetector.update(current.copy(phoneNumber = number))
            }
        }.also { mainHandler.postDelayed(it, CALL_DEBOUNCE_MS) }
    }

    private fun extractPhoneNumberFromTree(root: AccessibilityNodeInfo?): String? {
        root ?: return null
        val text = root.text?.toString()

        if (text != null) {
            val extracted = extractPhoneNumber(text)
            if (extracted != null) return extracted
        }
        for (i in 0 until root.childCount) {
            val child = root.getChild(i) ?: continue
            val result = extractPhoneNumberFromTree(child)
            child.recycle()
            if (result != null) return result
        }
        return null
    }

    /**
     * Returns a phone number found anywhere within [text], or null if none is present.
     * Uses [containsMatchIn] so numbers embedded alongside labels (e.g. "Incoming\n0812345678")
     * are still detected, whereas [matches] would require the entire string to be a number.
     */
    private fun extractPhoneNumber(text: String): String? {
        val cleaned = text
            .replace(Regex("\\p{Cf}"), "") // remove hidden chars
            .trim()
        return PHONE_NUMBER_REGEX.find(cleaned)?.value
    }

    // =========================================================================
    // 2.3 Notification detection
    // =========================================================================

    private fun onNotificationPosted(pkg: String, event: AccessibilityEvent) {
        val texts = event.text
        val title = texts.getOrNull(0)?.toString()?.takeIf { it.isNotBlank() }
        val content = texts.drop(1)
            .mapNotNull { it?.toString() }
            .filter { it.isNotBlank() }
            .joinToString(" ")
            .takeIf { it.isNotBlank() }

        Log.d(TAG, "Notification from [$pkg] title=$title")
        SurfaceDetector.update(
            ScreenSurface.Notification(
                pkg,
                title,
                content,
                DetectionStatus.UNKNOWN
            )
        )
        secureView.updateButton(FloatingButtonFeature.SMS_CHECK, DetectionStatus.UNKNOWN)
        secureView.updateActionCard(FloatingButtonFeature.SMS_CHECK, DetectionStatus.UNKNOWN)
    }

    // =========================================================================
    // 2.4 Any other user-installed app
    // =========================================================================

    private fun onAppForeground(pkg: String) {
        if (appTrustChecker.isSystemApp(pkg)) {
            Log.d(TAG, "AppTrust skipped — system app [$pkg]")
            //SurfaceDetector.update(ScreenSurface.Idle)
            return
        }

        SurfaceDetector.update(ScreenSurface.App(pkg, DetectionStatus.UNKNOWN))
//        secureView.updateButton(FloatingButtonFeature.APP_CHECK, DetectionStatus.UNKNOWN)
//        secureView.updateActionCard(FloatingButtonFeature.APP_CHECK, DetectionStatus.UNKNOWN)
        scheduleAppTrustCheck(pkg)
    }

    private fun scheduleAppTrustCheck(pkg: String) {
        val cached = appTrustChecker.cache[pkg]
        if (cached != null) {
            Log.d(TAG, "AppTrust cache hit [$pkg]")
            SurfaceDetector.update(ScreenSurface.App(pkg, cached))
            secureView.updateButton(FloatingButtonFeature.APP_CHECK, cached)
            secureView.updateActionCard(FloatingButtonFeature.APP_CHECK, cached)
            return
        }

        pendingAppCheck?.let { mainHandler.removeCallbacks(it) }
        pendingAppCheck = Runnable {
            pendingAppCheck = null
            serviceScope.launch {
                val status = appTrustChecker.evaluate(pkg)
                val current = SurfaceDetector.getCurrent()
                if (current is ScreenSurface.App && current.packageName == pkg) {
                    SurfaceDetector.update(current.copy(status = status))
                    secureView.updateButton(FloatingButtonFeature.APP_CHECK, status)
                    secureView.updateActionCard(FloatingButtonFeature.APP_CHECK, status)
                }
            }
        }.also { mainHandler.postDelayed(it, APP_DEBOUNCE_MS) }
    }


    // =========================================================================
    // Form inspection
    // =========================================================================

    private fun triggerFormInspection(normalUrl: String): Flow<Boolean> {
        return callbackFlow {
            val domain = UrlExtractor.extractDomain(normalUrl)
//            if (normalUrl in userAllowedUrls) {
//                trySend(false)
//                close()
//                return@callbackFlow
//            }
            val cacheResult = formScanCache[normalUrl]
            if (cacheResult != null) {
                trySend(cacheResult)
                close()
                return@callbackFlow
            }

            formInspector.inspect(normalUrl) { hasSensitiveForm, detectedFields ->
                if (hasSensitiveForm) {
                    Log.w(TAG, "Sensitive form detected at $normalUrl: $detectedFields")

                }
                formScanCache[normalUrl] = hasSensitiveForm
                trySend(hasSensitiveForm)
                close()
            }

            awaitClose {
                formInspector.cleanup()
            }
        }
    }

    // =========================================================================
    // Foreground notification
    // =========================================================================

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "URL Guard Service",
                NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        val flags = PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        val contentIntent = packageManager.getLaunchIntentForPackage(packageName)?.let {
            it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
            it.action = Intent.ACTION_MAIN
            it.addCategory(Intent.CATEGORY_LAUNCHER)
            PendingIntent.getActivity(this, 0, it, flags)
        }
        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("SafeBrowsing")
            .setContentText("URL Guard is active")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setContentIntent(contentIntent)
            .build()
    }

    // =========================================================================
    // Companion
    // =========================================================================

    companion object {
        private const val TAG = "UrlGuardA11y"
        private const val NOTIFICATION_ID = 1
        private const val NOTIFICATION_CHANNEL_ID = "url_guard_channel"

        private const val URL_CACHE_TTL_MS = 5 * 60 * 1000L
        private const val URL_CACHE_MAX_SIZE = 50

        private const val URL_DEBOUNCE_MS = 400L
        private const val APP_DEBOUNCE_MS = 600L
        private const val CALL_DEBOUNCE_MS = 300L

        private val PHONE_NUMBER_REGEX = Regex("""^\+?[\d\s\-().]{6,20}$""")

        // ── Intent actions for startService() control ────────────────────────
        const val ACTION_SHOW_FLOATING = "com.safeNest.ACTION_SHOW_FLOATING"
        const val ACTION_HIDE_FLOATING = "com.safeNest.ACTION_HIDE_FLOATING"

        // ── Known address bar view IDs per browser package ───────────────────
        val BROWSER_ADDRESS_BAR_IDS = mapOf(
            "com.android.chrome"           to "com.android.chrome:id/url_bar",
            "com.chrome.beta"              to "com.chrome.beta:id/url_bar",
            "com.chrome.dev"               to "com.chrome.dev:id/url_bar",
            "com.chrome.canary"            to "com.chrome.canary:id/url_bar",
            "org.mozilla.firefox"          to "org.mozilla.firefox:id/mozac_browser_toolbar_url_view",
            "org.mozilla.firefox_beta"     to "org.mozilla.firefox_beta:id/mozac_browser_toolbar_url_view",
            "com.brave.browser"            to "com.brave.browser:id/url_bar",
            "com.microsoft.emmx"           to "com.microsoft.emmx:id/url_bar",
            "com.opera.browser"            to "com.opera.browser:id/url_field",
            "com.opera.mini.native"        to "com.opera.mini.native:id/url_field",
            "com.sec.android.app.sbrowser" to "com.sec.android.app.sbrowser:id/location_bar_edit_text",
            "com.kiwibrowser.browser"      to "com.kiwibrowser.browser:id/url_bar",
            "com.vivaldi.browser"          to "com.vivaldi.browser:id/url_bar",
        )
    }

    // =========================================================================
    // Address bar focus detection
    // =========================================================================

    /**
     * Returns true if the browser address bar currently has input focus,
     * meaning the user is actively editing the URL and has not yet navigated.
     *
     * Lookup order:
     *   1. Known view ID for [pkg] → fast direct lookup
     *   2. Fallback to [AccessibilityNodeInfo.findFocus] → works for unknown browsers
     *      or when the browser updates its resource IDs between versions.
     */
    fun isAddressBarFocused(pkg: String): Boolean {
        val root = rootInActiveWindow ?: return false

        val knownId = BROWSER_ADDRESS_BAR_IDS[pkg]
        if (knownId != null) {
            val nodes = root.findAccessibilityNodeInfosByViewId(knownId)
            if (nodes.isNotEmpty()) {
                val focused = nodes.any { it.isFocused }
                nodes.forEach { it.recycle() }
                return focused
            }
            // ID didn't match — browser may have updated, fall through to generic check
        }

        val focused = root.findFocus(AccessibilityNodeInfo.FOCUS_INPUT)
        val isFocused = focused?.isFocused == true &&
                focused.className?.contains("EditText") == true
        focused?.recycle()
        return isFocused
    }
}
