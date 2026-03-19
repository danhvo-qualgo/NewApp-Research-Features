package com.safeNest.demo.features.scamAnalyzer.impl.presentation

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.safeNest.demo.features.designSystem.theme.DSTheme
import com.safeNest.demo.features.scamAnalyzer.impl.presentation.ui.result.AnalysisResultScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ScamAnalyzerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            enableEdgeToEdge(
                statusBarStyle = SystemBarStyle.light(Color.WHITE, Color.WHITE),
                navigationBarStyle = SystemBarStyle.light(Color.WHITE, Color.WHITE)
            )
            DSTheme {
                AnalysisResultScreen(
                    onBackClick = {
                        finish()
                    }
                )
            }
        }
    }
}