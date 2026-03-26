package com.safeNest.demo.features.urlGuard.impl.urlGuard

import android.accessibilityservice.AccessibilityService
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.Display
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.safeNest.demo.features.callProtection.impl.presentation.router.CallDetectionDeeplink
import com.safeNest.demo.features.commonAndroid.openAppSettings
import com.safeNest.demo.features.commonKotlin.IncomingCallType
import com.safeNest.demo.features.commonKotlin.incomingCallSharedFlow
import com.safeNest.demo.features.notificationInterceptor.api.NotificationObserver
import com.safeNest.demo.features.notificationInterceptor.api.model.NotificationCategory
import com.safeNest.demo.features.scamAnalyzer.api.models.AnalysisInput
import com.safeNest.demo.features.scamAnalyzer.api.router.ScamAnalyzerDeepLink
import com.safeNest.demo.features.scamAnalyzer.api.useCase.AnalyzeUseCase
import com.safeNest.demo.features.urlGuard.impl.R
import com.safeNest.demo.features.urlGuard.impl.detection.NotificationDetection
import com.safeNest.demo.features.urlGuard.impl.detection.PhoneDetection
import com.safeNest.demo.features.urlGuard.impl.detection.UrlDetection
import com.safeNest.demo.features.urlGuard.impl.presentation.ScreenCapturePermissionActivity
import com.safeNest.demo.features.urlGuard.impl.urlGuard.mapper.toModelDetectionStatus
import com.safeNest.demo.features.urlGuard.impl.urlGuard.util.UserAllowedDomainGuard
import com.safeNest.demo.features.urlGuard.impl.urlGuard.view.QuickActionCardView
import com.safeNest.demo.features.urlGuard.impl.urlGuard.view.SecureView
import com.safeNest.demo.features.urlGuard.impl.urlGuard.view.model.toActionCardViewListAction
import com.uney.core.router.RouterManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
    lateinit var notificationObserver: NotificationObserver
    @Inject
    lateinit var urlDetection: UrlDetection
    @Inject
    lateinit var phoneDetection: PhoneDetection
    @Inject
    lateinit var notificationDetection: NotificationDetection
    @Inject
    lateinit var appTrustChecker: AppTrustChecker
    @Inject
    lateinit var analyzeUseCase: AnalyzeUseCase
    @Inject
    lateinit var routerManager: RouterManager
    // ── UI layer ──────────────────────────────────────────────────────────────
    private lateinit var secureView: SecureView

    // ── Debounce handles ──────────────────────────────────────────────────────
    private var pendingUrlCheck: Runnable? = null
    private var pendingAppCheck: Runnable? = null
    private var pendingCallCheck: Runnable? = null

    // ── URL result cache ──────────────────────────────────────────────────────
    private data class UrlCacheEntry(
        val status: DetectionStatus,
        val cachedAt: Long = System.currentTimeMillis()
    )

    private val urlCache = object : LinkedHashMap<String, UrlCacheEntry>(64, 0.75f, true) {
        override fun removeEldestEntry(eldest: Map.Entry<String, UrlCacheEntry>) =
            size > URL_CACHE_MAX_SIZE
    }

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
     * True when an external event (notification, scam alert, etc.) has explicitly
     * requested the floating button to be shown while the user is on the home screen.
     * While this flag is set, [onLauncherForeground] will NOT hide the button.
     * Cleared automatically when the user navigates to any non-launcher screen.
     */
    @Volatile
    private var isEventForcedVisible = false

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

    // ── Custome ────────────────────────────────────────────────────────────
    // TODO: replace with logic firstTimeOpenApp with Persistence store
    private var firstTimeOpenApp: Boolean = false

    // =========================================================================
    // Lifecycle
    // =========================================================================

    override fun onServiceConnected() {
        Log.i(TAG, "UrlGuard accessibility service connected")

        // Initialise helpers
        screenshotHelper = ScreenshotHelper(this, serviceScope, mainHandler).also { helper ->
            helper.onScreenshotSaved = { uri -> onScreenshotSaved(uri) }
        }
        formInspector = FormInspectorWebView(
            this, getSystemService(WINDOW_SERVICE)
                    as android.view.WindowManager
        )

        // Initialise the SecureView object. The actual WindowManager.addView() calls
        // are deferred to tryAttachOverlay() so they only run once SYSTEM_ALERT_WINDOW
        // is granted. If the user enables A11y before granting the overlay permission,
        // the service starts cleanly here and the overlay is attached lazily when the
        // app sends ACTION_SHOW_FLOATING (which it does on every onResume).
        secureView = SecureView(this).apply {
            onGoBackClick = {
                lastBlockedUrl
                    ?.let { url -> UrlExtractor.extractDomain(url) }
                    ?.let { domain -> allowedDomainGuard.allow(domain) }
                hideBlockingPage()
            }
            onProceedAnywayClick = {
                // User consciously chose to proceed → whitelist the domain for
                // this session so the blocking page won't re-trigger on the same site.
                lastBlockedUrl
                    ?.let { url -> UrlExtractor.extractDomain(url) }
                    ?.let { domain -> allowedDomainGuard.allow(domain) }
                hideBlockingPage()
            }
            onFloatingButtonClick = { onFloatingButtonTapped() }
            onFloatingButtonLongClick = { triggerScreenshot() }
        }
        tryAttachOverlay()

        serviceScope.launch {
            notificationObserver.notificationFlow.collect { record ->
                if(record.category == NotificationCategory.SYSTEM) return@collect
                onNotificationPosted(
                    record.appSenderPkgName,
                    record.title ?: "",
                    record.content ?: "",
                    record.category
                )
            }
        }

        serviceScope.launch {
            incomingCallSharedFlow.collect { eventCall ->
                handleCallEvent(eventCall.phoneNumber, eventCall.message, eventCall.incomingCallType)

            }
        }

        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification())

        // Start in Idle state
        SurfaceDetector.update(ScreenSurface.Idle)
    }

    /**
     * Attaches all overlay layers to the WindowManager exactly once.
     *
     * This is a no-op if:
     *  - the overlay is already attached ([isOverlayShown] == true), OR
     *  - [Settings.canDrawOverlays] returns false (SYSTEM_ALERT_WINDOW not yet granted).
     *
     * Calling this from both [onServiceConnected] and [onStartCommand] ensures the
     * overlay is initialised regardless of which permission (A11y vs overlay) the user
     * grants first.
     */
    private fun tryAttachOverlay() {
        if (isOverlayShown) return
        if (!Settings.canDrawOverlays(this)) {
            Log.w(TAG, "SYSTEM_ALERT_WINDOW not granted — overlay deferred until permission is granted")
            return
        }
        secureView.showFirstTime()      // adds BlockingPage + FloatingView to WindowManager
        secureView.hideFloatingButton() // hidden by default; shown per foreground-app logic
        isOverlayShown = true
        Log.i(TAG, "Overlay attached")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG, "UrlGuard accessibility service disconnected")
        SurfaceDetector.update(ScreenSurface.Idle)
        if (isOverlayShown) secureView.dismiss()
        formInspector.cleanup()
        allowedDomainGuard.clear()
        //urlDetection.onDestroy()
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
        if (!::secureView.isInitialized) return START_NOT_STICKY
        // Lazy-attach the overlay in case SYSTEM_ALERT_WINDOW was granted after
        // the A11y service already started (e.g. user enabled A11y first).
        tryAttachOverlay()
        when (intent?.action) {
            ACTION_SHOW_FLOATING -> {
                Log.d(TAG, "onStartCommand: SHOW_FLOATING")
                onHostAppForeground()
            }

            ACTION_HIDE_FLOATING -> {
                Log.d(TAG, "onStartCommand: HIDE_FLOATING")
                secureView.hideFloatingButton()
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
        if(pkg == packageName) return
        // When the user navigates into the host app itself, show the floating button.
        // TYPE_WINDOW_STATE_CHANGED = activity/fragment transition (user is in the app).
        // All other own-package events (overlay content changes) are still ignored.
        Log.d(TAG, "EVENT : $event")

        when (event.eventType) {
            // ── A new window / screen came to the foreground ──────────────────
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED,
            AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> {
                when (pkg) {
                    in AppTrustChecker.BROWSER_PACKAGES -> onBrowserForeground(pkg)

                    //                    pkg in AppTrustChecker.CALL_PACKAGES -> onCallForeground(pkg, event)
                    in AppTrustChecker.SOCIAL_NETWORK_PACKAGES,
                    in AppTrustChecker.OTT_PACKAGES -> onSocialOrOttForeground(pkg)

                    in launcherPackages -> onLauncherForeground()
                    else -> onAppForeground(pkg)
                }
            }

//            // ── A notification appeared in the status bar ─────────────────────
//            AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED -> {
//                onNotificationPosted(pkg, event)
//            }
            else -> Unit
        }
    }

    // =========================================================================
    // 2.1 Browser detection
    // =========================================================================

    private fun onBrowserForeground(pkg: String) {
        isEventForcedVisible = false
        // Blocking page is a fullscreen overlay on top of the browser — while it is
        // visible the browser is still technically "in the foreground" from the A11y
        // perspective and keeps firing events. Ignore those so the blocking page state
        // (button colour, scan result) is not overwritten by a re-scan of the same URL.
        if (secureView.isBlockingPageVisible) return

        if (pkg != lastBrowserPackage) lastCheckedUrl = null
        lastBrowserPackage = pkg

        //First time open browser, show floating button
        if(lastCheckedUrl ==null) {
            val initialStatus = lastCheckedUrl
                ?.let { urlCache[UrlExtractor.normalize(it)]?.status }
                ?: DetectionStatus.SAFE   // null = first time → optimistic SAFE
            secureView.updateButton(FloatingButtonFeature.SAFE_BROWSING, initialStatus)
        }

        SurfaceDetector.update(ScreenSurface.Browser(pkg, lastCheckedUrl, DetectionStatus.UNKNOWN))
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
                val url = UrlExtractor.extract(rootInActiveWindow, lastBrowserPackage)
                Log.d(TAG, "extract url: $url")
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
        val detectedStatus: DetectionStatus

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
        } else {
            //val reputation = withContext(Dispatchers.IO) { ScamApiClient.checkUrl(normalUrl) }
            val modelDetectionStatus = urlDetection.detect(normalUrl)
            detectedStatus = modelDetectionStatus.toModelDetectionStatus()
        }
        Log.d(TAG, "urlCache: $normalUrl -> $detectedStatus")

        urlCache[normalUrl] = UrlCacheEntry(status = detectedStatus)
        applyUrlResult(normalUrl, detectedStatus, browserPkg)

    }

    private fun onDetailAction() {
        val input = SurfaceDetector.getCurrent().toAnalysisInput()
        Log.d(TAG, "onDetail action input: ${input}")
        if (input == null) {
            return
        }
        secureView.actionCard.showLoading()
        secureView.showButtonLoading()
        serviceScope.launch {
            try {
                val result = withContext(Dispatchers.IO) { analyzeUseCase(input) }
                result.onSuccess { resultKey ->
                    secureView.hideActionCard()
                    lastBlockedUrl
                        ?.let { url -> UrlExtractor.extractDomain(url) }
                        ?.let { allowedDomainGuard.allow(it) }
                    secureView.hideBlockingPage()
                    val uri = ScamAnalyzerDeepLink.entryPointWithResult(resultKey)
                    routerManager.getLaunchIntent(uri)?.also { intent ->
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        this@UrlGuardAccessibilityService.startActivity(intent)
                    }
                }.onFailure { error ->
                    Log.e(TAG, "Analysis failed", error)
                }
            } finally {
                secureView.actionCard.hideLoading()
                secureView.hideButtonLoading()
            }
        }
    }

    /**
     * Central handler for every floating-button tap.
     *
     * Routes based on the current [ScreenSurface]:
     *
     * • [ScreenSurface.App] whose package is **this app** (KinShield in foreground)
     *     → show a toast tooltip explaining the button's purpose.
     *
     * • [ScreenSurface.Browser] (user is browsing)
     *     → open KinShield and run the detail analysis on the current URL,
     *        identical to tapping the "detail" action inside the quick-action card.
     *
     * • Everything else
     *     → no-op for now; extend here as new surface types need button-tap handling.
     */
    private fun onFloatingButtonTapped() {
        when (val surface = SurfaceDetector.getCurrent()) {
            is ScreenSurface.App -> {
                when(surface.packageName) {
                    packageName -> {
                        Log.d(TAG, "Floating button tapped — KinShield foreground, showing tooltip")
                        secureView.showToastTooltip(
                            getString(R.string.floating_button_idle_tooltip)
                        )
                    }

                    in AppTrustChecker.OTT_PACKAGES,
                    in AppTrustChecker.SOCIAL_NETWORK_PACKAGES -> {

                    }
                    else -> {
                        Log.d(TAG, "Floating button tapped — other app foreground, open setting")
                        this.openAppSettings(surface.packageName)
                    }

                }

            }
            is ScreenSurface.Browser -> {
                Log.d(TAG, "Floating button tapped — Browser foreground, triggering detail action")
                onDetailAction()
            }

            is ScreenSurface.Notification -> {
                Log.d(TAG, "Floating button tapped — Notification handle, triggering detail action")
                onDetailAction()
            }

            is ScreenSurface.ActiveCall -> {
                serviceScope.launch {
                    val phone = surface.phoneNumber ?: return@launch
                    //val getCallerInfo = phoneDetection.getCallerInfo(phone) ?: return@launch
                    val uri = when(surface.fromEvent) {
                        IncomingCallType.BLOCKLIST -> CallDetectionDeeplink.entryPointBlocklist()
                        IncomingCallType.CALLER_ID -> {
                            val getCallerInfo =  phoneDetection.getCallerInfo(phone) ?: return@launch
                            CallDetectionDeeplink.entryPointMissingCall(getCallerInfo)
                        }

                        IncomingCallType.WHITELIST -> {
                            CallDetectionDeeplink.entryPointWhitelist()
                        }
                        else -> null
                    }

                    uri?.let {
                        Log.d(TAG, "tapp open detaill screen with uri: $it")
                        routerManager.getLaunchIntent(it)?.also { intent ->
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            Log.d(TAG, "tapp open detail screen with intent: $intent")
                            this@UrlGuardAccessibilityService.startActivity(intent)
                        }
                    }
                }
            }
            else -> Unit
        }
    }

    // =========================================================================
    // Screenshot on long-press
    // =========================================================================

    /**
     * Initiates a device screenshot.
     *
     * • API 30+ — uses [AccessibilityService.takeScreenshot]; no extra permission dialog.
     * • API 24–29 — launches [ScreenCapturePermissionActivity] which shows the system
     *   "Start recording?" dialog.  On approval the MediaProjection token is stored in
     *   [ScreenshotHelper] and the capture starts immediately.  On denial a failure toast
     *   is shown.
     */
    private fun triggerScreenshot() {
        Log.d(TAG, "triggerScreenshot — API ${Build.VERSION.SDK_INT}")
        secureView.showButtonLoading()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            takeScreenshot(
                Display.DEFAULT_DISPLAY,
                mainExecutor,
                object : TakeScreenshotCallback {
                    override fun onSuccess(result: ScreenshotResult) {
                        screenshotHelper.onA11yScreenshotSuccess(result)
                    }
                    override fun onFailure(errorCode: Int) {
                        screenshotHelper.onA11yScreenshotFailure(errorCode)
                    }
                }
            )
        } else {
            // Set the result callback before the activity is started so it is
            // guaranteed to be in place when the system dialog completes.
            ScreenCapturePermissionActivity.onResult = { resultCode, data ->
                if (resultCode == android.app.Activity.RESULT_OK && data != null) {
                    Log.d(TAG, "MediaProjection permission granted — starting capture")
                    screenshotHelper.setProjectionData(resultCode, data)
                    screenshotHelper.takeViaMediaProjection()
                } else {
                    Log.w(TAG, "MediaProjection permission denied (resultCode=$resultCode)")
                    secureView.hideButtonLoading()
                    secureView.showToastTooltip(getString(R.string.screenshot_failed_toast))
                }
            }
            startActivity(ScreenCapturePermissionActivity.createIntent(this))
        }
    }

    /**
     * Called on the main thread after a screenshot has been saved to the gallery.
     *
     * On success the image is immediately shared to [HomeActivity] via [ACTION_SEND]
     * so the user lands on the MediaPreview screen for further analysis.
     *
     * @param uri The gallery [Uri] of the saved image, or null if saving failed.
     */
    private fun onScreenshotSaved(uri: Uri?) {
        secureView.hideButtonLoading()
        if (uri != null) {
            Log.i(TAG, "Screenshot saved to gallery: $uri")
            Toast.makeText(this, "screenshot take", Toast.LENGTH_SHORT).show()
            shareScreenshotToHome(uri)
        } else {
            Log.w(TAG, "Screenshot save failed")
            secureView.showToastTooltip(getString(R.string.screenshot_failed_toast))
        }
    }

    /**
     * Fires an [Intent.ACTION_SEND] image intent directly at [HomeActivity].
     *
     * Because [HomeActivity] is declared with `launchMode="singleTask"`:
     *  - If the activity is already in the back-stack, [HomeActivity.onNewIntent] is
     *    called and the existing instance handles the share.
     *  - Otherwise a fresh instance is created via [HomeActivity.onCreate].
     *
     * The [Intent.FLAG_GRANT_READ_URI_PERMISSION] flag ensures [HomeActivity] can
     * open the content:// URI even on API 29+ without needing storage permission.
     */
    private fun shareScreenshotToHome(uri: Uri) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            // Target HomeActivity directly — skip the system share-chooser sheet.
            setClassName(
                packageName,
                HOME_ACTIVITY_CLASS
            )
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        Log.d(TAG, "shareScreenshotToHome: sending ACTION_SEND to HomeActivity uri=$uri")
        startActivity(intent)
    }

    private fun ScreenSurface.toAnalysisInput(): AnalysisInput? = when (this) {
        is ScreenSurface.Browser -> url?.let { AnalysisInput.Url(it) }
        is ScreenSurface.Notification -> {
            val text = listOfNotNull(title, content).joinToString(" ").takeIf { it.isNotBlank() }
            content?.let { AnalysisInput.Text(text = it) }
        }
        is ScreenSurface.ActiveCall -> phoneNumber?.let { AnalysisInput.Text(text = it) }
        else -> null
    }

    private fun buildActions(
        feature: FloatingButtonFeature,
        status: DetectionStatus,
        data: Any? = null
    ): List<QuickActionCardView.Action> =
        feature.toActionCardViewListAction(this, status, data) { onDetailAction() }

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
        secureView.updateActionCard(FloatingButtonFeature.SAFE_BROWSING, status, buildActions(FloatingButtonFeature.SAFE_BROWSING, status))
        secureView.showFloatingButton()
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
    // 2.2 Social network / OTT app detection
    // =========================================================================

    /**
     * The host app (KinShield) itself came to the foreground.
     * Always show the floating button.
     */
    private fun onHostAppForeground() {
        isEventForcedVisible = false
        Log.d(TAG, "Host app foreground — showing floating button")
        SurfaceDetector.update(ScreenSurface.App(packageName, DetectionStatus.SAFE))
        secureView.showFloatingButton()
        secureView.updateButton(FloatingButtonFeature.APP_CHECK, DetectionStatus.SAFE)
    }

    /**
     * A known social-network or OTT app entered the foreground.
     * Always show the floating button — no permission evaluation needed.
     */
    private fun onSocialOrOttForeground(pkg: String) {
        isEventForcedVisible = false
        Log.d(TAG, "Social/OTT foreground: $pkg")
        SurfaceDetector.update(ScreenSurface.App(pkg, DetectionStatus.UNKNOWN))
        secureView.showFloatingButton()
        secureView.updateButton(FloatingButtonFeature.APP_CHECK, DetectionStatus.UNKNOWN)
    }

    /**
     * The home-screen / launcher came to the foreground.
     * Hides the floating button unless an event has explicitly forced it visible
     * (e.g. an incoming scam notification while on the home screen).
     */
    private fun onLauncherForeground() {
        if (isEventForcedVisible) {
            Log.d(TAG, "Launcher foreground — button kept visible by event trigger")
            return
        }
        Log.d(TAG, "Launcher foreground — hiding floating button")
        //SurfaceDetector.update(ScreenSurface.Idle)
        //secureView.hideFloatingButton()
    }

    /**
     * Force the floating button to stay visible on the home screen.
     * Call this from any event-driven path (notification scam, incoming call alert, etc.)
     * that should surface the button even when the launcher is in the foreground.
     * The flag is automatically cleared next time the user opens a real app.
     */
    fun showFloatingButtonFromEvent() {
        isEventForcedVisible = true
        secureView.showFloatingButton()
    }

    // =========================================================================
    // 2.3 Call detection
    // =========================================================================

    private fun onCallForeground(pkg: String, event: AccessibilityEvent) {
        val quickNumber = event.text
            .mapNotNull { it?.toString() }
            .firstNotNullOfOrNull { extractPhoneNumber(it) }

        SurfaceDetector.update(ScreenSurface.ActiveCall(quickNumber, DetectionStatus.UNKNOWN))
//        secureView.updateButton(FloatingButtonFeature.CALL_PROTECTION, DetectionStatus.UNKNOWN)
//        secureView.updateActionCard(FloatingButtonFeature.CALL_PROTECTION, DetectionStatus.UNKNOWN)

        if (quickNumber == null) scheduleCallInfoExtraction(pkg)
    }

    private fun scheduleCallInfoExtraction(pkg: String) {
        pendingCallCheck?.let { mainHandler.removeCallbacks(it) }
        pendingCallCheck = Runnable {
            pendingCallCheck = null
            val number = extractPhoneNumberFromTree(rootInActiveWindow)
            Log.d(TAG, "Incomming phone number: $number")
            serviceScope.launch {
                number?.let {
                    schedulePhoneNumberCheck(number)
                }
            }
        }.also { mainHandler.postDelayed(it, CALL_DEBOUNCE_MS) }
    }

    private suspend fun schedulePhoneNumberCheck(phoneNumber: String) {
        val detectionStatus = phoneDetection.detectPhone(phoneNumber)

        secureView.updateButton(FloatingButtonFeature.CALL_PROTECTION, detectionStatus)
        secureView.updateActionCard(FloatingButtonFeature.CALL_PROTECTION, detectionStatus, buildActions(FloatingButtonFeature.CALL_PROTECTION, detectionStatus))
        SurfaceDetector.update(ScreenSurface.ActiveCall(phoneNumber, detectionStatus))
    }

    private fun handleCallEvent(phoneNumber: String, message: String, callEventType: IncomingCallType) {
        pendingCallCheck?.let { mainHandler.removeCallbacks(it) }
        pendingCallCheck = Runnable {
            pendingCallCheck = null
            Log.d(TAG, "Incomming phone number: $phoneNumber")
            Log.d(TAG, "Incomming call event: $callEventType")
            serviceScope.launch {
                val detectionStatus = phoneDetection.detectPhone(phoneNumber)
                SurfaceDetector.update(ScreenSurface.ActiveCall(phoneNumber, detectionStatus, callEventType))
                secureView.updateButton(FloatingButtonFeature.CALL_PROTECTION, detectionStatus)
                showFloatingButtonFromEvent()
                Log.d(TAG, "show tool tip with message: $message")
                secureView.showToastTooltip(message)
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

    /** Handle notification from notification listener service
     */
    private fun onNotificationPosted(pkg: String, title: String, content: String, notificationCategory: NotificationCategory) {
        if (pkg !in AppTrustChecker.NOTIFICATION_SCAN_PACKAGES) {
            Log.d(TAG, "Notification from [$pkg] skipped — not in monitored app list")
            return
        }

        Log.d(TAG, "Notification from [$pkg] content=$content title=$title category=$notificationCategory")


        scheduleNotificationCheck(pkg, title,content, notificationCategory)
    }

    private fun scheduleNotificationCheck(pkg: String, title: String, notificationContent: String, notificationCategory: NotificationCategory) {
        pendingCallCheck?.let { mainHandler.removeCallbacks(it) }
        pendingCallCheck = Runnable {
            pendingCallCheck = null
            serviceScope.launch {
                val (result, message) = if(notificationCategory == NotificationCategory.CALL) {
                    DetectionStatus.WARNING to "Be cautious with unexpected video calls"
                } else {
                    notificationDetection.detectNotificationContent(notificationContent) to "The message you just received can be scam, be careful. Tap for more detail."
                }

                SurfaceDetector.update(ScreenSurface.Notification(pkg, title, notificationContent, result))
                secureView.updateButton(FloatingButtonFeature.SMS_CHECK, result)
                secureView.updateActionCard(FloatingButtonFeature.SMS_CHECK, result, buildActions(FloatingButtonFeature.SMS_CHECK, result))
                if(result == DetectionStatus.WARNING || result == DetectionStatus.DANGEROUS) {
                    secureView.showToastTooltip(message)
                }
                showFloatingButtonFromEvent()
            }
        }.also { mainHandler.postDelayed(it, CALL_DEBOUNCE_MS) }
    }


    // =========================================================================
    // 2.4 Any other user-installed app
    // =========================================================================

    private fun onAppForeground(pkg: String) {

        if (appTrustChecker.isSystemApp(pkg)) {
            Log.d(TAG, "AppTrust skipped — system app [$pkg]")
            return
        }
        isEventForcedVisible = false
        SurfaceDetector.update(ScreenSurface.App(pkg, DetectionStatus.UNKNOWN))
        scheduleAppTrustCheck(pkg)
    }

    private fun scheduleAppTrustCheck(pkg: String) {
        val cached = appTrustChecker.cache[pkg]
        if (cached != null) {
            Log.d(TAG, "AppTrust cache hit [$pkg]")
            SurfaceDetector.update(ScreenSurface.App(pkg, cached))
            applyAppTrustResult(cached, pkg)
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
                    applyAppTrustResult(status, pkg)
                }
            }
        }.also { mainHandler.postDelayed(it, APP_DEBOUNCE_MS) }
    }

    /**
     * Shows or hides the floating button based on the app-trust [status].
     * Button is shown only when the app has dangerous-level permissions (WARNING or DANGEROUS).
     */
    private fun applyAppTrustResult(status: DetectionStatus, pkg: String) {
            secureView.showFloatingButton()
            secureView.updateButton(FloatingButtonFeature.APP_CHECK, status)
            secureView.updateActionCard(
                FloatingButtonFeature.APP_CHECK,
                status,
                buildActions(FloatingButtonFeature.APP_CHECK, status, pkg)
            )
    }


    // =========================================================================
    // Form inspection
    // =========================================================================

    private fun triggerFormInspection(normalUrl: String): Flow<Boolean> {
        return callbackFlow {
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

        // ── Screenshot share target ───────────────────────────────────────────
        private const val HOME_ACTIVITY_CLASS =
            "com.safeNest.demo.features.home.impl.presentation.HomeActivity"

        // Address-bar view IDs are now owned by UrlExtractor.ADDRESS_BAR_VIEW_IDS
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

        val knownId = UrlExtractor.ADDRESS_BAR_VIEW_IDS[pkg]
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
