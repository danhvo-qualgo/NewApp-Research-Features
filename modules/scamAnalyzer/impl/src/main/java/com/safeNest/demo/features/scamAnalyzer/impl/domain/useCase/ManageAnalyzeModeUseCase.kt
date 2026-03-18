package com.safeNest.demo.features.scamAnalyzer.impl.domain.useCase

import com.safeNest.demo.features.scamAnalyzer.api.models.AnalyzeMode
import com.safeNest.demo.features.scamAnalyzer.api.useCase.ManageAnalyzeModeUseCase
import com.safeNest.demo.features.scamAnalyzer.impl.data.store.AnalyzeStore
import javax.inject.Inject

class ManageAnalyzeModeUseCaseImpl @Inject constructor(
    private val analyzeStore: AnalyzeStore
) : ManageAnalyzeModeUseCase {
    override suspend fun setMode(mode: AnalyzeMode) {
        analyzeStore.setMode(mode)
    }

    override suspend fun getMode(): AnalyzeMode {
        return analyzeStore.getMode()
    }
}