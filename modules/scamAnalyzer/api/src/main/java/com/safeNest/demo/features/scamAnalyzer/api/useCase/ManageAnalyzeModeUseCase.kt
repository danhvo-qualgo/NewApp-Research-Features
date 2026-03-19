package com.safeNest.demo.features.scamAnalyzer.api.useCase

import com.safeNest.demo.features.scamAnalyzer.api.models.AnalyzeMode

interface ManageAnalyzeModeUseCase {
    suspend fun setMode(mode: AnalyzeMode)
    suspend fun getMode(): AnalyzeMode
}
