package com.safeNest.demo.features.scamAnalyzer.impl.presentation

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.safeNest.demo.features.designSystem.theme.DSTheme
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
                AnalysisResultScreen(result = AnalysisResult.Text(
                    status = AnalysisResultStatus.Scam,
                    analysisItems = listOf(
                        AnalysisItem(
                            title = "Artificial Urgency",
                            description = "The message uses high-pressure language (\"URGENT\", \"immediately\") to force a quick reaction.",
                        ),
                        AnalysisItem(
                            title = "Suspicious Link",
                            description = "The URL does not match official bank domains and uses masking techniques.",
                        ),
                        AnalysisItem(
                            title = "Unverified Sender",
                            description = "Phrases like \"Action Required Immediately\" and \"Account Suspension\" are typical pressure tactics.",
                        )
                    ),
                    originalText = "Dear Nguyen Van A your account 018726547 has been locked. Please call +84 908765678 to verify your identity immediately.",
                    maskedText = "Dear N***** A, your account 01234**** has been locked. Please call \u2028+84 90***** to verify your identity immediately."
                )) {  }
            }
        }
    }
}