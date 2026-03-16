package com.safeNest.demo.features.urlGuard.api

import android.accessibilityservice.AccessibilityService

interface UrlGuardProvider {
    fun getA11yServiceName(): Class<out AccessibilityService>
}