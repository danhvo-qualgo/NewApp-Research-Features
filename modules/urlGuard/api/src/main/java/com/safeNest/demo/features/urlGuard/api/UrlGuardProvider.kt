package com.safeNest.demo.features.urlGuard.api

import android.accessibilityservice.AccessibilityService
import android.content.Context

interface UrlGuardProvider {
    fun getA11yServiceName(): Class<out AccessibilityService>
    fun startService(context: Context)
}