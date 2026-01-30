package com.safeNest.features.core.signIn.extension

import android.net.Uri
import androidx.core.net.toUri

object SignInUtil {
    fun entryPoint(): Uri {
        return "internal://signIn".toUri()
    }
}
