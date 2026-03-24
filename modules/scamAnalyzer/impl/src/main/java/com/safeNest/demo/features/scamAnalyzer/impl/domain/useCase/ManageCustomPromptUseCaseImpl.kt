package com.safeNest.demo.features.scamAnalyzer.impl.domain.useCase

import com.safeNest.demo.features.scamAnalyzer.api.useCase.ManageCustomPromptUseCase
import com.safeNest.demo.features.scamAnalyzer.impl.data.store.AnalyzeStore
import javax.inject.Inject

class ManageCustomPromptUseCaseImpl @Inject constructor(
    private val analyzeStore: AnalyzeStore
) : ManageCustomPromptUseCase {
    override suspend fun getCustomPrompt(): String {
        return getDefaultPrompt()
    }

    override suspend fun setCustomPrompt(prompt: String) {

    }

    override suspend fun getDefaultPrompt(): String {
        return ""
    }
}
