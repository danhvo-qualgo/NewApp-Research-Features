package com.safeNest.demo.features.callProtection.impl.presentation.service.call

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PixelFormat
import android.graphics.Point
import android.os.Build
import android.util.Size
import android.util.TypedValue
import android.view.ViewGroup
import android.view.WindowManager
import kotlin.math.min

@SuppressLint("StaticFieldLeak")
object CallDetectionPopup {

    private const val MAX_WIDTH_DP = 600f
    private var popupView: ViewGroup? = null

    fun show(applicationContext: Context, content: PopupContent) {
        runCatching {
            val windowManager = getWindowManager(applicationContext) ?: return
            val screenSize = getScreenSize(applicationContext) ?: return

            val width = min(dpToPx(applicationContext, MAX_WIDTH_DP), (screenSize.width * 0.95f).toInt())
            val height = (width / 1.95f).toInt()
            val popupSize = Size(width, height)

            popupView = getPopupView(
                context = applicationContext,
                content = content
            ).also {
                val params = getPopupLayoutParams(popupSize)
                windowManager.addView(it, params)
            }
        }.onFailure {
        }
    }

    fun dismiss(applicationContext: Context) {
        runCatching {
            popupView?.let {
                val windowManager = getWindowManager(applicationContext) ?: return
                windowManager.removeView(it)
            }
        }.onFailure {
        }
        popupView = null
    }

    fun isShowing(): Boolean = popupView != null

    private fun getWindowManager(context: Context): WindowManager? {
        return context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
    }

    private fun getPopupView(
        context: Context, content: PopupContent
    ): ViewGroup? {
        return null
//        return LayoutInflater.from(context).inflate(R.layout.popup_call_detection, null) as ViewGroup
//        val rootView = LayoutInflater.from(context).inflate(R.layout.popup_call_detection, null)

//        rootView.findViewById<TextView>(R.id.label).text = content.label.ifBlank { content.emptyLabel }
//        rootView.findViewById<TextView>(R.id.phoneNumber).text = content.phoneNumber
//
//        val callDetectionTag = CallDetectionTag.get()
//        val tagBgColor = getTagBgColor(callDetectionTag.tagBgColor[content.originalLabel].orEmpty())
//        val tagBgIcon = getTagBgIcon(callDetectionTag.tagBgIcon[content.originalLabel].orEmpty())
//        val tagIcon = getTagIcon(callDetectionTag.tagIcon[content.originalLabel].orEmpty())
//
//        rootView.findViewById<ImageView>(R.id.avatar).let {
//            if (tagIcon != 0) it.setImageResource(tagIcon)
//            else it.isVisible = false
//        }
//
//        rootView.findViewById<ImageView>(R.id.icon).let {
//            if (tagBgIcon != 0) it.setImageResource(tagBgIcon)
//            else it.isVisible = false
//        }
//        rootView.findViewById<TextView>(R.id.caption).setText(
//            if (tagBgColor == R.drawable.bg_gradient_gray) R.string.call_detection_incoming_call_caption_unknown
//            else R.string.call_detection_incoming_call_caption
//        )
//        rootView.setBackgroundResource(tagBgColor)
    }

    private fun dpToPx(context: Context, dp: Float): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp,
            context.resources.displayMetrics
        ).toInt()
    }

    private fun getPopupLayoutParams(popupSize: Size): WindowManager.LayoutParams {
        return WindowManager.LayoutParams(
            popupSize.width,
            popupSize.height,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )
    }

    private fun getScreenSize(context: Context): Size? {
        val windowManager = getWindowManager(context) ?: return null
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val bounds = windowManager.currentWindowMetrics.bounds
            Size(bounds.width(), bounds.height())
        } else {
            val size = Point()
            windowManager.defaultDisplay?.getSize(size)
            Size(size.x, size.y)
        }
    }

    data class PopupContent(
        val phoneNumber: String,
        val tag: String,
        val label: String,
        val originalLabel: String,
        val emptyLabel: String,
        val footer: String
    )
}