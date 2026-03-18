package net.qualgo.safeNest.features.notificationInterceptor.impl.presentation

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.os.IBinder
import android.util.Log
import android.view.Gravity
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class ScamWarningOverlayService : Service() {

    private var windowManager: WindowManager? = null
    private var overlayView: LinearLayout? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (overlayView == null) {
            showWarningOverlay()
        }
        return START_NOT_STICKY
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun showWarningOverlay() {
        val wm = windowManager ?: return

        // Root container — full-screen semi-transparent dark background
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(Color.parseColor("#CC000000"))
        }

        // Warning icon
        val icon = TextView(this).apply {
            text = "⚠️"
            textSize = 64f
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, dpToPx(8))
        }
        root.addView(icon)

        // Title
        val title = TextView(this).apply {
            text = "Scam Warning"
            textSize = 22f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setPadding(dpToPx(24), dpToPx(4), dpToPx(24), dpToPx(12))
        }
        root.addView(title)

        // Description
        val description = TextView(this).apply {
            text = "This notification may contain scam content.\nAre you sure you want to continue?"
            textSize = 15f
            setTextColor(Color.parseColor("#CCFFFFFF"))
            gravity = Gravity.CENTER
            setPadding(dpToPx(32), 0, dpToPx(32), dpToPx(32))
        }
        root.addView(description)

        // Button row
        val buttonRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }

        val continueBtn = Button(this).apply {
            text = "I understand"
            textSize = 15f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.parseColor("#FFC62828"))
            layoutParams = LinearLayout.LayoutParams(dpToPx(120), dpToPx(48))
        }
        continueBtn.setOnClickListener { dismissOverlay() }
        buttonRow.addView(continueBtn)

        root.addView(buttonRow)
        overlayView = root

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
        }

        try {
            wm.addView(root, params)
            Log.i(TAG, "Scam warning overlay shown")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to add overlay view", e)
            stopSelf()
        }
    }

    private fun dismissOverlay() {
        overlayView?.let { view ->
            try {
                windowManager?.removeView(view)
            } catch (e: Exception) {
                Log.w(TAG, "removeView failed", e)
            }
        }
        overlayView = null
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        overlayView?.let { view ->
            try {
                windowManager?.removeView(view)
            } catch (e: Exception) {
                Log.w(TAG, "removeView on destroy failed", e)
            }
        }
        overlayView = null
    }

    private fun dpToPx(dp: Int): Int =
        (dp * resources.displayMetrics.density + 0.5f).toInt()

    companion object {
        private const val TAG = "ScamWarningOverlay"
    }
}
