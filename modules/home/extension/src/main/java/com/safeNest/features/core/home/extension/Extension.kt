package com.safeNest.features.core.home.extension

import android.net.Uri
import androidx.core.net.toUri

object HomeUtil {
    fun entryPoint(): Uri {
        return "internal://home".toUri()
    }
}
