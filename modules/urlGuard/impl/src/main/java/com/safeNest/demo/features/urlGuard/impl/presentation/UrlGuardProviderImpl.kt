package com.safeNest.demo.features.urlGuard.impl.presentation

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.Intent
import com.safeNest.demo.features.urlGuard.api.UrlGuardProvider
import com.safeNest.demo.features.urlGuard.impl.urlGuard.UrlGuardAccessibilityService
import javax.inject.Inject

class UrlGuardProviderImpl @Inject constructor() : UrlGuardProvider {
    override fun getA11yServiceName(): Class<out AccessibilityService> {
        return UrlGuardAccessibilityService::class.java
    }

    override fun startService(context: Context) {
        val intent = Intent(context, UrlGuardAccessibilityService::class.java)
        intent.action = UrlGuardAccessibilityService.ACTION_SHOW_FLOATING
        context.startService(intent)
    }
}