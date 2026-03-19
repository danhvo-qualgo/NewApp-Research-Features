package com.safeNest.demo.features.scamAnalyzer.impl.presentation

import android.content.Context
import android.content.Intent
import com.safeNest.demo.features.scamAnalyzer.api.ScamAnalyzerProvider
import javax.inject.Inject

class ScamAnalyzerProviderImpl @Inject constructor() : ScamAnalyzerProvider {
    override fun openActivity(context: Context) {
        val intent = Intent(context, ScamAnalyzerActivity::class.java)
            .apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        context.startActivity(intent)
    }
}