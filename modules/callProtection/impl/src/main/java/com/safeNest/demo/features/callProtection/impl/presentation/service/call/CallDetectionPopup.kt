package com.safeNest.demo.features.callProtection.impl.presentation.service.call

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.PixelFormat
import android.graphics.Point
import android.os.Build
import android.util.Log
import android.util.Size
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.compose.ui.graphics.toArgb
import com.safeNest.demo.features.callProtection.api.domain.model.CallerIdInfoType
import com.safeNest.demo.features.callProtection.impl.R
import com.safeNest.demo.features.designSystem.theme.color.colorAmber600
import com.safeNest.demo.features.designSystem.theme.color.colorGray600
import com.safeNest.demo.features.designSystem.theme.color.colorRed500
import com.safeNest.demo.features.designSystem.theme.color.colorTeal600
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
                Log.d("CallDetectionPopup", "show popup $content")
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
    ): ViewGroup {
        return createCallDetectionPopup(context, content)
    }

    private fun createCallDetectionPopup(context: Context, content: PopupContent)
    = (LayoutInflater.from(context).inflate(R.layout.popup_call_detection, null) as ViewGroup).apply {
        val color = getColorByType(content.type)
        findViewById<FrameLayout>(R.id.cardContainer).backgroundTintList =
            ColorStateList.valueOf(color)
        findViewById<TextView>(R.id.txtAlertLabel).setTextColor(color)
        findViewById<TextView>(R.id.txtAlertTitle).text = getLabelByType(content.type)
        findViewById<ImageView>(R.id.imgAlertIcon).setImageResource(getIconByType(content.type))
    }

    private fun getColorByType(type: CallerIdInfoType) =
        when (type) {
            CallerIdInfoType.SPAM -> colorAmber600.toArgb()
            CallerIdInfoType.SAFE -> colorTeal600.toArgb()
            CallerIdInfoType.UNKNOW -> colorGray600.toArgb()
            CallerIdInfoType.PHISHING -> colorRed500.toArgb()
            else -> colorTeal600.toArgb()
        }

    private fun getIconByType(type: CallerIdInfoType) =
        when (type) {
            CallerIdInfoType.SPAM -> R.drawable.ic_alert_spam
            CallerIdInfoType.SAFE -> R.drawable.ic_alert_safe
            CallerIdInfoType.UNKNOW -> R.drawable.ic_alert_unverified
            CallerIdInfoType.PHISHING -> R.drawable.ic_alert_phishing
            else -> R.drawable.ic_alert_safe
        }

    private fun getLabelByType(type: CallerIdInfoType) =
        when (type) {
            CallerIdInfoType.SPAM -> "Spam - Financial Loan"
            CallerIdInfoType.SAFE -> "Safe - Verified Caller"
            CallerIdInfoType.UNKNOW -> "Unverified Caller"
            CallerIdInfoType.PHISHING -> "Phishing - High Risk"
            else ->  "Safe - Verified Caller"
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
            WindowManager.LayoutParams.WRAP_CONTENT,
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
        val type: CallerIdInfoType
    )
}