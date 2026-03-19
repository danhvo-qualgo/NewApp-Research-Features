package com.safeNest.demo.features.urlGuard.impl.detection

import com.safeNest.demo.features.urlGuard.impl.detection.model.ModelDetectStatus

interface UrlDetection {
    fun detect(url: String): ModelDetectStatus

    fun onDestroy()
}
