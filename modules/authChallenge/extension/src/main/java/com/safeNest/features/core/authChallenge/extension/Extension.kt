package com.safeNest.features.core.authChallenge.extension

import android.net.Uri
import androidx.core.net.toUri

object AuthChallengeUtil {
    fun entryPoint(): Uri {
        return "internal://authChallenge".toUri()
    }
}
