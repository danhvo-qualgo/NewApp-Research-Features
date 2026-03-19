package com.safeNest.demo.features.commonAndroid

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings

fun Context.openApp() {
    val intent = this.packageManager
        .getLaunchIntentForPackage(this.packageName)
        ?: return
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
    this.startActivity(intent)
}

fun Context.openAppSettings(packageName: String) {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", packageName, null)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    this.startActivity(intent)
}